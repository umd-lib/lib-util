/*
 * Copyright (c) 2003 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;

/*********************************************************************
 Pipe exception used by {@link PipeObjectReader} and 
 {@link PipeObjectWriter}.
 
 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/12/15: Ben
     - initial version
 
 </pre>

*********************************************************************/

public class PipeException extends Exception {

  public
  PipeException(String message)
  {
    super(message);
  }

}
