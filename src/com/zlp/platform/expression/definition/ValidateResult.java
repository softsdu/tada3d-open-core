package com.zlp.platform.expression.definition;

import java.util.List;

import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

//表达式验证结果
public class ValidateResult { 
	public boolean getSucceed(){
		return this.errors.size() == 0;
	} 
	
	private List<String> errors ;
	public String getError(){
		return ValueConverter.arrayToString(this.errors, ". ");
	}
	public List<String> getErrors(){
		return this.errors;
	}
	public void setErrors(List<String> errors){
		this.errors = errors;
	}
	
	private ExpTreePart partTree ;
	public ExpTreePart getPartTree(){
		return this.partTree;
	}
	public void setPartTree(ExpTreePart partTree){
		this.partTree = partTree;
	}
	
	private String name ;
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	private String valueType ;
	public String getValueType(){
		return this.valueType;
	}
	public void setValueType(String valueType){
		this.valueType = valueType;
	}
	
	private List<String> usedParameters;
	public List<String> getUsedParameters(){
		return this.usedParameters;
	}
	public void setUsedParameters(List<String> usedParameters){
		this.usedParameters = usedParameters;
	}
	
	public String getPs(){
		StringBuilder ss = new StringBuilder("|");
		for(int i = 0; i < usedParameters.size(); i++){
			ss.append(usedParameters.get(i) + "|");
		}
		return ss.toString();
	}

	//运行位置
	private RunAt runAt;
	public RunAt getRunAt(){
		return this.runAt;
	}
	public void setRunAt(RunAt runAt){
		this.runAt = runAt;
	}
}
