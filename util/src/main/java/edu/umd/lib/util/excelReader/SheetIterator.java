package edu.umd.lib.util.excelReader;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class SheetIterator implements Iterable<HashMap<String, String>>, Iterator<HashMap<String, String>>{
	
	private int position;
	private ExcelReader itReader;
	
	public SheetIterator(ExcelReader reader) {
		position = 0;
		itReader = reader;
		
	}

	
	public boolean hasNext() {
		
		if (this.itReader.sheet == null) {
		  System.err.println("lib-util:SheetIterator - The ExcelReader has a null sheet.");
		  return false;
		}
		
	  if( position < this.itReader.sheet.getLastRowNum()){
			try {
				if(this.itReader.getData(position+1) != null){
				return true;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	
	public HashMap<String, String> next() {
		
		position += 1;
		HashMap<String, String>dataMap = null;
		try {
			dataMap = this.itReader.getData(position);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataMap;
	}

	
	public void remove() {
		// TODO Auto-generated method stub
		
	}


  @Override
  public Iterator<HashMap<String, String>> iterator() {
    // TODO Auto-generated method stub
    return this;
  }



}
