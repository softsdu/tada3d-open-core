package com.zlp.platform.dataManagement.importExportDefinition;

//输入输出excel文件列定义
public class ExcelColumn implements FileColumn {

	//对应的excel列
	private String excelColumnName = null;
	public String getExcelColumnName(){
		return this.excelColumnName;
	}
	public void setExcelColumnName(String excelColumnName){
		this.excelColumnName = excelColumnName;
	}
	
	//数据项名称
	private String itemName = null;
	public String getItemName(){
		return this.itemName;
	}
	public void setItemName(String itemName){
		this.itemName = itemName;
	}
	
	//数据类型
	private DataType dataType = null;
	public DataType getDataType(){
		return this.dataType;
	}
	public void setDataType(DataType dataType){
		this.dataType = dataType;
	}
	
	//格式
	private String formatPattern = null;
	public String getFormatPattern(){
		return this.formatPattern;
	}
	public void setFormatPattern(String formatPattern){
		this.formatPattern = formatPattern;
	}
		
}
