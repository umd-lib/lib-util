/*
 * Copyright (c) 2014 The University of Maryland. All Rights Reserved.
 *
 */

package edu.umd.lib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

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

      // Close the input
      p.getOutputStream().close();

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
   * @param lThreads
   *          list of MPBatchThread's to wait on
   */

  public static void run(Object oSync, List<MPBatchThread> lThreads)
      throws InterruptedException {
    ArrayList<MPBatchThread> l = new ArrayList<MPBatchThread>(lThreads);

    // Don't let any spawned threads call oSync.notify() unless we
    // call oSync.wait()
    synchronized (oSync) {

      // Loop through all the threads
      for (MPBatchThread mp : l) {
        // Start the thread
        mp.start();
      }
      log.debug(l.size() + " threads started");

      // Check the threads until there are none left
      while (l.size() > 0) {
        // Wait for an MPBatch to complete
        log.debug(l.size() + " threads still running; wait()");
        oSync.wait();
        log.debug("awakened from wait()");

        // Loop through all the threads
        Iterator<MPBatchThread> iter = l.iterator();
        while (iter.hasNext()) {
          // Get the thread
          MPBatchThread mp = iter.next();

          // Check if this one is done running
          if (mp.fDone) {
            // Yes, remove it from the list
            iter.remove();
            log.debug("thread [" + mp.getName() + "] is done");
          }
        }
      }
    }
  }
}
