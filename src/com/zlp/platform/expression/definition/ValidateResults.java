package com.zlp.platform.expression.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

//表达式验证结果
public class ValidateResults { 
	public boolean getSucceed(){
		return this.errors == null || this.errors.size() == 0;
	} 
	
	public void init(List<Parameter> parameters, HashMap<String, ValidateResult> validateResults) throws Exception{
		this.init(parameters, validateResults, true);
	} 
	
	public void init(List<Parameter> parameters, HashMap<String, ValidateResult> validateResults, boolean checkCyclicRef) throws Exception{
		
		List<String> allErrors = new ArrayList<String>();
		for(String paramName : validateResults.keySet()){
			ValidateResult result = validateResults.get(paramName);
			if(!result.getSucceed()){
				List<String> errors = result.getErrors();
				for(String error : errors){
					allErrors.add(paramName +": " + error);
				}
			}
		}
		
		if(allErrors.size() != 0){
			this.setErrors(allErrors);
		}
		else{
			if(checkCyclicRef){
				HashMap<String, String> jsCodes = new HashMap<String, String>();
				List<String> sortedParameters = new ArrayList<String>();
				for(Parameter param : parameters){
					String paramName = param.getName();
					if(!validateResults.containsKey(paramName)){
						sortedParameters.add(paramName);
					}
				}
				HashMap<String, List<String>> paramToRefParams = new HashMap<String, List<String>>();
				for(String paramName : validateResults.keySet()){
					ValidateResult result = validateResults.get(paramName);
					jsCodes.put(paramName, result.getError());
					List<String> paramList = result.getUsedParameters();
					if(paramList.size() == 0){
						sortedParameters.add(paramName);
					}
					else{
						List<String> refParamList = new ArrayList<String>();
						refParamList.addAll(result.getUsedParameters());
						paramToRefParams.put(paramName, refParamList);
					}
				}
			
				List<String> checkNameList = new ArrayList<String>();
				checkNameList.addAll(sortedParameters);
				while(paramToRefParams.size() > 0){
					if(checkNameList.size() == 0){
						throw new Exception("出现了循环引用");
					}
					else{
						String checkName = checkNameList.get(0);
						List<String> tempNameList = new ArrayList<String>();
						for(int i = 1; i < checkNameList.size(); i++){
							tempNameList.add(checkNameList.get(i));
						}
						checkNameList = tempNameList;
						
						List<String> needRemoveParamList = new ArrayList<String>();
						for(String paramName : paramToRefParams.keySet()){
							List<String> refParamList = paramToRefParams.get(paramName);
							refParamList.remove(checkName);
							if(refParamList.size() == 0){
								needRemoveParamList.add(paramName);
							}
						}
						for(String paramName : needRemoveParamList){
							paramToRefParams.remove(paramName);
							sortedParameters.add(paramName);
							checkNameList.add(paramName);
						}				
					}
				}
				this.setSortedParameters(sortedParameters);
			}
		}
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
	
	private List<String> sortedParameters ; 
	public List<String> getSortedParameters(){
		return this.sortedParameters;
	}
	public void setSortedParameters(List<String> sortedParameters){
		this.sortedParameters = sortedParameters;
	}   
	
}
