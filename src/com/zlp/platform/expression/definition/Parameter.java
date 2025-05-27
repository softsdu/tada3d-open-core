package com.zlp.platform.expression.definition;

import com.zlp.platform.dao.db.ValueType;

//参数
public class Parameter {

	//参数名称
	private String name = "";
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}

	//描述
	private String description = "";
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	//值类型
	private String valueType;
	public String getValueType(){
		return this.valueType;
	}
	public void setValueType(String valueType){
		this.valueType = valueType;
	}
	
	//是否忽略的标识，格式为"数字"或"!数字"，数字表示参数的序号 added by ls 20230421
	private String ignoreMark = null; 
	public String getIgnoreMark() {
		return ignoreMark;
	}
	public void setIgnoreMark(String ignoreMark) {
		this.ignoreMark = ignoreMark;
	}
	
	//可重复的
	private boolean repeatable;
	public boolean getRepeatable(){
		return this.repeatable;
	}
	public void setRepeatable(boolean repeatable){
		this.repeatable = repeatable;
	}
}
