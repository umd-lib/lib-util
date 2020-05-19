/*
 * Copyright (c) 2014 The University of Maryland. All Rights Reserved.
 *
 */

package edu.umd.lib.util.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import edu.umd.lib.util.MPBatch;
import edu.umd.lib.util.MPBatchThread;
import edu.umd.lims.util.ErrorHandling;

import com.google.gson.stream.JsonWriter;

/**
 * MPBatch: Run multiple processes in the background. Report their results in an
 * orderly fashion as they finish and exit when the last process has completed.
 *
 * @author Ben Wallberg
 */

public class MPBatchApp {

  static Logger log = Logger.getLogger(MPBatchApp.class);

  /**
   * Number of threads to run simultaneously
   */
  private static int nThreads = Integer.MAX_VALUE;

  /**
   * Delay in milliseconds after starting each thread
   */
  private static int nDelay = 0;

  /**
   * Enable debug logging.
   */
  private static boolean debug = false;

  /**
   * Log4J configuration file.
   */
  private static File log4jConfig = null;

  /**
   * JSON format log file.
   */
  private static File jsonFile = null;

  /**
   * Exec style commands.
   */
  private static boolean exec = false;

  /***************************************************************** main */
  /**
   * Main: runtime entry point.
   */

  public static void main(String args[]) throws Exception {

    /**
     * Currently executing threads
     */
    ArrayList<MPBatch> lCurrent = new ArrayList<MPBatch>();

    /**
     * Pending threads
     */
    ArrayList<MPBatch> lPending = new ArrayList<MPBatch>();

    /**
     * Accumulate running time for each thread
     */
    long cumulative = 0l;

    int nReturnCode = 0;

    /**
     * synchronization object for thread notification
     */
    Object oSync = new Object();

    parseCommandLine(args);

    // Setup logging
    if (log4jConfig != null && log4jConfig.canRead()) {
      PropertyConfigurator.configure(log4jConfig.getName());
      log.info("Using log conf file: " + log4jConfig.getAbsolutePath());
    } else {
      if (debug) {
        ErrorHandling.setDebugLogging();
      } else {
        ErrorHandling.setDefaultLogging();
        Logger root = Logger.getRootLogger();
        root.removeAllAppenders();
        PatternLayout l = new PatternLayout("%m%n");
        root.addAppender(new ConsoleAppender(l));
      }
    }

    // Setup JSON logging
    JsonWriter jsonWriter = null;
    if (jsonFile != null) {
      jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(jsonFile)));
      jsonWriter.setIndent("    ");
      jsonWriter.beginObject();
      jsonWriter.name("processes");
      jsonWriter.beginArray();
    }

    // Don't let any spawned threads call oSync.notify() unless we
    // call oSync.wait()
    Date dStart = new Date();
    synchronized (oSync) {

      // Read in the commands and kick them off
      BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
      String strLine;
      while ((strLine = brIn.readLine()) != null) {
        if (!strLine.equals("") && !strLine.startsWith("#")) {
          MPBatch mp = new MPBatch(oSync, strLine, exec);
          mp.setName(strLine);

          if (lCurrent.size() < nThreads) {
            mp.start();
            lCurrent.add(mp);

            // Delay
            if (nDelay > 0) {
              try {
                Thread.sleep(nDelay);
              } catch (Exception e) {
              }
            }

          } else {
            lPending.add(mp);
          }
        }
      }

      // Check the threads until there are none left
      while (lCurrent.size() > 0) {

        // Wait for an MPBatch to complete
        oSync.wait();

        // Loop through all the threads
        ListIterator<MPBatch> iter = lCurrent.listIterator();
        while (iter.hasNext()) {
          // Get the thread
          MPBatch mp = iter.next();

          // Check if this one is done running
          if (mp.fDone) {

            // Yes, remove it from the list
            iter.remove();

            // Set MPBatch's return code
            if (mp.nReturnCode != 0)
              nReturnCode = 1;

            // Report process information
            StringBuilder sb = new StringBuilder();
            sb.append("*************************************************\n");
            sb.append("* Command:      " + mp.strCmd + "\n");
            sb.append("* Start:        " + new Date(mp.lStart) + "\n");
            sb.append("* Stop:         " + new Date(mp.lStop) + "\n");
            sb.append("* Elapsed Time: "
                + MPBatchThread.getElapsed(mp.lStop - mp.lStart) + "\n");
            sb.append("* Return:  " + mp.nReturnCode + "\n");
            log.info(sb);

            cumulative += mp.lStop - mp.lStart;

            if (!mp.strOut.equals(""))
              log.info("* Stdout:\n" + mp.strOut);
            if (!mp.strErr.equals(""))
              log.info("* Stderr:\n" + mp.strErr);
            if (mp.exception != null)
              log.info("* Exception:\n"
                  + ErrorHandling.getStackTrace(mp.exception));

            // JSON logging
            if (jsonWriter != null) {
              jsonWriter.beginObject();
              jsonWriter.name("command").value(mp.strCmd);
              jsonWriter.name("start").value((new Date(mp.lStart)).toString());
              jsonWriter.name("stop").value((new Date(mp.lStop)).toString());
              jsonWriter.name("status").value(mp.nReturnCode);
              jsonWriter.name("stdout").value(mp.strOut);
              jsonWriter.name("stderr").value(mp.strErr);
              if (mp.exception != null) {
               jsonWriter.name("exception").value(ErrorHandling.getStackTrace(mp.exception));
              }
              jsonWriter.endObject();
              jsonWriter.flush();
            }

            // Add a pending thread
            if (lPending.size() > 0) {
              mp = lPending.remove(0);
              mp.start();
              iter.add(mp);

              // Delay
              if (nDelay > 0) {
                try {
                  Thread.sleep(nDelay);
                } catch (Exception e) {
                }
              }

            }
          }
        }
      }
    }

    Date dStop = new Date();

    // Report cumulative information
    StringBuilder sb = new StringBuilder();
    sb.append("*************************************************\n");
    sb.append("* Final\n");
    sb.append("* Start:           " + dStart + "\n");
    sb.append("* Stop:            " + dStop + "\n");
    sb.append("* Elapsed Time:    "
        + MPBatchThread.getElapsed(dStop.getTime() - dStart.getTime()) + "\n");
    sb.append("* Cumulative Time: " + MPBatchThread.getElapsed(cumulative)
        + "\n");
    sb.append("* Return:          " + nReturnCode + "\n");
    log.info(sb);

    // JSON logging
    if (jsonWriter != null) {
      jsonWriter.endArray();

      jsonWriter.name("cumulative");
      jsonWriter.beginObject();
      jsonWriter.name("start").value(dStart.toString());
      jsonWriter.name("stop").value(dStop.toString());
      jsonWriter.name("elapsed").value(MPBatchThread.getElapsed(dStop.getTime() - dStart.getTime()));
      jsonWriter.name("cumulative").value(MPBatchThread.getElapsed(cumulative));
      jsonWriter.name("status").value(nReturnCode);
      jsonWriter.endObject();

      jsonWriter.endObject();

      jsonWriter.close();
    }

    System.exit(nReturnCode);
  }

  /**
   * Parse command line and set options.
   *
   * @param args
   * @throws ParseException
   */
  private static void parseCommandLine(String[] args) throws ParseException {
    // Setup the options
    Options options = new Options();
    Option option;

    option = new Option("n", "threads", true,
        "number of simultaneous threads (default is one for each command");
    options.addOption(option);

    option = new Option("t", "time delay", true,
        "number of milliseconds to delay after starting each process (default is 0)");
    options.addOption(option);

    option = new Option("l", "log4j-config", true, "log4j properties file");
    options.addOption(option);

    option = new Option("d", "debug", false, "debug logging");
    options.addOption(option);

    option = new Option("h", "help", false, "get this list");
    options.addOption(option);

    option = new Option("j", "json-logfile", true, "json format log file");
    options.addOption(option);

    option = new Option("e", "exec", false, "commands are tab delimited list of parameters to exec()");
    options.addOption(option);

    // Parse the command line
    if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
      printUsage(options);
    }

    PosixParser parser = new PosixParser();
    CommandLine cmd = parser.parse(options, args);

    // Handle results
    if (cmd.hasOption('h')) {
      printUsage(options);
    }

    if (cmd.hasOption('n')) {
      nThreads = Integer.parseInt(cmd.getOptionValue('n'));
    }

    if (cmd.hasOption('t')) {
      nDelay = Integer.parseInt(cmd.getOptionValue('t'));
    }

    if (cmd.hasOption('l')) {
      log4jConfig = new File(cmd.getOptionValue('l'));
      if (!log4jConfig.canRead()) {
        printUsage(options, "Unable to open " + log4jConfig.getAbsoluteFile()
            + " for reading");
      }
    }

    if (cmd.hasOption('d')) {
      debug = true;
    }

    if (cmd.hasOption('j')) {
      jsonFile = new File(cmd.getOptionValue('j'));
      if (jsonFile.exists() && !jsonFile.canWrite()) {
        printUsage(options, "Unable to open " + jsonFile.getAbsoluteFile()
            + " for writing");
      }
    }

    if (cmd.hasOption('e')) {
      exec = true;
    }
  }

  /**
   * Print command-line usage to System.err and exit
   *
   * @param options
   * @param msgs
   */
  public static void printUsage(Options options, String... msgs) {
    PrintWriter err = new PrintWriter(System.err, true);

    // print messages
    for (String msg : msgs) {
      err.println(msg);
    }
    if (msgs.length != 0) {
      err.println();
    }

    StringBuffer footer = new StringBuffer();
    footer.append("\n");
    footer.append("Reads each line of stdin as a separate process to run.");
    footer.append(" Empty lines and those beginning with # are skipped.");
    footer.append(" Normally, each line is a single process string which is sent");
    footer.append(" to /bin/csh for interpretation.  If the -e flag is enabled,");
    footer.append(" each line is a tab delimited list of raw parameters to exec()");

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(err, 80,
        "MPBatchApp [-n threads] [-t delay] [-l log4j-config] [-j json-logfile] [-e] [-d]", null,
        options, 2, 2, footer.toString());

    err.close();

    System.exit(1);
  }

}
