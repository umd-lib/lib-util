/*
 * Copyright (c) 2014 The University of Maryland. All Rights Reserved.
 *
 */

package edu.umd.lib.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.umd.lims.util.ErrorHandling;
import edu.umd.lims.util.StringBuffer;

/**
 * MPBatchThread: A Thread, with a little more.
 *
 * @author Ben Wallberg
 *
 */

public abstract class MPBatchThread extends Thread {

  static Logger log = Logger.getLogger(MPBatch.class);

  public String strCmd;
  public String strOut;
  public String strErr;
  public int nReturnCode;
  public Exception exception;
  public long lStart;
  public long lStop;
  public boolean fDone;

  /**
   * synchronization object for thread notification
   */
  public Object oSync = new Object();

  /******************************************************** MPBatchThread */
  /**
   */

  public MPBatchThread(Object oSync) {
    super();

    fDone = false;
    this.oSync = oSync;
  }

  /********************************************************* collectStream */
  /**
   * Collect IO from an InputStream.
   *
   * @param is
   *          The InputStream
   * @return The data read from the InputStream
   */

  static public String collectInputStream(InputStream is) throws IOException {
    // Gather the result
    int ch;
    StringBuffer sbRet = new StringBuffer();

    while ((ch = is.read()) != -1)
      sbRet.append((char) ch);

    return sbRet.toString();
  }

  /*************************************************************** doWork */
  /**
   * Do the work of the thread.
   */

  public abstract void doWork();

  /************************************************************ getElapsed */
  /**
   * Generate a String format for the elapsed time HH:MM:SS
   */

  static public String getElapsed(long lTime) {
    // Convert from milliseconds to seconds
    lTime /= 1000;

    // Get hours, minutes, seconds
    int nHr = (int) lTime / 3600;
    lTime %= 3600;
    int nMn = (int) lTime / 60;
    lTime %= 60;
    int nSc = (int) lTime;

    // Convert to a string and return
    return (((nHr < 10) ? "0" + nHr : new Integer(nHr).toString()) + ":"
        + ((nMn < 10) ? "0" + nMn : new Integer(nMn).toString()) + ":" + ((nSc < 10) ? "0"
        + nSc
        : new Integer(nSc).toString()));
  }

  /****************************************************************** run */
  /**
   * Execute a process in the background.
   */

  @Override
  public void run() {

    strOut = "";
    strErr = "";
    nReturnCode = -1;
    exception = null;
    lStart = System.currentTimeMillis();
    lStop = 0;

    // Do the work of the thread
    log.debug("Calling doWork [" + getName() + "]");
    doWork();

    // Set the completion time
    lStop = System.currentTimeMillis();

    // Logging
    if (log.isDebugEnabled() || exception != null) {
      StringBuffer sb = new StringBuffer();
      sb.append("doWork complete [ + " + getName() + "]\n");
      sb.append("* Start:   " + new Date(lStart) + "\n");
      sb.append("* Stop:    " + new Date(lStop) + "\n");
      sb.append("* Elapsed: " + getElapsed(lStop - lStart) + "\n");
      sb.append("* Return:  " + nReturnCode + "\n");

      if (!strOut.equals(""))
        sb.append("* Stdout:\n" + strOut + "\n");
      if (!strErr.equals(""))
        sb.append("* Stderr:\n" + strErr + "\n");
      if (exception != null)
        sb.append("* Exception:\n" + ErrorHandling.getStackTrace(exception)
            + "\n");

      if (exception != null)
        log.error(sb.toString());
      else
        log.debug(sb.toString());
    }

    // Notify the main thread of completion
    synchronized (oSync) {
      try {
        log.debug("Notifying oSync of completion [" + getName() + "]");
        fDone = true;
        oSync.notify();
      } catch (Exception e) {
        log.error(ErrorHandling.getStackTrace(e));
      }
    }
  }
}
