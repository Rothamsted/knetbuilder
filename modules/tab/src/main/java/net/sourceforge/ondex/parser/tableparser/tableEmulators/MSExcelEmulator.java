package net.sourceforge.ondex.parser.tableparser.tableEmulators;

import java.io.File;

import net.sourceforge.ondex.tools.tab.importer.DataReader;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MSExcelEmulator extends DataReader{
	private Sheet sheet;
	private Workbook workbook;
	boolean open = false;
	private int row = 1;
	private int lastLine = Integer.MAX_VALUE;
	
	public MSExcelEmulator(String fileName, Object sheetId){
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
			open = true;
			if(sheetId instanceof Integer){
				sheet = workbook.getSheet((Integer)sheetId);		
			}
			else if(sheetId instanceof String){
				sheet = workbook.getSheet((String)sheetId);
			}
			else{
				throw new Exception("Invalid sheet id - must be Integer or String.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getData(int column, int row) {
		try{
			return sheet.getCell(column, row).getContents();	
		}
		catch(Exception e){
			return null;
		}
	}

	@Override
	public void close() {
		sheet = null;
		workbook.close();
		
	}

	@Override
	public boolean hasNext() {
		return (sheet.getRows()< row+1 && row+1 <= lastLine);
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	protected String[] readLine() {
		if(!hasNext())
			return new String[0];
		Cell[] temp = sheet.getRow(row);
		String[] line = new String[temp.length];
		for(int i = 0; i < temp.length; i++){
			line[i] = temp[i].getContents();	
		}
		return line;
	}

	@Override
	public void reset() {
		row = 1;	
	}

	@Override
	public void setLine(int lineNumber) {
		if(lastLine < lineNumber)
			lineNumber = lastLine;
		if(sheet.getRows()< lineNumber){
			row = lineNumber;	
		}
		row = sheet.getRows();
	}

	@Override
	public void setLastLine(int lineNumber) {
		lastLine = lineNumber;
	}
}