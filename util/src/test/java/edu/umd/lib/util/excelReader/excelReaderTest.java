package edu.umd.lib.util.excelReader;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


public class excelReaderTest {

	@Test
	public void testExcelReader() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch2.xlsx";
		ExcelReader testReader = new ExcelReader(path, "Sheet1_2");			
		assertTrue(testReader.workbook != null);
	}	
		
	@Test
	public void testSheet() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch.xls";
		ExcelReader testReader = new ExcelReader(path, "Sheet1_2");
		assertTrue(testReader.sheet != null);
	}
		
	@Test
	public void testGetLabels() throws IOException{
		String path = "src/test/resources/edu/umd/lib/util/PrangeBatch.xls";
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

  @Test
  public void testStaffData() throws IOException {
    String path = "src/test/resources/edu/umd/lib/util/people.xls";
    ExcelReader staffdir = new ExcelReader(path);
    Iterator<HashMap<String, String>> iterator = staffdir.iterator();
    while(iterator.hasNext()){
      System.out.println(iterator.next());
    }
    System.out.println("Finished");
  }    

}
