/*
 * Copyright (c) 2003 The University of Maryland. All Rights Reserved.
 * 
 * Copied from java.io.PipedReader.java, Copyright 2002 Sun Microsystems
 */


package edu.umd.lims.util;

/*********************************************************************
 Piped Object stream reader.
 Copied from java.io.PipedWriter.java, Copyright 2002 Sun Microsystems

 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/12/15: Ben
     - initial version
 </pre>

*********************************************************************/

public class PipedObjectReader {

  boolean closedByWriter = false;
  boolean closedByReader = false;
  boolean connected = false;

  /* REMIND: identification of the read and write sides needs to be
     more sophisticated.  Either using thread groups (but what about
     pipes within a thread?) or using finalization (but it may be a
     long time until the next GC). */
  Thread readSide;
  Thread writeSide;

  /**
   * The size of the pipe's circular input buffer.
   */
  static final int PIPE_SIZE = 1024;

  /**
   * The circular buffer into which incoming data is placed.
   */
  Object buffer[] = new Object[PIPE_SIZE];

  /**
   * The index of the position in the circular buffer at which the 
   * next Object will be stored when received from the connected 
   * piped writer. <code>in&lt;0</code> implies the buffer is empty, 
   * <code>in==out</code> implies the buffer is full
   */
  int in = -1;

  /**
   * The index of the position in the circular buffer at which the next 
   * character of data will be read by this piped reader.
   */
  int out = 0;

  /***************************************************** PipedObjectReader */
  /**
   * Creates a <code>PipedObjectReader</code> so
   * that it is connected to the piped writer
   * <code>src</code>. Data written to <code>src</code> 
   * will then be  available as input from this stream.
   *
   * @param      src   the stream to connect to.
   * @exception  PipeException  if an error occurs.
   */

  public 
  PipedObjectReader(PipedObjectWriter src) 
    throws PipeException 
  {
    connect(src);
  }


  /**************************************************** PipedObjectReader */
  /**
   * Creates a <code>PipedObjectReader</code> so
   * that it is not  yet connected. It must be
   * connected to a <code>PipedObjectWriter</code>
   * before being used.
   *
   * @see     java.io.PipedObjectReader#connect(java.io.PipedObjectWriter)
   * @see     java.io.PipedObjectWriter#connect(java.io.PipedObjectReader)
   */

  public 
  PipedObjectReader() 
  {}

  /************************************************************** connect */
  /**
   * Causes this piped reader to be connected
   * to the piped  writer <code>src</code>.
   * If this object is already connected to some
   * other piped writer, an <code>PipeException</code>
   * is thrown.
   * <p>
   * If <code>src</code> is an
   * unconnected piped writer and <code>snk</code>
   * is an unconnected piped reader, they
   * may be connected by either the call:
   * <p>
   * <pre><code>snk.connect(src)</code> </pre> 
   * <p>
   * or the call:
   * <p>
   * <pre><code>src.connect(snk)</code> </pre> 
   * <p>
   * The two
   * calls have the same effect.
   *
   * @param      src   The piped writer to connect to.
   * @exception  PipeException  if an error occurs.
   */
    public void connect(PipedObjectWriter src) throws PipeException {
	src.connect(this);
    }
    

  /************************************************************** receive */
  /**
   * Receives an Object.  This method will block if no input is
   * available.
   */

  synchronized void 
  receive(Object o) 
    throws PipeException 
  {
    if (!connected) {
      throw new PipeException("Pipe not connected");
    } else if (closedByWriter || closedByReader) {
      throw new PipeException("Pipe closed");
    } else if (readSide != null && !readSide.isAlive()) {
      throw new PipeException("Read end dead");
    }

    writeSide = Thread.currentThread();
    while (in == out) {
      if ((readSide != null) && !readSide.isAlive()) {
	throw new PipeException("Pipe broken");
      }
      /* full: kick any waiting readers */
      notifyAll();	
      try {
	wait(1000);
      } catch (InterruptedException ex) {
	throw new PipeException("Interrupted pipe");
      }
    }
    if (in < 0) {
      in = 0;
      out = 0;
    }
    buffer[in++] = o;
    if (in >= buffer.length) {
      in = 0;
    }
  }

  
  /********************************************************* receivedLast */
  /**
   * Notifies all waiting threads that the last character of data has been
   * received.
   */

  synchronized void 
  receivedLast() 
  {
    closedByWriter = true;
    notifyAll();
  }

  
  /**************************************************************** read */
  /**
   * Reads the next Object from this piped stream.
   * If no Object is available because the end of the stream 
   * has been reached, the value <code>null</code> is returned. 
   * This method blocks until input data is available, the end of
   * the stream is detected, or an exception is thrown. 
   *
   * If a thread was providing Objects
   * to the connected piped writer, but
   * the  thread is no longer alive, then an
   * <code>PipeException</code> is thrown.
   *
   * @return     the next Object, or <code>null</code> if the end of the
   *             stream is reached.
   * @exception  PipeException  if the pipe is broken.
   */

  public synchronized Object 
  read()  
    throws PipeException 
  {
    if (!connected) {
      throw new PipeException("Pipe not connected");
    } else if (closedByReader) {
      throw new PipeException("Pipe closed");
    } else if (writeSide != null && !writeSide.isAlive()
	       && !closedByWriter && (in < 0)) {
      throw new PipeException("Write end dead");
    }

    readSide = Thread.currentThread();
    int trials = 2;
    while (in < 0) {
      if (closedByWriter) { 
	/* closed by writer, return EOF */
	return null;
      }
      if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
	throw new PipeException("Pipe broken");
      }
      /* might be a writer waiting */
      notifyAll();
      try {
	wait(1000);
      } catch (InterruptedException ex) {
	throw new PipeException("Interrupted pipe");
      }
    }
    Object ret = buffer[out++];
    if (out >= buffer.length) {
      out = 0;
    }
    if (in == out) {
      /* now empty */
      in = -1;		
    }
    return ret;
  }


  /**************************************************************** ready */
  /**
   * Tell whether this stream is ready to be read.  A piped
   * stream is ready if the circular buffer is not empty.
   *
   * @exception  PipeException  If an error occurs
   */

  public synchronized boolean 
  ready() 
    throws PipeException 
  {
    if (!connected) {
      throw new PipeException("Pipe not connected");
    } else if (closedByReader) {
      throw new PipeException("Pipe closed");
    } else if (writeSide != null && !writeSide.isAlive()
	       && !closedByWriter && (in < 0)) {
      throw new PipeException("Write end dead");
    }
    if (in < 0) {
      return false;
    } else {
      return true;
    }
  }
 

  /**************************************************************** close */
  /**
   * Closes this piped stream and releases any system resources 
   * associated with the stream. 
   *
   * @exception  PipeException  if an error occurs.
   */

  public void 
  close()  
    throws PipeException 
  {
    in = -1;
    closedByReader = true;
  }
}
