package com.zlp.platform.dao.db;

//字段
public class Field {
	
	//字段名
	private String name;	
	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return this.name;
	}
	
	//资源类型
	private ValueType type; 
	public void setType(ValueType type){
		this.type=type;
	}
	public ValueType getValueType(){
		return this.type;
	}
}
