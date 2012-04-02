/*
 * Copyright (c) 2003 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;


import java.io.IOException;
import java.io.FilterWriter;
import java.io.Writer;


/*********************************************************************
 Tee a write stream to two Writers.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/12/05: Ben
     - initial version
 </pre>

*********************************************************************/


public class TeeWriter extends Writer
{

  /**
   * The underlying character-output streams.
   */
  protected Writer out1;
  protected Writer out2;


  /************************************************************ TeeWriter */
  /**
   * Create a new filtered writer.
   *
   * @param out  a Writer object to provide the underlying stream.
   */

  public
  TeeWriter(Writer out1, Writer out2) 
  {
    super();
    this.out1 = out1;
    this.out2 = out2;
  }


  /**************************************************************** write */
  /**
   * Write a portion of an array of characters.
   *
   * @param  cbuf  Buffer of characters to be written
   * @param  off   Offset from which to start reading characters
   * @param  len   Number of characters to be written
   *
   * @exception  IOException  If an I/O error occurs
   */

  public void 
  write(char cbuf[], int off, int len) 
    throws IOException 
  {
    out1.write(cbuf, off, len);
    out2.write(cbuf, off, len);
  }

  /**************************************************************** flush */
  /**
   * Flush the stream.
   *
   * @exception  IOException  If an I/O error occurs
   */

  public void 
  flush() 
    throws IOException 
  {
    out1.flush();
    out2.flush();
  }

  /**************************************************************** close */
  /**
   * Close the stream.
   *
   * @exception  IOException  If an I/O error occurs
   */

  public void 
  close() 
    throws IOException 
  {
    out1.close();
    out2.close();
  }

}






