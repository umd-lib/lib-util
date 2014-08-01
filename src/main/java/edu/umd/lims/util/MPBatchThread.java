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
 MPBatchThread: A Thread, with a little more.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2002/11/11: Ben
     - initial version
 </pre>

*********************************************************************/


public abstract class MPBatchThread extends Thread {

  static Category cat = Category.getInstance(MPBatch.class.getName());

  public String strCmd;
  public String strOut;
  public String strErr;
  public int nReturnCode;
  public Exception exception;
  public long lStart;
  public long lStop;
  public boolean fDone;

  public Object oSync = new Object();  // synchronization object for 
                                       // thread notification


  /******************************************************** MPBatchThread */
  /**
   */

  public
    MPBatchThread(Object oSync)
  {
    super();

    fDone = false;
    this.oSync = oSync;
  }


  /********************************************************* collectStream */
  /**
   * Collect IO from an InputStream.
   *
   * @param is The InputStream
   * @return The data read from the InputStream
   */

  static public String
    collectInputStream(InputStream is)
    throws IOException
  {
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

  public abstract void
    doWork();


  /************************************************************ getElapsed */
  /**
   * Generate a String format for the elapsed time HH:MM:SS
   */

  static public String
    getElapsed(long lTime)
  {
    // Convert from milliseconds to seconds
    lTime /= 1000;

    // Get hours, minutes, seconds
    int nHr = (int)lTime / 3600;
    lTime %= 3600;
    int nMn = (int)lTime / 60;
    lTime %= 60;
    int nSc = (int)lTime;

    // Convert to a string and return
    return(((nHr < 10) ? "0" + nHr : new Integer(nHr).toString()) + 
           ":" + 
           ((nMn < 10) ? "0" + nMn : new Integer(nMn).toString()) + 
           ":" + 
           ((nSc < 10) ? "0" + nSc : new Integer(nSc).toString())
           );
  }

         
  /****************************************************************** run */
  /**
   * Execute a process in the background.
   */

   public void
    run()
  {

    strOut = "";
    strErr = "";
    nReturnCode = -1;
    exception = null;
    lStart = System.currentTimeMillis();
    lStop = 0;

    // Do the work of the thread
    cat.debug("Calling doWork [" + getName() + "]");
    doWork();

    // Set the completion time
    lStop = System.currentTimeMillis();

    // Logging
    if (cat.isDebugEnabled() || exception != null) {
      StringBuffer sb = new StringBuffer();
      sb.append("doWork complete [ + " + getName() + "]\n");
      sb.append("* Start:   " + new Date(lStart) + "\n");
      sb.append("* Stop:    " + new Date(lStop) + "\n");
      sb.append("* Elapsed: " + getElapsed(lStop - lStart) + "\n");
      sb.append("* Return:  " + nReturnCode + "\n");
    
      if (! strOut.equals(""))
        sb.append("* Stdout:\n" + strOut + "\n");
      if (! strErr.equals(""))
        sb.append("* Stderr:\n" + strErr + "\n");
      if (exception != null)
        sb.append("* Exception:\n" + ErrorHandling.getStackTrace(exception) + "\n");
      
      if (exception != null)
        cat.error(sb.toString());
      else
        cat.debug(sb.toString());
    }

    // Notify the main thread of completion
    synchronized (oSync) {
      try {
        cat.debug("Notifying oSync of completion [" + getName() + "]");
        fDone = true;
        oSync.notify();
      }
      catch (Exception e) {
        cat.error(ErrorHandling.getStackTrace(e));
      }
    }
  }
}

