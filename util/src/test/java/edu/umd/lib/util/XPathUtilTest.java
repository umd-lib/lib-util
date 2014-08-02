package edu.umd.lib.util;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class XPathUtilTest {

  @Before
  public void setUp() throws Exception {
    XPathUtil.setDefaultMapping();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testDefaultMapping() {
    // global namespace mapping
    assertNotNull(DocumentFactory.getInstance().getXPathNamespaceURIs());  

    assertNotNull(DocumentFactory.getInstance().getXPathNamespaceURIs().size() > 0);  
  }

  @Test
  public void testGetXPath() {

    assertNotNull(XPathUtil.getXPath("/a"));

    assertTrue(XPathUtil.getXPath("/a") instanceof XPath);
    
    assertSame("caching", XPathUtil.getXPath("/b"), XPathUtil.getXPath("/b"));
    
    SAXReader reader = new SAXReader();
    
    Document hoursDoc = null;
    try {
      InputStream hours = getClass().getResourceAsStream("/edu/umd/lib/util/hippo-hours.xml");
      assertNotNull("read hippo-hours.xml", hours);
      
      hoursDoc = reader.read(hours);
    }
    catch (DocumentException e) {
      fail(e.getMessage());
    }
    
    assertEquals(1, XPathUtil.getXPath("//sv:node[@sv:name='mck']").selectNodes(hoursDoc).size());
    assertEquals(26, hoursDoc.selectNodes("//sv:property[@sv:name='jcr:primaryType' and @sv:type='Name']").size());

  }
  
  @Test
  public void testAddMapping() {

    SAXReader reader = new SAXReader();
    
    Document hoursDoc = null;
    try {
      InputStream hours = getClass().getResourceAsStream("/edu/umd/lib/util/hippo-hours.xml");
      assertNotNull("read hippo-hours.xml", hours);
      
      hoursDoc = reader.read(hours);
    }
    catch (DocumentException e) {
      fail(e.getMessage());
    }
    
    // additional mappings
   
    Properties p = new Properties();
    try {
      p.load((new Object()).getClass().getResourceAsStream("/edu/umd/lib/util/namespaceAdditional.properties"));
    } catch (Exception e) {
      fail("XPathUtil: unable to load /edu/umd/lib/util/namespaceAdditional.properties : " + e.getMessage());
    }
    XPathUtil.addMapping(p);

    // already compiled xpath gets old mapping
    assertEquals(1, XPathUtil.getXPath("//sv:node[@sv:name='mck']").selectNodes(hoursDoc).size());

    // new compiled xpath gets new mapping
    assertEquals(0, hoursDoc.selectNodes("//sv:node[@sv:name='mck']").size());    

    // new compiled xpath get new mapping
    assertEquals(1, XPathUtil.getXPath("//foobar:node[@foobar:name='mck']").selectNodes(hoursDoc).size());

    // new compiled xpath gets new mapping
    assertEquals(26, hoursDoc.selectNodes("//foobar:property[@foobar:name='jcr:primaryType' and @foobar:type='Name']").size());

  }
}
