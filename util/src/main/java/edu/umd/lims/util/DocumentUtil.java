/*
 * Copyright (c) 2001 The University of Maryland. All Rights Reserved.
 * 
 */

package edu.umd.lims.util;


import java.io.*;
import java.util.regex.Pattern;

import java.util.Vector;

import org.w3c.dom.*;

import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.DOMParser;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


/*********************************************************************
 XML Document utilities.

 @author  Ben Wallberg

 <pre>
 Revision History:

   2003/12/18: Ben
     - change xmlSerialize to not wrap

   2003/11/20: Ben
     - change xmlNormalize() to strip out invalid control characters

   2003/10/10: Ben
     - fixed getNodeValue() to not throw NullPointerException when the
       selected Node has no value.

   2003/10/02: Ben
     - added xmlPrint(), xmlNormalize(), and sortAttributes() all
       of which were taken from the Xerces sample dom.DOMWriter class.
     - added xmlSerialize()

   2003/08/21: Ben
     - add traverse(node, int, PrintWriter)

   2001/10/25: Ben
     - initial version
 </pre>

*********************************************************************/


public class DocumentUtil
{
  public static Pattern pControl = Pattern.compile("[\u0000-\u0008\u000B-\u000C\u000E-\u001F\u007F-\u0084\u0086-\u009F]");

//  /************************************************************ getPCDATA */
//  /**
//   * Extract the text of the PCDATA subnode.
//   *
//   * @param Node
//   */
//
//  public static String
//  getPCDATA(Node e)
//  {
//    Node child = e.getChild(0);
//    if (child != null && child.getNodeType() == Node.PCDATA)
//      return child.getText();
//    else
//      return "";
//  }
//   
//   
//  /********************************************************** printNode */
//  /**
//   * Print an Node to out
//   *
//   * @param PrintStream
//   * @param Node - the XML Node
//   * @param level - indentation level
//   */
//
//  public static void
//  printNode(PrintStream out, Node e, int level)
//  {
//    Enumeration n;
//
//    // Generate the indention string
//    String i = "";
//    for (int x =0; x < level; x++)
//      i = i + "  ";
//
//    // Node Name
//    String t = e.getNodeValue();
//    if (t != null)
//      out.println(i + "Tag name: " + t);
//
//    // Node Type
//    String strType;
//    switch (e.getNodeType()) {
//    case Node.ATTRIBUTE_NOTE:              strType = "ATTRIBUTE";      break;
//    case Node.CDATA_SECTION_NODE:          strType = "CDATA_SECTION";  break;
//    case Node.COMMENT_NODE:                strType = "COMMENT";        break;
//    case Node.DOCUMENT_FRAGMENT_NODE:       strType = "DOC_FRAGMENT";   break;
//    case Node.DOCUMENT_NODE:               strType = "DOCUMENT";       break;
//    case Node.DOCUMENT_TYPE_NODE:          strType = "DOCUMENT_TYPE";  break;
//    case Node.ENTITY_NODE:                 strType = "ENTITY";         break;
//    case Node.ENTITY_REFERENCE_NODE:       strType = "ENTITYREF";      break;
//    case Node.NOTATION_NODE:               strType = "NOTATION";       break;
//    case Node.PROCESSING_INSTRUCTION_NODE: strType = "PI";             break;
//    case Node.TEXT_NODE:                   strType = "TEXT";     break;
//    default:                               strType = "???";            break;
//    }
//    out.println(i + "Type: " + strType);
//    
//    // Node Attributes
//    if (e.hasAttributes()) {
//    for (n = e.getAttributes(); n.hasMoreNodes() ;) {
//      a = (Attribute)n.nextNode();
//      out.println(i + "Attribute: " + a.getName() + "=" + a.getValue());
//    }
//    out.println();
//
//    // Node Text
//    if (e.getNodeType() == Node.PCDATA)
//      out.println(i + "Text: " + e.getText());
//
//  }
//
//
//  /******************************************************* printNodeTree */
//  /**
//   * Print to out an XML Node tree.
//   *
//   * @param PrintStream
//   * @param Node - the XML Node
//   * @param level - indentation level
//   */
//
//  public static void
//  printNodeTree(PrintStream out, Node n, int level)
//  {
//    Enumeration n;
//
//
//    // Recurse on the Node Children
//    Node child;
//    for (n = e.getNodes(); n.hasMoreNodes() ;) {
//      child = (Node)n.nextNode();
//      if (child != null)
//        printNodeTree(out, child,level+1);
//    }
//  }


  /************************************************************* getNode */
  /**
   * Extract 1 node by name from a Node tree.
   */

  public static Node
    getNode(Node node, String strPath)
  {
    Vector v = getNodes(node, strPath);

    if (v == null || v.size() != 1)
      return null;

    return ((Node)v.elementAt(0));
  }
   
 
  /************************************************************** getNodes */
  /**
   * Extract nodes by name from a Node tree.
   */

