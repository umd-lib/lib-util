/*
 * Copyright (c) 2002 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;

import java.util.Iterator;

/*********************************************************************
 Extend {@link java.util.TreeSet TreeSet} by:
 <ul>
 <li> addAll(Object [])
 </ul>

 @author  Ben Wallberg

 <pre>
 Revision History:

   2002/12/02: Ben
     - initial version
 </pre>

*********************************************************************/


public class TreeSet extends java.util.TreeSet {


  /*************************************************************** addAll */
  /**
	* Add all elements of an array to the TreeSet.
	*
	* @param array
	* @return <tt>true</tt> if this collection was changed as a result
	*         of the call.
	*/

  public boolean 
	 addAll(Object array[])
  {

	 boolean bModified = false;
	 for (int i=0; i < array.length; i++) {
		if (add(array[i]))
		  bModified = true;
	 }

	 return bModified;
  }


  /*************************************************************** getAll */
  /**
	* Get all elements, sorted, of the TreeSet in an array.
	*
	* @return object array
	*/

  public Object[]
	 getAll()
  {

	 Object array[] = new Object[size()];

	 Iterator e = iterator();
	 int i=0;
	 for (; e.hasNext(); i++)
		array[i] = e.next();

	 return array;
  }

}






