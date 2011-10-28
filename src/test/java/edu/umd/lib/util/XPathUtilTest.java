package edu.umd.lib.util;

import static org.junit.Assert.*;

import org.dom4j.DocumentFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class XPathUtilTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testStaticInitialization() {
    // global namespace mapping
    XPathUtil.setDefault();
    assertNotNull(DocumentFactory.getInstance().getXPathNamespaceURIs());  
  }

  @Test
  @Ignore("not ready")
  public void testGetXPath() {
    // global namespace mapping has been set
    assertNotNull(DocumentFactory.getInstance().getXPathNamespaceURIs());
  }
}