  public static Vector
    getNodes(Node node, String strPath)
  {

    
    Vector vRet = new Vector();

    // Error checking
    if (node == null || strPath == null)
      return vRet;

    // Strip preceeding '/'
    while (strPath.startsWith("/"))
      strPath = strPath.substring(1);

    // Parse strPath
    String strThis;   // First element in the search path
    String strRest;   // Everything else
    int ndx = strPath.indexOf('/');
    if (ndx == -1) {
      strThis = strPath;
      strRest = null;
    } else {
      strThis = strPath.substring(0,ndx);
      strRest = strPath.substring(ndx+1);
    }

    //System.out.println("GN: strThis=" + strThis + 
    //                   ", strRest=" + strRest +
    //                   ", strNode=" + node.getNodeName());

    // Handle the node types
    int nType = node.getNodeType();

    switch (nType) {

    case Node.DOCUMENT_NODE: {
      // Document node
      vRet.addAll(getNodes(((Document)node).getDocumentElement(), strPath));
      break;
    }

    case Node.ELEMENT_NODE: {
      if (node.getNodeName().equals(strThis) || strThis.equals("*")) {

        // Is this it?
        if (strRest == null) {
          vRet.add(node);

        } else if (strRest.startsWith("#attr/")) {
          // Attributes
          NamedNodeMap attrs = node.getAttributes();
          for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
            vRet.addAll(getNodes(attrs.item(i), strRest.substring(6)));
          }

        } else {

          // Children
          NodeList children = node.getChildNodes();
          if (children != null) {
            int len = children.getLength();
            for (int i = 0; i < len; i++) {
              vRet.addAll(getNodes(children.item(i), strRest));
            }
          }
        }
      }
      break;
    }

    case Node.TEXT_NODE: 
    case Node.ATTRIBUTE_NODE:
      {
        // Is this it?
        if (strRest == null && node.getNodeName().equals(strThis))
          vRet.add(node);
      }
    }

