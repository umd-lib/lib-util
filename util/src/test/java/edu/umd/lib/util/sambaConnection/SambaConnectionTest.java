package edu.umd.lib.util.sambaConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Test;

import edu.umd.lib.util.excelReader.ExcelReader;

public class SambaConnectionTest {

  @Test
  public void test() throws IOException {

    String username = null;
    String password = null;
    String domain = null;
    String sharePath = null;
    boolean sambaWorks = false;

    if (sambaWorks) {
      /*
       * the Properties file should be placed in ref folder under project
       * directory
       */
      /*
       * In Windows it will perhaps be
       * "C:\Users\allan\Documents\workspace-sts-2.8.0.RELEASE\lib-util\ref"
       */
      // Not having a working samba connection to play with we will skip this
      String testFilePath = System.getProperty("user.dir");
      File file = new File(testFilePath + File.separator + "ref"
          + File.separator + "connection.properties");
      FileInputStream fileInput = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(fileInput);
      fileInput.close();
      /* get information from .properties file. */
      username = properties.getProperty("username");
      password = properties.getProperty("password");
      domain = properties.getProperty("domain");
      sharePath = properties.getProperty("sharepath");
      SMBConnection smbConnection = new SMBConnection(username, password,
          sharePath, domain);
      System.out.println(smbConnection.listFile());
      System.out.println(smbConnection.gotoDirectory("FedoraBatch"));
      smbConnection.setFile("SamplePrangeBatch.xls");
      ExcelReader testReader = new ExcelReader(smbConnection);
      System.out.println(testReader.getLabels());
      Iterator<HashMap<String, String>> it = testReader.iterator();
      while (it.hasNext()) {
        System.out.println(it.next());
      }
      System.out.println("Finished.");
    }
  }

}
