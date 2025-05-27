package com.zlp.platform.dataManagement.importExportDefinition;

//字段
public class Field {
	//数据项名称
	private String itemName = null;
	public String getItemName(){
		return this.itemName;
	}
	public void setItemName(String itemName){
		this.itemName = itemName;
	}
	
	//数据库字段名称
	private String dbFieldName = null;
	public String getDbFieldName(){
		return this.dbFieldName;
	}
	public void setDbFieldName(String dbFieldName){
		this.dbFieldName = dbFieldName;
	}
	
	//显示名称
	private String showName = null;
	public String getShowName(){
		return this.showName;
	}
	public void setShowName(String showName){
		this.showName = showName;
	}
	
	//是否唯一（指定多个时，为组合）
	private boolean isUnique = false;
	public boolean getIsUnique(){
		return this.isUnique;
	}
	public void setIsUnique(boolean isUnique){
		this.isUnique = isUnique;
	}
	
	//字段长度
	private int width = 0;
	public int getWidth(){
		return this.width;
	}
	public void setWidth(int width){
		this.width = width;
	}
	
	//显示区域宽度
	private int displayWidth = 0;
	public int getDisplayWidth(){
		return this.displayWidth;
	}
	public void setDisplayWidth(int displayWidth){
		this.displayWidth = displayWidth;
	}
	
	//字段类型String、Decimal、Date、Time
	private DataType fieldType = null;
	public DataType getFieldType(){
		return this.fieldType;
	}
	public void setFieldType(DataType fieldType){
		this.fieldType = fieldType;
	}
	
	//格式
	private String formatPattern = null;
	public String getFormatPattern(){
		return this.formatPattern;
	}
	public void setFormatPattern(String formatPattern){
		this.formatPattern = formatPattern;
	}
	
	//是否允许作为查询条件
	private boolean canQuery = false;
	public boolean getCanQuery(){
		return this.canQuery;
	}
	public void setCanQuery(boolean canQuery){
		this.canQuery = canQuery;
	}
	
	//是否允许选多值
	private boolean isMultiValue = false;
	public boolean getIsMultiValue(){
		return this.isMultiValue;
	}
	public void setIsMultiValue(boolean isMultiValue){
		this.isMultiValue = isMultiValue;
	}

	//多值分隔符
	private String multiSplitter = ",";
	public String getMultiSplitter(){
		return this.multiSplitter;
	}
	public void setMultiSplitter(String multiSplitter){
		this.multiSplitter = multiSplitter;
	}

	//下拉值
	private ListOptions listOptions = null;
	public ListOptions getListOptions(){
		return this.listOptions;
	}
	public void setListOptions(ListOptions listOptions){
		this.listOptions = listOptions;
	}
	
	

}
