package edu.umd.lib.util.excelReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.umd.lib.util.sambaConnection.SMBConnection;

/**
 * An ExcelReader class.
 * 
 * @author Allan Shaoxiong Jiang
 * 
 */

public class ExcelReader {

	private Workbook workbook;
	private Sheet sheet;

	private final int STRING_TYPE = Cell.CELL_TYPE_STRING;
	private final int NUMERIC_TYPE = Cell.CELL_TYPE_NUMERIC;
	private final int BOOLEAN_TYPE = Cell.CELL_TYPE_BOOLEAN;

	/**
	 * Constructor of a ExcelReader Object. If sheetName is null, use the first
	 * sheet as default.
	 * 
	 * @param path
	 * @param sheetName
	 * @throws IOException
	 * @throws BiffException
	 */
	public ExcelReader(String path, String sheetName) throws IOException {

		workbook = this.getWorkbook(path);
		sheet = this.getSheet(sheetName);

	}

	/**
	 * Constructor of a ExcelReader object. use the first sheet in the workbook
	 * as default.
	 * 
	 * @param path
	 * @throws IOException
	 */
	public ExcelReader(String path) throws IOException {

		workbook = this.getWorkbook(path);
		sheet = this.getSheet(null);

	}

	/**
	 * @param path
	 *            , is
	 * @throws IOException
	 */
	public ExcelReader(String path, InputStream is) throws IOException {

		// Check weather this is a ".xls" or ".xlsx" File
		if (path.contains(".xlsx") == true) {
			workbook = new XSSFWorkbook(is);
		} else {
			workbook = new HSSFWorkbook(is);
		}
		sheet = this.getSheet(null);
		is.close();

	}

	/**
	 * 
	 * @param connection
	 * @throws IOException
	 */
	public ExcelReader(SMBConnection connection) throws IOException {
		InputStream is = connection.getInputStream();
		String path = connection.getFileURL();

		// Check weather this is a ".xls" or ".xlsx" File
		if (path.contains(".xlsx") == true) {
			workbook = new XSSFWorkbook(is);
		} else {
			workbook = new HSSFWorkbook(is);
		}
		sheet = this.getSheet(null);
		is.close();
	}

	/**
	 * Change to the sheet of sheetName.
	 * 
	 * @param sheetName
	 */
	public void setSheet(String sheetName) {

		this.sheet = this.getSheet(sheetName);

	}

	/**
	 * Returns workbook object of current ExcelReader instance.
	 * 
	 * @param uri
	 * @throws IOException
	 *             @ return
	 */
	public Workbook getWorkbook(String path) throws IOException {

		Workbook workbook;
		InputStream is = new FileInputStream(path);

		// Check weather this is a ".xls" or ".xlsx" File
		if (path.contains(".xlsx") == true) {
			workbook = new XSSFWorkbook(is);
			return workbook;
		}

		workbook = new HSSFWorkbook(is);
		is.close();
		return workbook;
	}

	/**
	 * Return a sheet in the workbook with specified name. return null if it is
	 * not found. If sheet's name is known, pass it in as name. If name is null,
	 * return first sheet as default.
	 * 
	 * @param name
	 * @return Sheet
	 */
	public Sheet getSheet(String name) {

		// needed to be fix by returns the first sheet NOT the SHEET1 !
		Sheet sheet;

		if (name == null) {
			sheet = this.workbook.getSheetAt(0);
		}

		else {
			sheet = this.workbook.getSheet(name);
		}
		return sheet;
	}

	/**
	 * Return a hashMap containing data of each row>1
	 * 
	 * @param rowNum
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public HashMap<String, String> getData(int rowNum)
			throws UnsupportedEncodingException {

		int rowIndex = rowNum;
		Row row;

		HashMap<String, String> dataMap = new HashMap<String, String>();

		// do nothing if row number less than label
		if (rowIndex <= 0) {
			return dataMap;
		}

		row = this.sheet.getRow(rowIndex);
		if (row.getPhysicalNumberOfCells() == 0) {
			return null;
		}

		for (int cellIndex = 0; cellIndex < this.getLabels().size(); cellIndex++) {

			Cell currCell = row.getCell(cellIndex);
			if (currCell == null) {
				continue;
			}
			dataMap.put(getLabel(currCell), getValue(currCell));

		}

		return dataMap;
	}

	/**
	 * Return a list of all labels(with UTF-8 Encoding).
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<String> getLabels() throws UnsupportedEncodingException {
		int index = 0;
		String value;
		List<String> labelList = new LinkedList<String>();

		Row row = this.sheet.getRow(0);
		int length = row.getPhysicalNumberOfCells();
		
		while (index < length && row.getCell(index) != null &&
		    row.getCell(index).getStringCellValue() != null &&
		    row.getCell(index).getStringCellValue() != "") {
			value = toUTF_8(row.getCell(index).getStringCellValue());
			labelList.add(value);
			index++;
		}

		return labelList;
	}

	/**
	 * Return the Label of a given cell.
	 * 
	 * @param cell
	 * @return
	 */
	private String getLabel(Cell cell) {
		int col = cell.getColumnIndex();
		Row row = this.sheet.getRow(0);
		return row.getCell(col).getStringCellValue();
	}

	/**
	 * Return a value of given cell.
	 * 
	 * @param cell
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getValue(Cell cell) throws UnsupportedEncodingException {

		/*
		 * We always want a string sooooooo get the type and go from there
		 */
		int cellType = cell.getCellType();
		String value;

		switch (cellType) {
		case STRING_TYPE:
			value = cell.getStringCellValue();
			break;
		case NUMERIC_TYPE:
			value = String.valueOf(cell.getNumericCellValue());
			break;
		case BOOLEAN_TYPE:
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		default:
			value = "";
		}

		value = toUTF_8(value);
		return value;
	}

	public Iterator<HashMap<String, String>> iterator() {
		return new SheetIterator(this);
	}

	/**
	 * Encoding the String to UTF-8.
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String toUTF_8(String str) throws UnsupportedEncodingException {

		String Encoding = "UTF-8";
		byte[] byteArr = str.getBytes(Encoding);
		String encoded = new String(byteArr);
		return encoded;
	}
	
	/**
	 * Test that the sheet was parsed 
	 * and the local structures properly populated.
	 * 
	 * @return whether the object is OK or not
	 */
	public boolean isOK() {
		if( workbook != null && sheet != null ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the number of the last row in the sheet.
	 * 
	 * @return Number of the last row
	 */
	public int getLastRowNum() {
		return sheet.getLastRowNum();
	}
}
