/*
 * Copyright (c) 2002 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;

import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.*;

import edu.umd.lims.util.ErrorHandling;


/*********************************************************************
 MPBatch: Run multiple processes in the background.  Report their
 results in an orderly fashion as they finish and exit when the last
 process has completed.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2006/06/28: Ben
     - add option for time delay after each process is started

   2006/04/27: Ben
     - add cumulative information

   2004/02/09: Ben
     - add threads option to specify maximum number of concurrently
       executing threads

   2005/06/02: Ben
     - add debugging statements

   2003/03/20: Ben
     - add run()

   2002/06/13: Ben
     - use edu.umd.lims.util.ErrorHandling

   2002/01/22: Ben
     - execute the commands within csh

   2002/01/03: Ben
     - additional comments

   2002/01/02: Ben
     - initial version
 </pre>

*********************************************************************/


public class MPBatch extends MPBatchThread {

  static Category cat = Category.getInstance(MPBatch.class.getName());

  public String strCmd;


  /************************************************************** MPBatch */
  /**
   */

  public
    MPBatch(Object oSync, String strCmd)
  {
    super(oSync);

    this.strCmd = strCmd;
  }


  /****************************************************************** run */
  /**
   * Execute a process in the background.
   */

  public void
    doWork()
  {

    try {
      // Start the process
      String[] strExec = {"/bin/csh", "-f", "-c", strCmd};
      Process p = Runtime.getRuntime().exec(strExec);

      // Gather the output
      strOut = collectInputStream(p.getInputStream());
      strErr = collectInputStream(p.getErrorStream());

      // Get the return value
      p.waitFor();
      nReturnCode = p.exitValue();

      // Stop the process
      p.destroy();

    }
    catch (Exception e) {
      exception = e;
    }
  }


  /****************************************************************** run */
  /**
   * Run a list of MPBatchThread's to complete.  The MPBatchThreads's
   * should not have been start()ed yet.
   *
   * @param oSync synchronization object used to create the MPBatchThread's
   *              in vThreads
   * @param vThreads list of MPBatchThread's to wait on
   */

  public static void
    run(Object oSync, Vector vThreads)
    throws InterruptedException
  {
    Vector v = (Vector)vThreads.clone();
    int n;
    MPBatchThread mp;

    // Don't let any spawned threads call oSync.notify() unless we 
    // call oSync.wait()
    synchronized (oSync) {

      // Loop through all the threads
      for (n=0; n < v.size(); n++) {
        // Start the thread
        mp = (MPBatchThread)v.elementAt(n);
        mp.start();
      }
      cat.debug(v.size() + " threads started");

      // Check the threads until there are none left
      while (v.size() > 0) {
        // Wait for an MPBatch to complete 
	cat.debug(v.size() + " threads still running; wait()");
        oSync.wait();
	cat.debug("awakened from wait()");

        // Loop through all the threads
        for (n=0; n < v.size(); ) {
          // Get the thread
          mp = (MPBatchThread)v.elementAt(n);

          // Check if this one is still running
          if (! mp.fDone) {
            // Yes
            n++;

          } else {
            // No, remove it from the list
            v.removeElementAt(n);
	    cat.debug("thread [" + mp.getName() + "] is done");
          }
        }
      }
    }
  }


  /***************************************************************** main */
  /**
   * Main: runtime entry point.
   */

  public static void
    main(String arg[])
    throws Exception
  {
    MPBatch mp;
    Vector v = new Vector();  // Currently executing threads
    Vector vPending = new Vector(); // Pending threads
    int nReturnCode = 0;
    Object oSync = new Object();  // synchronization object for 
                                  // thread notification


    // Get program parameters
    Properties props   = System.getProperties();
    String strLogConf  = props.getProperty("MPBatch.log4j", "log4j.conf");
    String strThreads  = props.getProperty("MPBatch.threads");
    String strDelay    = props.getProperty("MPBatch.delay", "0");

    int nThreads = (strThreads != null 
		    ? (new Integer(strThreads)).intValue() 
		    : Integer.MAX_VALUE);

    int nDelay = Integer.parseInt(strDelay);

    // Setup logging
    File fLogConf = new File(strLogConf);
    if (fLogConf.canRead()) {
      PropertyConfigurator.configure(fLogConf.getName());
      cat.info("Using log conf file: " + fLogConf.getAbsolutePath());
    }
    else {
      ErrorHandling.setDefaultLogging();
      Category root = Category.getRoot();
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
          mp = new MPBatch(oSync, strLine);
          mp.setName(strLine);

	  if (v.size() < nThreads) {
	    mp.start();
	    v.add(mp);

	    // Delay
	    if (nDelay > 0) {
	      try {
		Thread.sleep(nDelay);
	      }
	      catch (Exception e) {}
	    }

	  } else {
	    vPending.add(mp);
	  }
        }
      }

      // Check the threads until there are none left
      while (v.size() > 0) {

        // Wait for an MPBatch to complete 
        oSync.wait();

        // Loop through all the threads
        for (int n=0; n < v.size(); ) {
          // Get the thread
          mp = (MPBatch)v.elementAt(n);

          // Check if this one is still running
          if (! mp.fDone) {
            n++;

          } else {
            // No, remove it from the list
            v.removeElementAt(n);

            // Set MPBatch's return code
            if (mp.nReturnCode != 0)
              nReturnCode = 1;

            // Report process information
            cat.info("*************************************************");
            cat.info("* Command: " + mp.strCmd);
            cat.info("* Start:   " + new Date(mp.lStart));
            cat.info("* Stop:    " + new Date(mp.lStop));
            cat.info("* Elapsed: " + getElapsed(mp.lStop - mp.lStart));
            cat.info("* Return:  " + mp.nReturnCode);

            if (! mp.strOut.equals(""))
              cat.info("* Stdout:\n" + mp.strOut);
            if (! mp.strErr.equals(""))
              cat.info("* Stderr:\n" + mp.strErr);
            if (mp.exception != null)
              cat.info("* Exception:\n" + ErrorHandling.getStackTrace(mp.exception));
	    // Add a pending thread
	    if (vPending.size() > 0) {
	      mp = (MPBatch)vPending.remove(0);
	      mp.start();
	      v.add(mp);

	      // Delay
	      if (nDelay > 0) {
		try {
		  Thread.sleep(nDelay);
		}
		catch (Exception e) {}
	      }

	    }
          }
        }
      }
    }

    Date dStop = new Date();

    // Report cumulative information
    cat.info("*************************************************");
    cat.info("* Cumulative");
    cat.info("* Start:   " + dStart);
    cat.info("* Stop:    " + dStop);
    cat.info("* Elapsed: " + getElapsed(dStop.getTime() - dStart.getTime()));
    cat.info("* Return:  " + nReturnCode);

    System.exit(nReturnCode);
  }
}

