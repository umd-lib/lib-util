package edu.umd.lib.util.excelReader;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

public class excelReaderTest {

	@Test
	public void testExcelReader() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch2.xlsx";
		ExcelReader testReader = new ExcelReader(path, "Sheet1_2");
		assertTrue(testReader.isOK());
	}
	
	@Test
	public void testSheet() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch2.xlsx";
		ExcelReader testReader = new ExcelReader(path, "Sheet1_2");
		assertTrue(testReader.isOK());
	}
	
	@Test
	public void testGetLabels() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch2.xlsx";
		ExcelReader testReader = new ExcelReader(path, "Sheet1_2");
		System.out.println("Label list: "+testReader.getLabels());
		assertTrue(testReader.getLabels() != null);
	}
	
	@Test
	public void testGetData() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch.xls";
		ExcelReader testReader = new ExcelReader(path);
		Iterator<HashMap<String, String>> it = testReader.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		System.out.println("Finished.");

	}
	
}
