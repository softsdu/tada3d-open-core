package com.zlp.platform.dataManagement.importExportDefinition;

import java.util.List;

public class ListOptions {

	//多值分隔符
	private String listSplitter = ",";
	public String getListSplitter(){
		return this.listSplitter;
	}
	public void setListSplitter(String listSplitter){
		this.listSplitter = listSplitter;
	}	
	
	//多值
	private boolean isMultiValue = false;
	public boolean getIsMultiValue(){
		return this.isMultiValue;
	}
	public void setIsMultiValue(boolean isMultiValue){
		this.isMultiValue = isMultiValue;
	}	
	
	//导入文件解析
	private ListType listType = ListType.Static;
	public ListType getListType(){
		return this.listType;
	}
	public void setListType(ListType listType){
		this.listType = listType;
	}	
	
	//静态的下拉值
	private List<Option> options = null;
	public List<Option> getOptions(){
		return this.options;
	}
	public void setOptions(List<Option> options){
		this.options = options;
	}
}
