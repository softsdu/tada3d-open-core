package com.zlp.platform.grab;

import java.math.BigDecimal;

public class Grab_StepDef {
	private String id;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	
	private String taskDefId;
	public String getTaskDefId(){
		return this.taskDefId;
	}
	public void setTaskDefId(String taskDefId){
		this.taskDefId = taskDefId;
	}	
	
	private String parametersExp;
	public String getParametersExp(){
		return this.parametersExp;
	}
	public void setParametersExp(String parametersExp){
		this.parametersExp = parametersExp;
	}
	
	private BigDecimal runIndex;
	public BigDecimal getRunIndex(){
		return this.runIndex;
	}
	public void setRunIndex(BigDecimal runIndex){
		this.runIndex = runIndex;
	}
	
	private String projectName;
	public String getProjectName(){
		return this.projectName;
	}
	public void setProjectName(String projectName){
		this.projectName = projectName;
	}
	
	private String groupName;
	public String getGroupName(){
		return this.groupName;
	}
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	
	private String outputDirExp;
	public String getOutputDirExp(){
		return this.outputDirExp;
	}
	public void setOutputDirExp(String outputDirExp){
		this.outputDirExp = outputDirExp;
	}
	
	private String listFilePathExp;
	public String getListFilePathExp(){
		return this.listFilePathExp;
	}
	public void setListFilePathExp(String listFilePathExp){
		this.listFilePathExp = listFilePathExp;
	}
	
	private String inputDirExp;
	public String getInputDirExp(){
		return this.inputDirExp;
	}
	public void setInputDirExp(String inputDirExp){
		this.inputDirExp = inputDirExp;
	}
	
	private String middleDirExp;
	public String getMiddleDirExp(){
		return this.middleDirExp;
	}
	public void setMiddleDirExp(String middleDirExp){
		this.middleDirExp = middleDirExp;
	}
}
