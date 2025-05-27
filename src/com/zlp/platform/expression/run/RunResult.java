package com.zlp.platform.expression.run;

import java.util.List;

import com.zlp.platform.dao.db.ValueType;

//表达式验证结果
public class RunResult { 
	//返回值
	private boolean succeed;
	public boolean getSucceed(){
		return this.succeed;
	}
	public void setSucceed(boolean succeed){
		this.succeed = succeed;
	}
	
	//返回值
	private Object value;
	public Object getValue(){
		return this.value;
	}
	public void setValue(Object value){
		this.value = value;
	}
	
	//错误信息
	private String error;
	public String getError(){
		return this.error;
	}
	public void setError(String errorvalue){
		this.error = errorvalue;
	}
}
