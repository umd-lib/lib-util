/*
 * Copyright (c) 2014 The University of Maryland. All Rights Reserved.
 *
 */

package edu.umd.lib.util.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import edu.umd.lib.util.MPBatch;
import edu.umd.lib.util.MPBatchThread;
import edu.umd.lims.util.ErrorHandling;

/**
 * MPBatch: Run multiple processes in the background. Report their results in an
 * orderly fashion as they finish and exit when the last process has completed.
 *
 * @author Ben Wallberg
 */

public class MPBatchApp {

  static Logger log = Logger.getLogger(MPBatchApp.class);

  /***************************************************************** main */
  /**
   * Main: runtime entry point.
   */

  public static void main(String arg[]) throws Exception {

    /**
     * Currently executing threads
     */
    ArrayList<MPBatch> lCurrent = new ArrayList<MPBatch>();

    /**
     * Pending threads
     */
    ArrayList<MPBatch> lPending = new ArrayList<MPBatch>();

    int nReturnCode = 0;

    /**
     * synchronization object for thread notification
     */
    Object oSync = new Object();

    // Get program parameters
    Properties props = System.getProperties();
    String strLogConf = props.getProperty("MPBatch.log4j", "log4j.conf");
    String strThreads = props.getProperty("MPBatch.threads");
    String strDelay = props.getProperty("MPBatch.delay", "0");

    int nThreads = (strThreads != null ? (new Integer(strThreads)).intValue()
        : Integer.MAX_VALUE);

    int nDelay = Integer.parseInt(strDelay);

    // Setup logging
    File fLogConf = new File(strLogConf);
    if (fLogConf.canRead()) {
      PropertyConfigurator.configure(fLogConf.getName());
      log.info("Using log conf file: " + fLogConf.getAbsolutePath());
    } else {
      ErrorHandling.setDefaultLogging();
      Logger root = Logger.getRootLogger();
      root.removeAllAppenders();
      PatternLayout l = new PatternLayout("%m%n");
      root.addAppender(new ConsoleAppender(l));
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
          MPBatch mp = new MPBatch(oSync, strLine);
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
            log.info("*************************************************");
            log.info("* Command: " + mp.strCmd);
            log.info("* Start:   " + new Date(mp.lStart));
            log.info("* Stop:    " + new Date(mp.lStop));
            log.info("* Elapsed: "
                + MPBatchThread.getElapsed(mp.lStop - mp.lStart));
            log.info("* Return:  " + mp.nReturnCode);

            if (!mp.strOut.equals(""))
              log.info("* Stdout:\n" + mp.strOut);
            if (!mp.strErr.equals(""))
              log.info("* Stderr:\n" + mp.strErr);
            if (mp.exception != null)
              log.info("* Exception:\n"
                  + ErrorHandling.getStackTrace(mp.exception));

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
    log.info("*************************************************");
    log.info("* Cumulative");
    log.info("* Start:   " + dStart);
    log.info("* Stop:    " + dStop);
    log.info("* Elapsed: "
        + MPBatchThread.getElapsed(dStop.getTime() - dStart.getTime()));
    log.info("* Return:  " + nReturnCode);

    System.exit(nReturnCode);
  }
}
