package com.zlp.platform.dataManagement.commonFunction;

import java.io.FileInputStream;
import java.io.InputStream; 
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List; 

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dataManagement.importExportDefinition.DataType;
import com.zlp.platform.dataManagement.importExportDefinition.ExcelColumn;
import com.zlp.platform.dataManagement.importExportDefinition.ExcelParser;
import com.zlp.platform.dataManagement.importExportDefinition.Field;
import com.zlp.platform.dataManagement.importExportDefinition.ImportExportDefinition;
import com.zlp.platform.model.sysmodel.Data; 

public class ExcelReader {	

	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	} 	
	
	private XSSFWorkbook getWorkbook(String filePath) throws Exception{		
		InputStream fs= null;
		try {
			fs = new FileInputStream(filePath); 
			XSSFWorkbook wb = new XSSFWorkbook(fs);
			return wb;
		}
		catch(Exception ex){
			throw new Exception("读取Excel文件出错。 File Path = " + filePath);
		}
	}

	private XSSFSheet getSheet(XSSFWorkbook wb, String sheetName){
		XSSFSheet sheet = null;
		if(sheetName == null || sheetName == ""){
			sheet = wb.getSheetAt(0);
		}
		else{
			sheet = wb.getSheet(sheetName);
		}
		return sheet;
	}
	private List<Row> getAllRows(XSSFSheet sheet){
		int rowCount = sheet.getLastRowNum();
		List<Row> allRows = new ArrayList<Row>();
        for( int i=0; i<=rowCount; i++){
        	Row row = sheet.getRow(i);
        	allRows.add(row);
        }
        return allRows;
	}
	
	private HashMap<String, Integer> getDefaultColName2Index(){
		String[] lts = new String[]{"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		HashMap<String, Integer> excelColName2Index = new HashMap<String, Integer>();
		int colIndex = 0;
		for(int i=0;i<lts.length;i++){
			for(int j=1;j<lts.length;j++){
				excelColName2Index.put(lts[i] + lts[j], colIndex);
				colIndex++;
			}	
		}
		return excelColName2Index;
	}
	
	private HashMap<String, Integer> getColName2Index(Row headerRow) throws Exception{
		HashMap<String, Integer> excelColName2Index = new HashMap<String, Integer>(); 
		for(int i=0; i<headerRow.getLastCellNum(); i++){
			Cell cell = headerRow.getCell(i);
			if(cell != null){
				String cellStr = cell.getStringCellValue();
				if(cellStr.length() != 0){
					cellStr = cellStr.trim();
					if(excelColName2Index.containsKey(cellStr)){
						throw new Exception("Excel中不在两列（或更多）的名称为'" + cellStr + "'.");
					}
					else{
						excelColName2Index.put(cellStr, i);
					}
				}
			}
		}
		return excelColName2Index;
	}
	
	public List<HashMap<String, Object>> getSourceData(String filePath, String sheetName, ExcelParser excelParser) throws Exception{
		XSSFWorkbook wb = this.getWorkbook(filePath);
		XSSFSheet sheet = this.getSheet(wb, sheetName);
		if(sheet == null){
			throw new Exception("无法获取名为'" + sheetName + "'的Sheet页.");
		}
		else{
			List<Row> allRows = this.getAllRows(sheet); 
	
			HashMap<Integer, ExcelColumn> index2ExcelColumns = new HashMap<Integer, ExcelColumn>();  
			List<ExcelColumn> allColumns = excelParser.getColumns();
			
			boolean hasHeaderRow = excelParser.getHasHeaderRow();
			HashMap<String, Integer> excelColName2Index = null;
			if(hasHeaderRow){
				if(allRows.size() > 0){
					Row headerRow = allRows.get(0);
					excelColName2Index =this.getColName2Index(headerRow); 
				}
				else{
					throw new Exception("Excel的Sheet页为空，所以无法获取表头行.");
				}
			}
			else{
				excelColName2Index = this.getDefaultColName2Index();
			}
			for(int i=0;i<allColumns.size();i++){
				ExcelColumn ec = allColumns.get(i);
				String excelColName = ec.getExcelColumnName();
				if(excelColName2Index.containsKey(excelColName))	{
					int index = excelColName2Index.get(excelColName);
					index2ExcelColumns.put(index, ec);
				}
				else{
					throw new Exception("Excel中不能存在列‘" + excelColName + "'.");
				}					
			}			
			
			List<HashMap<String, Object>> allMemoRows = new ArrayList<HashMap<String, Object>>();
	
			int startRowIndex = hasHeaderRow ? 1 : 0;
			
			for(int i=startRowIndex; i<allRows.size(); i++){
				Row row = allRows.get(i); 
				int cellCount = row.getLastCellNum();
				HashMap<String, Object> f2vs  = new HashMap<String, Object>();
				for(int j=0;j<cellCount;j++){
					if(index2ExcelColumns.containsKey(j)){
						Cell cell = row.getCell(j);
						if(cell != null){
						    String sourceValue = cell.toString(); 
						    ExcelColumn ec = index2ExcelColumns.get(j);
						    DataType dType = ec.getDataType();
						    Object destValue = null;
						    switch(dType){
							    case String:
							    	destValue = sourceValue;
							    	break;
							    case Decimal:
							    	destValue = ValueConverter.convertToDecimal(sourceValue, ec.getFormatPattern());
							    	break;
							    case Date:
							    	destValue = ValueConverter.convertToDate(sourceValue, ec.getFormatPattern());
							    	break;
							    case Time:
							    	destValue = ValueConverter.convertToTime(sourceValue, ec.getFormatPattern());
							    	break;
							    case Boolean:
							    	destValue = ValueConverter.convertToBoolean(sourceValue, "是","否");
							    	break; 
						    }
						    f2vs.put(ec.getItemName() + "_source", sourceValue);		
						    f2vs.put(ec.getItemName(), destValue);						    	
						}
					}
				}
				allMemoRows.add(f2vs);
			}
			return allMemoRows;
		}
	}
	
	public void insertToDB(Session dbSession, ImportExportDefinition importDef, Data dataModel, String parentId, String parentIdFieldName, List<HashMap<String, Object>> allMemoRows) throws Exception{
		 
		List<Field> allFields = importDef.getFieldList();
		HashMap<String, String> itemName2DbName = new HashMap<String, String>();
		for(int i=0;i<allFields.size();i++){
			Field f = allFields.get(i);
			itemName2DbName.put(f.getItemName(), f.getDbFieldName());
		}
		
		int rowCount = allMemoRows.size();
		List<HashMap<String, Object>> allRows = new ArrayList<HashMap<String, Object>>();
		for(int i=0;i<rowCount;i++){
			HashMap<String, Object> insertRow = new HashMap<String, Object>();
			HashMap<String, Object> memoRow = allMemoRows.get(i);
			for(String itemName : itemName2DbName.keySet()){
				String dbFieldName = itemName2DbName.get(itemName);
				Object objValue = memoRow.get(itemName);
				insertRow.put(dbFieldName, objValue);
			}
			insertRow.put(parentIdFieldName, parentId);
			allRows.add(insertRow);
		}
		IDBParserAccess dbParserAccess =  this.getDBParserAccess(); 
		dbParserAccess.deleteByData(dbSession, dataModel, parentIdFieldName, "=", parentId);	

		List<HashMap<String, Object>> batchRows = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<rowCount; i++){ 
			//每1000个为一个事务
			if( i % 1000 == 0){ 
				if(i != 0){
					batchInsertToDB(dbSession, dbParserAccess, dataModel, batchRows);
				}
				batchRows = new ArrayList<HashMap<String, Object>>();
			}
			batchRows.add(allRows.get(i));  
		}
		if(batchRows.size()>0){
			batchInsertToDB(dbSession, dbParserAccess, dataModel, batchRows);
		}
	}
	
	public void insertToDB(Session dbSession, ImportExportDefinition importDef, Data dataModel, List<HashMap<String, Object>> allMemoRows) throws Exception{
		 
		List<Field> allFields = importDef.getFieldList();
		HashMap<String, String> itemName2DbName = new HashMap<String, String>();
		for(int i=0;i<allFields.size();i++){
			Field f = allFields.get(i);
			itemName2DbName.put(f.getItemName(), f.getDbFieldName());
		}
		
		int rowCount = allMemoRows.size();
		List<HashMap<String, Object>> allRows = new ArrayList<HashMap<String, Object>>();
		for(int i=0;i<rowCount;i++){
			HashMap<String, Object> insertRow = new HashMap<String, Object>();
			HashMap<String, Object> memoRow = allMemoRows.get(i);
			for(String itemName : itemName2DbName.keySet()){
				String dbFieldName = itemName2DbName.get(itemName);
				Object objValue = memoRow.get(itemName);
				insertRow.put(dbFieldName, objValue);
			} 
			allRows.add(insertRow);
		}
		IDBParserAccess dbParserAccess =  this.getDBParserAccess();  

		List<HashMap<String, Object>> batchRows = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<rowCount; i++){ 
			//每1000个为一个事务
			if( i % 1000 == 0){ 
				if(i != 0){
					batchInsertToDB(dbSession, dbParserAccess, dataModel, batchRows);
				}
				batchRows = new ArrayList<HashMap<String, Object>>();
			}
			batchRows.add(allRows.get(i));  
		}
		if(batchRows.size()>0){
			batchInsertToDB(dbSession, dbParserAccess, dataModel, batchRows);
		}
	}
	
	private void batchInsertToDB(Session dbSession, IDBParserAccess dbParserAccess, Data dataModel, List<HashMap<String, Object>> batchRows) throws Exception{
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			dbParserAccess.insertByData(dbSession, dataModel, batchRows);
			tx.commit();
		}
		catch(RuntimeException ex){
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}
	}
		 
}
