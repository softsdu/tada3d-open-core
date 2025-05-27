package com.zlp.platform.grab;

import java.math.BigDecimal;
import java.util.Date;

public class Grab_StepInstance {
	private String id;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	private String parameters;
	public String getParameters(){
		return this.parameters;
	}
	public void setParameters(String parameters){
		this.parameters = parameters;
	}
	
	private String taskInstanceId;
	public String getTaskInstanceId(){
		return this.taskInstanceId;
	}
	public void setTaskInstanceId(String taskInstanceId){
		this.taskInstanceId = taskInstanceId;
	}
	
	private Date startTime;
	public Date getStartTime()
	{
		return this.startTime;
	}
	public void setStartTime(Date startTime){
		this.startTime = startTime;
	}

	private TaskStatusType statusType;
	public TaskStatusType getStatusType(){
		return this.statusType;
	}
	public void setStatusType(TaskStatusType statusType){
		this.statusType = statusType;
	}
	
	private Date endTime;
	public Date getEndTime()
	{
		return this.endTime;
	}
	public void setEndTime(Date endTime){
		this.endTime = endTime;
	}
	
	private String message;
	public String getMessage(){
		return this.message;
	}
	public void setMessage(String message){
		this.message = message;
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
	
	private String outputDir;
	public String getOutputDir(){
		return this.outputDir;
	}
	public void setOutputDir(String outputDir){
		this.outputDir = outputDir;
	}
	
	private String listFilePath;
	public String getListFilePath(){
		return this.listFilePath;
	}
	public void setListFilePath(String listFilePath){
		this.listFilePath = listFilePath;
	}
	
	private String inputDir;
	public String getInputDir(){
		return this.inputDir;
	}
	public void setInputDir(String inputDir){
		this.inputDir = inputDir;
	}
	
	private String middleDir;
	public String getMiddleDir(){
		return this.middleDir;
	}
	public void setMiddleDir(String middleDir){
		this.middleDir = middleDir;
	}
}
