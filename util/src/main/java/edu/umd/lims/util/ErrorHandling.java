/*
 * Copyright (c) 2001 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;

import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.log4j.Category;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Priority;


/*********************************************************************
 ErrorHandling utilities.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2002/02/13: Ben
     - change the default log pattern

   2001/12/14: Ben
     - change the default priority for log4j logging to INFO.

   2001/10/18: Ben
     - initial version
 </pre>

*********************************************************************/

public class ErrorHandling {


  /******************************************************** getStackTrace */
  /**
   * Return a stack trace print captured to a string.
   */

  public static String
  getStackTrace(Throwable t)
  {
    // Create a StringWriter to catch the stack trace.
    StringWriter sw = new StringWriter();

    // Create a PrintWriter which Throwable needs
    PrintWriter pw = new PrintWriter(sw, true);

    // Print the stack trace
    t.printStackTrace(pw);

    // Return the String version of the stack trace.
    return sw.toString();
  }


  /***************************************************** setDefaultLogging */
  /**
   * Setup default log4j logging.
   */

  public static void
    setDefaultLogging()
  {
    setDefaultLogging(ConsoleAppender.SYSTEM_OUT);
  }


  /***************************************************** setDefaultLogging */
  /**
   * Setup default log4j logging.
   */

  public static void
    setDefaultLogging(String strTarget)
  {
    // Get the root category
    Category root = Category.getRoot();

    // Add a ConsoleAppender
    PatternLayout l = new PatternLayout("[%d] [%-5p]: (%c{2})%n%m%n%n");
    root.addAppender(new ConsoleAppender(l, strTarget));

    // Set the Priority to INFO
    root.setPriority(Priority.INFO);
  }


  /******************************************************* setDebugLogging */
  /**
   * Setup debug log4j logging.
   */

  public static void
    setDebugLogging()
  {
    setDebugLogging(ConsoleAppender.SYSTEM_OUT);
  }


  /******************************************************* setDebugLogging */
  /**
   * Setup debug log4j logging.
   */

  public static void
    setDebugLogging(String strTarget)
  {
    // Get the root category
    Category root = Category.getRoot();

    // Add a ConsoleAppender
    PatternLayout l = new PatternLayout("[%d] [%-5p]: (%c{2})%n%m%n%n");
    root.addAppender(new ConsoleAppender(l, strTarget));

    // Set the Priority to DEBUUG
    root.setPriority(Priority.DEBUG);
  }
}