    return vRet;
  }


  /******************************************************** getNodeValue */
  /**
   * Extract 1 node by name from a Node tree and extract its value.
   */

  public static String
    getNodeValue(Node node, String strPath)
  {
    Vector v = getNodes(node, strPath);

    if (v == null || v.size() != 1)
      return null;

    String strValue = ((Node)v.elementAt(0)).getNodeValue();
    return (strValue != null ? strValue.trim() : null);
  }
   
 
  /************************************************************* toString */
  /** 
   * String representation of a node.
   */

  public String 
    toString(Node node)
  {
    StringWriter sw = new StringWriter();
    traverse(node, 0, new PrintWriter(sw));
    return sw.toString();
  }


  /**********************************************************************/
  /** 
   * Traverses the specified node and prints it to System.out
   */

  public static void
    traverse(Node node, int level) 
  {
    traverse(node, level, new PrintWriter(System.out));
  }


  /**********************************************************************/
  /** 
   * Traverses the specified node and prints it to a PrintWriter.
   */

  public static void
    traverse(Node node, int level, PrintWriter out) 
  {

    // is there anything to do?
    if (node == null) {
      return;
    }

    // Print
    String strIndent = "";
    for (int i = 0; i < level; i++) 
      strIndent += "-";
    out.println(strIndent + node);

    int type = node.getNodeType();
    switch (type) {

    case Node.DOCUMENT_NODE: {
      traverse(((Document)node).getDocumentElement(),level+1, out);
      break;
    }

    // print element with attributes
    case Node.ELEMENT_NODE: {

      // Attributes
      out.println(strIndent + "Attributes:");
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
          traverse(attrs.item(i),level+1, out);
        }
      }

      // Children
      out.println(strIndent + "Children:");
      NodeList children = node.getChildNodes();
      if (children != null) {
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
          traverse(children.item(i),level+1,out);
        }
      }
      break;
    }

    // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE: {
      NodeList children = node.getChildNodes();
      if (children != null) {
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
          traverse(children.item(i),level+1,out);
        }
      }
      break;
    }

    // print text
    case Node.CDATA_SECTION_NODE: {
      break;
    }

    case Node.TEXT_NODE: {
      break;
    }
    }
    
  } // traverse(Node)


  /********************************************************* xmlSerialize */
  /**
   * Serialize the DOM document.
   */

  public static void
    xmlSerialize(Document doc, Writer w)
    throws IOException
  {
    OutputFormat f = new OutputFormat(doc,"utf8",true);
    f.setIndent(2);
    f.setLineWidth(0);
    XMLSerializer s = new XMLSerializer(w, f);
    s.serialize(doc);
  }


  /********************************************************* xmlSerialize */
  /**
   * Serialize the DOM document.
   */

  public static void
    xmlSerialize(Document doc, Element e, Writer w)
    throws IOException
  {
    OutputFormat f = new OutputFormat(doc,"utf8",true);
    f.setIndent(2);
    f.setLineWidth(0);
    f.setOmitXMLDeclaration(true);
    XMLSerializer s = new XMLSerializer(w, f);
    s.serialize(e);
  }


  /************************************************************** xmlPrint */
  /** 
   * Prints the specified node, recursively.
   */

  public static void 
    xmlPrint(Node node, PrintWriter out) 
  {
    xmlPrint(node, out, "");
  }


  /************************************************************** xmlPrint */
  /** 
   * Prints the specified node, recursively.
   */

  public static void 
    xmlPrint(Node node, PrintWriter out, String nd) 
  {

    // is there anything to do?
    if ( node == null ) {
      return;
    }

    int type = node.getNodeType();
    switch ( type ) {
      // print document
    case Node.DOCUMENT_NODE: {
      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

      NodeList children = node.getChildNodes();
      for ( int iChild = 0; iChild < children.getLength(); iChild++ ) {
        xmlPrint(children.item(iChild), out);
      }
      out.flush();
      break;
    }

    // print element with attributes
    case Node.ELEMENT_NODE: {
      out.print(nd + "<");
      out.print(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for ( int i = 0; i < attrs.length; i++ ) {
        Attr attr = attrs[i];
        out.print(' ');
        out.print(attr.getNodeName());
        out.print("=\"");
        out.print(xmlNormalize(attr.getNodeValue()));
        out.print('"');
      }
      out.print(">\n");
      NodeList children = node.getChildNodes();
      if ( children != null ) {
        int len = children.getLength();
        for ( int i = 0; i < len; i++ ) {
          xmlPrint(children.item(i), out, nd+"  ");
        }
      }
      break;
    }

    // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE: {
      NodeList children = node.getChildNodes();
      if ( children != null ) {
        int len = children.getLength();
        for ( int i = 0; i < len; i++ ) {
          xmlPrint(children.item(i), out, nd+"  ");
        }
      }
      break;
    }

    // print cdata sections
    case Node.CDATA_SECTION_NODE: {
      out.print(xmlNormalize(node.getNodeValue()));
      break;
    }

    // print text
    case Node.TEXT_NODE: {
      out.print(xmlNormalize(node.getNodeValue()));
      // I think we're modifying the text??
      out.print("\n");
      break;
    }

    // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE: {
      out.print(nd + "<?");
      out.print(node.getNodeName());
      String data = node.getNodeValue();
      if ( data != null && data.length() > 0 ) {
        out.print(' ');
        out.print(data);
      }
      out.println("?>\n");
      break;
    }
    }

    if ( type == Node.ELEMENT_NODE ) {
      out.print(nd+"</");
      out.print(node.getNodeName());
      out.print(">\n");
    }

    out.flush();

  } // print(Node)


  /******************************************************* sortAttributes */
  /** 
   * Returns a sorted list of attributes. 
   */

  public static Attr[] 
    sortAttributes(NamedNodeMap attrs) 
  {

    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for ( int i = 0; i < len; i++ ) {
      array[i] = (Attr)attrs.item(i);
    }
    for ( int i = 0; i < len - 1; i++ ) {
      String name  = array[i].getNodeName();
      int    index = i;
      for ( int j = i + 1; j < len; j++ ) {
        String curName = array[j].getNodeName();
        if ( curName.compareTo(name) < 0 ) {
          name  = curName;
          index = j;
        }
      }
      if ( index != i ) {
        Attr temp    = array[i];
        array[i]     = array[index];
        array[index] = temp;
      }
    }

    return(array);

  }


  /******************************************************* xmlStripControl */
  /**
   * Strip out control characters which are invalid xml.
   */

  public static String
    xmlStripControl(String s)
  {
    return pControl.matcher(s).replaceAll(" ");
  }


  /********************************************************* xmlNormalize */
  /** 
   * Normalizes the given string.
   */

  public static String 
    xmlNormalize(String s) 
  {
    StringBuffer str = new StringBuffer();

    s = xmlStripControl(s);

    int len = (s != null) ? s.length() : 0;
    for ( int i = 0; i < len; i++ ) {
      char ch = s.charAt(i);

      switch ( ch ) {
      case '<': {
        str.append("&lt;");
        break;
      }
      case '>': {
        str.append("&gt;");
        break;
      }
      case '&': {
        str.append("&amp;");
        break;
      }
      case '"': {
        str.append("&quot;");
        break;
      }
      case '\'': {
        str.append("&apos;");
        break;
      }
      default: {
        str.append(ch);
      }
      }
    }

    return(str.toString());

  }


  /***************************************************************** main */
  /**
   * Command-line interface for testing.
   */

  public static void
    main(String argv[])
    throws Exception
  {
    // Create the document
    Document doc = new DocumentImpl();

    Node root = doc.appendChild(doc.createElement("report"));
    Element bib  = (Element)root.appendChild(doc.createElement("bib"));
    bib.appendChild(doc.createElement("item"));
    Element item = (Element)bib.appendChild(doc.createElement("item"));
    item.appendChild(doc.createTextNode("barcode here asdfjlasdf;lkjasf asdfljsdal;j asdfl;jsdj sadfljsdaf;jklsdf ;asfl;dsf ;ldsf;ldsf ;ladfl;dsf"));
    item.setAttribute("id","123");

    xmlSerialize(doc, new PrintWriter(System.out));
    xmlSerialize(doc, bib, new PrintWriter(System.out));
  }

}
