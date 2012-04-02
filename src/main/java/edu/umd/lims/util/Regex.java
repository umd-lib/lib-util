/*
 * Copyright (c) 2004 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;


/*********************************************************************
 Regular expression utilities.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2005/01/27: Ben
     - add date ranges

   2004/02/13: Ben
     - initial version
 </pre>

*********************************************************************/


public class Regex
{
  protected static Category cat = Category.getInstance(Regex.class.getName());

  static Pattern pYear = Pattern.compile("(.*)\\{\\{y\\s*((?:\\d{4})?\\s*-\\s*(?:\\d{4})?)\\s*\\}\\}(.*)");
  static Pattern pDate = Pattern.compile("(.*)\\{\\{d\\s*((?:\\d{8})?\\s*-\\s*(?:\\d{8})?)\\s*\\}\\}(.*)");

  
  /*************************************************************** expand */
  /**
   * Expand a regular expression:
   * <p>
   * <ul>
   * <li>year range: {{yYYYY-YYYY}} (cf. /lims/bin/year2re)
   * </ul>
   */

  public static String
  expand(String strRegex)
  {
    String strRet = strRegex;

    // Expand all year expansions
    Matcher m = pYear.matcher(strRet);
    while (m.matches()) {
      strRet = m.group(1) + expandYearRange(m.group(2)) + m.group(3);
      m = pYear.matcher(strRet);
    }

    // Expand all date expansions
    m = pDate.matcher(strRet);
    while (m.matches()) {
      strRet = m.group(1) + expandDateRange(m.group(2)) + m.group(3);
      m = pDate.matcher(strRet);
    }

    return strRet;
  }


  /******************************************************* expandYearRange */
  /**
   * Use /lims/bin/year2re to expand the year range as an r.e.
   */

  public static String
    expandYearRange(String strYearRange)
  {
    String strRet = "**Unable to expand year range**";

    try {
      cat.debug("Attempting to start /lims/bin/year2re");

      Runtime r = Runtime.getRuntime();

      // Start the process
      Process p = r.exec(new String[] {"/lims/bin/year2re", "-d", strYearRange});

      // Setup i/o
      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

      cat.debug("/lims/bin/year2re sucessfully started");

      // Read the result
      String strLine = in.readLine();
      if (strLine != null) {
	strRet = strLine;
      }

    }
    catch (Exception e) {
      cat.error("Error running /lims/bin/year2re\n" +
		ErrorHandling.getStackTrace(e));
    }

    return strRet;
  }


  /******************************************************* expandDateRange */
  /**
   * Use /lims/bin/date2re to expand the date range as an r.e.
   */

  public static String
    expandDateRange(String strDateRange)
  {
    String strRet = "**Unable to expand date range**";

    try {
      cat.debug("Attempting to start /lims/bin/date2re");

      Runtime r = Runtime.getRuntime();

      // Start the process
      Process p = r.exec(new String[] {"/lims/bin/date2re", "-d", strDateRange});

      // Setup i/o
      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

      cat.debug("/lims/bin/date2re sucessfully started");

      // Read the result
      String strLine = in.readLine();
      if (strLine != null) {
	strRet = strLine;
      }

    }
    catch (Exception e) {
      cat.error("Error running /lims/bin/date2re\n" +
		ErrorHandling.getStackTrace(e));
    }

    return strRet;
  }


  /***************************************************************** main */
  /**
   * Command-line interface for testing.
   */

  public static void
    main(String argv[])
    throws Exception
  {
    ErrorHandling.setDefaultLogging();

    System.out.println(expand("abc"));
    System.out.println(expand("{{y1990-1999}}abc"));
    System.out.println(expand("a{{y 1999 - 2001 }}bc"));
    System.out.println(expand("a{{y1999-2001}}b{{y1999-}}c"));
    System.out.println(expand("abc{{y-}}"));

    System.out.println(expand("abc{{d-}}abc"));
    System.out.println(expand("{{d20030000-20040000}}"));
  }

}






