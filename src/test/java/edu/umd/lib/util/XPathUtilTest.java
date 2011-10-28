package edu.umd.lib.util;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

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
    XPathUtil.setDefault();
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
}
