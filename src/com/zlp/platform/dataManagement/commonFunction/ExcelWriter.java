package com.zlp.platform.dataManagement.commonFunction;
 
import java.io.FileOutputStream; 
import java.util.Date;
import java.util.HashMap;
import java.util.List; 
 
import org.apache.poi.ss.usermodel.CellStyle; 
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;  
import com.zlp.platform.dataManagement.importExportDefinition.DataType;
import com.zlp.platform.dataManagement.importExportDefinition.ExcelColumn;
import com.zlp.platform.dataManagement.importExportDefinition.ExcelParser; 

public class ExcelWriter {	
	private XSSFWorkbook createWorkbook() throws Exception{	 
		try {
			XSSFWorkbook wb=new XSSFWorkbook(); 
			return wb;
		}
		catch(Exception ex){
			throw new Exception("创建Excel文件出错。 ", ex);
		}
	}

	private XSSFSheet createSheet(XSSFWorkbook wb, String sheetName){
		
		//创建sheet页
		XSSFSheet sheet = wb.createSheet();	
		
		//设置sheet名称
		int sheetIndex = wb.getSheetIndex(sheet);  
		wb.setSheetName(sheetIndex, sheetName);  
		
		return sheet;
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
	
	public void saveExcel(String filePath, String sheetName, ExcelParser excelParser, List<HashMap<String, Object>> dataRows) throws Exception{
		XSSFWorkbook wb = this.createWorkbook();
		XSSFSheet sheet = this.createSheet(wb, sheetName); 
 
		List<ExcelColumn> allColumns = excelParser.getColumns();
		
		boolean hasHeaderRow = excelParser.getHasHeaderRow();
		HashMap<String, Integer> excelColName2Index = new HashMap<String, Integer>();
		
		if(hasHeaderRow){ 
			//取得第一行 
			XSSFRow firstRow = sheet.createRow(0);
			for(int i = 0; i < allColumns.size(); i++){
				ExcelColumn ec = allColumns.get(i);
				String excelColName = ec.getExcelColumnName();
				excelColName2Index.put(excelColName, i); 
				XSSFCell colTitleCell = firstRow.createCell(i);
				colTitleCell.setCellValue(excelColName);
				String formatPattern = ec.getFormatPattern();
				if(formatPattern != null && !formatPattern.isEmpty()){
					CellStyle cellStyle = wb.createCellStyle();
					XSSFDataFormat dataFormat = wb.createDataFormat();
					cellStyle.setDataFormat(dataFormat.getFormat(formatPattern)); 
					sheet.setDefaultColumnStyle(i, cellStyle);
				}
			}
		}
		else{ 
			HashMap<String, Integer> defaultExcelColName2Index = this.getDefaultColName2Index(); 
			for(int i = 0; i < allColumns.size(); i++){
				ExcelColumn ec = allColumns.get(i);
				String excelColName = ec.getExcelColumnName();
				if(defaultExcelColName2Index.containsKey(excelColName)){					
					int colIndex = defaultExcelColName2Index.get(excelColName);
					excelColName2Index.put(excelColName, colIndex);   
					String formatPattern = ec.getFormatPattern();
					if(formatPattern != null && !formatPattern.isEmpty()){
						CellStyle cellStyle = wb.createCellStyle();
						XSSFDataFormat dataFormat = wb.createDataFormat();
						cellStyle.setDataFormat(dataFormat.getFormat(formatPattern)); 
						sheet.setDefaultColumnStyle(i, cellStyle);
					}
				}
				else{
					throw new Exception("Can not get column index. colName = " + excelColName + ", hasHeaderRow = false");
				}
			}
		}
		 
		int startRowIndex = hasHeaderRow ? 1 : 0;
		
		for(int i = 0; i < dataRows.size(); i++){
			HashMap<String, Object> dataRow = dataRows.get(i); 
			//取得第一行 
			XSSFRow excelRow = sheet.createRow(startRowIndex + i);
			for(int j = 0; j < allColumns.size(); j++){
				ExcelColumn ec = allColumns.get(j);
				String excelColName = ec.getExcelColumnName();
				int columnIndex = excelColName2Index.get(excelColName);
				DataType cellDataType = ec.getDataType();
				String itemName = ec.getItemName();
				if(dataRow.containsKey(itemName)){
					Object cellValue = dataRow.get(itemName);
					XSSFCell excelCell = excelRow.createCell(columnIndex);
					
					switch(cellDataType){
						case String:
							excelCell.setCellType(XSSFCell.CELL_TYPE_STRING);
							excelCell.setCellValue((String)cellValue);
							break;
						case Decimal:
							excelCell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							excelCell.setCellValue( cellValue == null ? null : (Double.parseDouble(cellValue.toString())));
							break;
						case Date:
							excelCell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							excelCell.setCellValue(cellValue == null ? null : (Date)cellValue);
							break;
						case Time:
							excelCell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							excelCell.setCellValue(cellValue == null ? null : (Date)cellValue);
							break;
						case Boolean: 
							//excelCell.setCellType(XSSFCell.CELL_TYPE_BOOLEAN);
							//excelCell.setCellValue(cellValue == null ? null : (Boolean)cellValue);
							excelCell.setCellType(XSSFCell.CELL_TYPE_STRING);
							excelCell.setCellValue((String)cellValue);
							break;
					}
				}
			} 
		}
		
		FileOutputStream fileoutputstream = null;
		try{
			fileoutputstream = new FileOutputStream(filePath); 
			wb.write(fileoutputstream); 
		}
		catch(Exception ex){
			throw new Exception("将Excel写入到磁盘失败.", ex);
		}
		finally{
			if(fileoutputstream != null){
				fileoutputstream.close();
			}
		}
	} 
}
