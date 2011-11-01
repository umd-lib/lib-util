package edu.umd.lib.util.sambaConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Test;

import edu.umd.lib.util.excelReader.*;

public class SambaConnectionTest {


	@Test
	public void test() throws IOException {
		
		String username = null;
		String password = null;
		String domain = null;
		String sharePath = null;
		
		File file = new File("connection.properties");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		
		/*get information from .properties file.*/		
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		domain = properties.getProperty("domain");
		sharePath = properties.getProperty("sharepath");
		
		
		SMBConnection smbConnection = new SMBConnection(username, password, sharePath, domain);
		
		System.out.println(smbConnection.listFile());
		System.out.println(smbConnection.gotoDirectory("FedoraBatch"));
		smbConnection.setFile("src/test/resources/edu/umd/lib/util/SamplePrangeBatch.xls");
		
		ExcelReader testReader = new ExcelReader(smbConnection);
		System.out.println(testReader.getLabels());
		Iterator<HashMap<String, String>> it = testReader.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		System.out.println("Finished.");
	}

}
