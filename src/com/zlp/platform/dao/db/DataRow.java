package com.zlp.platform.dao.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;

import com.zlp.platform.common.ValueConverter;

//数据行
public class DataRow {
	//字段名和值
	private HashMap<String,Object> fieldValues=new HashMap<String,Object>();	
	public HashMap<String,Object> getFieldValues(){
		return this.fieldValues;
	}
	
	//设置字段值
	public void setValue(String name,Object value){ 
		this.fieldValues.put(name, value);
	}
	
	//获取字段值
	public Object getValue(String name) {
		return this.fieldValues.get(name);
	}

	//获取判断字段值是否为空
	public boolean isNull(String name) {
		return this.fieldValues.get(name) == null;
	}

	//获取判断存在此字段
	public boolean isExist(String name) {
		return this.fieldValues.containsKey(name);
	}

	//获取Integer字段值
	public Integer String(String name){
		Object value = this.getValue(name);
		return value == null ? null : ((BigDecimal)value).intValue();
	}

	//获取BigDecimal字段值
	public BigDecimal getBigDecimalValue(String name){
		Object value = this.getValue(name);
		return (BigDecimal)value;
	}
	
	//获取String字段值
	public String getStringValue(String name){
		Object value = this.getValue(name);
		return (String)value;
	}
	
	//将字段值转化为字符串
	public String getStringValue(String name, ValueType valueType) throws Exception{
		try {
			Object value = this.getValue(name);
			return valueType==ValueType.Boolean ? (String)value : ValueConverter.convertToString(value, valueType);
		}
		catch(Exception ex){
			throw ex;
		}
	}
	
	//获取日期时间字段值
	public Date getDateTimeValue(String name){
		Object value = this.getValue(name);
		return (Date)value;
	}
	
	//获取日期时间字段值
	public Boolean getBooleanValue(String name){
		Object value = this.getValue(name);
		if( "Y".equals(value) || "y".equals(value)) {
			return true;
		}
		else if("N".equals(value) || "n".equals(value)){
			return false;
		}
		else{
			return null;
		} 
	}
	
	//获取Integer字段值
	public Integer getIntegerValue(String name){
		Object value = this.getValue(name);
		return value == null ? null : ((BigDecimal)value).intValue();
	} 

	//获取Integer字段值 added by ls 20190618
	public BigInteger getBigIntegerValue(String name){
		Object value = this.getValue(name);
		return value == null ? null : (BigInteger)value;
	}
}
