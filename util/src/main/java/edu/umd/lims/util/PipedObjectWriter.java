/*
 * Copyright (c) 2003 The University of Maryland. All Rights Reserved.
 * 
 * Copied from java.io.PipedWriter.java, Copyright 2002 Sun Microsystems
 */

package edu.umd.lims.util;


/*********************************************************************
 Piped Object stream writer. 
 Copied from java.io.PipedWriter.java, Copyright 2002 Sun Microsystems

 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/12/15: Ben
     - initial version
 </pre>

*********************************************************************/


public class PipedObjectWriter {

  /* REMIND: identification of the read and write sides needs to be
     more sophisticated.  Either using thread groups (but what about
     pipes within a thread?) or using finalization (but it may be a
     long time until the next GC). */
  private PipedObjectReader sink;

  /* This flag records the open status of this particular writer. It
   * is independent of the status flags defined in PipedObjectReader. It is
   * used to do a sanity check on connect.
   */
  private boolean closed = false;


  /***************************************************** PipedObjectWriter */
  /**
   * Creates a piped writer connected to the specified piped 
   * reader. Objects written to this stream will then be 
   * available as input from <code>sink</code>.
   *
   * @param      sink   The piped reader to connect to.
   * @exception  PipeException  if an error occurs.
   */

  public 
    PipedObjectWriter(PipedObjectReader sink)  
    throws PipeException 
  {
    connect(sink);
  }
    
  /**************************************************** PipedObjectWriter */
  /**
   * Creates a piped writer that is not yet connected to a 
   * piped reader. It must be connected to a piped reader, 
   * either by the receiver or the sender, before being used. 
   *
   * @see     java.io.PipedObjectReader#connect(java.io.PipedObjectWriter)
   * @see     java.io.PipedObjectWriter#connect(java.io.PipedObjectReader)
   */

    public 
      PipedObjectWriter() 
    {}
    

  /************************************************************** connect */
  /**
   * Connects this piped writer to a receiver. If this object
   * is already connected to some other piped reader, a
   * <code>PipeException</code> is thrown.
   * <p>
   * If <code>sink</code> is an unconnected piped reader and 
   * <code>src</code> is an unconnected piped writer, they may 
   * be connected by either the call:
   * <blockquote><pre>
   * src.connect(sink)</pre></blockquote>
   * or the call:
   * <blockquote><pre>
   * sink.connect(src)</pre></blockquote>
   * The two calls have the same effect.
   *
   * @param      sink   the piped reader to connect to.
   * @exception  PipeException  if an error occurs.
   */
  
  public synchronized void 
    connect(PipedObjectReader sink) 
    throws PipeException 
  {
    if (sink == null) {
      throw new NullPointerException();
    } else if (this.sink != null || sink.connected) {
      throw new PipeException("Already connected");
    } else if (sink.closedByReader || closed) {
      throw new PipeException("Pipe closed");
    }
        
    this.sink = sink;
    sink.in = -1;
    sink.out = 0;
    sink.connected = true;
  }


  /**************************************************************** write */
  /**
   * Writes the specified <code>Object</code> to the piped stream.
   * If a thread was reading Objects from the connected piped
   * stream, but the thread is no longer alive, then a 
   * <code>PipeException</code> is thrown.
   *
   * @param      o   the <code>Object</code> to be written.
   * @exception  PipeException  if an error occurs.
   */

  public void 
    write(Object o)  
    throws PipeException 
  {
    if (sink == null) {
      throw new PipeException("Pipe not connected");
    } else if (o == null) {
      throw new NullPointerException("Object for stream must not be null");
    }

    sink.receive(o);
  }

  /**************************************************************** flush */
  /**
   * Flushes this output stream and forces any buffered Objects
   * to be written out. 
   * This will notify any readers that characters are waiting in the pipe.
   *
   * @exception PipeException if an error occurs.
   */

  public synchronized void 
    flush() 
    throws PipeException 
  {
    if (sink != null) {
      if (sink.closedByReader || closed) {
	throw new PipeException("Pipe closed");
      }            
      synchronized (sink) {
	sink.notifyAll();
      }
    }
  }

  /**************************************************************** close */
  /**
   * Closes this piped output stream and releases any system resources 
   * associated with this stream. This stream may no longer be used for 
   * Objects.
   *
   * @exception  PipeException  if an error occurs.
   */
  
  public void 
  close()  
    throws PipeException 
  {
    closed = true;
    if (sink != null) {
      sink.receivedLast();
    }
  }

}

