package com.zlp.platform.dataManagement.importExportDefinition;

import java.util.List;

public class ExcelParser implements FileParser {
	//第一行为标题
	private boolean hasHeaderRow = true;
	public boolean getHasHeaderRow(){
		return this.hasHeaderRow;
	}
	public void setHasHeaderRow(boolean hasHeaderRow){
		 this.hasHeaderRow = hasHeaderRow;
	}
	
	//所有列
	private List<ExcelColumn> columns= null;
	public List<ExcelColumn> getColumns(){
		return this.columns;
	}
	public void setColumns(List<ExcelColumn> columns){
		this.columns = columns;
	}
}
