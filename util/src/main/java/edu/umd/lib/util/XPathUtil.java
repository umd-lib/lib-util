package edu.umd.lib.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
  
  {
    setDefaultMapping();
  }
  
  /**
   * Set global namespace prefix/uri mapping based on default values 
   */
  public static void setDefaultMapping() {
    // namespace initialization
    Map<String,String> namespaces = new HashMap<String,String>();
    
    Properties p = new Properties();
    try {
      p.load((new Object()).getClass().getResourceAsStream("/edu/umd/lib/util/namespace.properties"));
    } catch (Exception e) {
      System.err.println("XPathUtil: unable to load /edu/umd/lib/util/namespace.properties : " + e.getMessage());
    }
    
    for (Object key : p.keySet()) {
      namespaces.put((String)key, (String)p.get(key));
    }
    
    DocumentFactory.getInstance().setXPathNamespaceURIs(namespaces); 
  }

  /**
   * Add additional namespace prefix/uri mappings from a Properties object
   */
  public static void addMapping(Map m) {
    DocumentFactory.getInstance().getXPathNamespaceURIs().putAll(m);
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
