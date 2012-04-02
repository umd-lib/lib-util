/*
 * Copyright (c) 2003 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;


/*********************************************************************
 Xalan extension functions.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/10/17: Ben
     - initial version
 </pre>

*********************************************************************/


public class Xalan {


  /*************************************************************** addAll */
  /**
	* Truncate or blank-pad a string to a set length.
	*
	* @param str source String
	* @param nLength pad length
	* @return padded string
	*/

  public String
	 pad(String str, int nLength)
  {
	 
	 StringBuffer sb = new StringBuffer(str);
	 if (sb.length() > nLength) {
		sb.setLength(nLength);
	 } else {
		while (sb.length() < nLength) {
		  sb.append(" ");
		}
	 }

	 return sb.toString();
  }
}






