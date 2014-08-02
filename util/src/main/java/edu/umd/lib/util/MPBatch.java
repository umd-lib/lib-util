/*
 * Copyright (c) 2014 The University of Maryland. All Rights Reserved.
 *
 */

package edu.umd.lib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import edu.umd.lims.util.ErrorHandling;

/**
 * MPBatch: Run multiple processes in the background. Report their results in an
 * orderly fashion as they finish and exit when the last process has completed.
 *
 * @author Ben Wallberg
 */

public class MPBatch extends MPBatchThread {

  static Logger log = Logger.getLogger(MPBatch.class);

  public String strCmd;

  public MPBatch(Object oSync, String strCmd) {
    super(oSync);

    this.strCmd = strCmd;
  }

  /****************************************************************** run */
  /**
   * Execute a process in the background.
   */

  @Override
  public void doWork() {

    try {
      // Start the process
      String[] strExec = { "/bin/csh", "-f", "-c", strCmd };
      Process p = Runtime.getRuntime().exec(strExec);

      // Gather the output
      strOut = collectInputStream(p.getInputStream());
      strErr = collectInputStream(p.getErrorStream());

      // Get the return value
      p.waitFor();
      nReturnCode = p.exitValue();

      // Stop the process
      p.destroy();

    } catch (Exception e) {
      exception = e;
    }
  }

  /****************************************************************** run */
  /**
   * Run a list of MPBatchThread's to complete. The MPBatchThreads's should not
   * have been start()ed yet.
   *
   * @param oSync
   *          synchronization object used to create the MPBatchThread's in
   *          vThreads
   * @param vThreads
   *          list of MPBatchThread's to wait on
   */

  public static void run(Object oSync, Vector<MPBatchThread> vThreads)
      throws InterruptedException {
    Vector<MPBatchThread> v = (Vector<MPBatchThread>) vThreads.clone();
    int n;
    MPBatchThread mp;

    // Don't let any spawned threads call oSync.notify() unless we
    // call oSync.wait()
    synchronized (oSync) {

      // Loop through all the threads
      for (n = 0; n < v.size(); n++) {
        // Start the thread
        mp = v.elementAt(n);
        mp.start();
      }
      log.debug(v.size() + " threads started");

      // Check the threads until there are none left
      while (v.size() > 0) {
        // Wait for an MPBatch to complete
        log.debug(v.size() + " threads still running; wait()");
        oSync.wait();
        log.debug("awakened from wait()");

        // Loop through all the threads
        for (n = 0; n < v.size();) {
          // Get the thread
          mp = v.elementAt(n);

          // Check if this one is still running
          if (!mp.fDone) {
            // Yes
            n++;

          } else {
            // No, remove it from the list
            v.removeElementAt(n);
            log.debug("thread [" + mp.getName() + "] is done");
          }
        }
      }
    }
  }
}
