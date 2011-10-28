package edu.umd.lib.util;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentFactory;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;

/**
 * Utility class for working with org.dom4j.XPath objects.
 * 
 * <ul>
 * <li>Set default global namespace mapping</li>
 * <li>Create cached xpath objects</li>
 * </ul>
 *  
 * @author wallberg
 */
public class XPathUtil {

  private static final Map<String,XPath> cache = new HashMap<String,XPath>();
  
  /**
   * Set global namespace prefix/uri mapping based on default values 
   */
  public static void setDefault() {
    // namespace initialization
    Map<String,String> namespaces = new HashMap<String,String>();
    
    namespaces.put("oai_dc","http://www.openarchives.org/OAI/2.0/oai_dc/");
    namespaces.put("sparql","http://www.w3.org/2001/sw/DataAccess/rf1/result");
    namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    namespaces.put("mets", "http://www.loc.gov/METS/");
    namespaces.put("xlink", "http://www.w3.org/1999/xlink");
    namespaces.put("marc", "http://www.loc.gov/MARC21/slim");
    namespaces.put("doInfo", "http://www.itd.umd.edu/fedora/doInfo");
    namespaces.put("amInfo", "http://www.itd.umd.edu/fedora/amInfo");
    
    DocumentFactory.getInstance().setXPathNamespaceURIs(namespaces); 
  }

  /**
   * Get a compiled XPath object for the expression. Cache.
   */
  public static XPath getXPath(String xpath) throws InvalidXPathException {

    XPath result = null;

    if (cache.containsKey(xpath)) {
      result = cache.get(xpath);

    } else {
      result = DocumentFactory.getInstance().createXPath(xpath);
      cache.put(xpath, result);
    }

    return result;
  }
}
