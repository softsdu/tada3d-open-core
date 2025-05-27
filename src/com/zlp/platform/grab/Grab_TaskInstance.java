package com.zlp.platform.grab;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class Grab_TaskInstance {
	private String id;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	
	private String name;
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	private Date createTime;
	public Date getCreateTime()
	{
		return this.createTime;
	}
	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	
	private Date insertTime;
	public Date getInsertTime()
	{
		return this.insertTime;
	}
	public void setInsertTime(Date insertTime){
		this.insertTime = insertTime;
	}

	private TaskStatusType statusType;
	private TaskStatusType getStatusType(){
		return this.statusType;
	}
	public void setStatusType(TaskStatusType statusType){
		this.statusType = statusType;
	}
	
	private String description;
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	private BigDecimal level;
	public BigDecimal getLevel(){
		return this.level;
	}
	public void setLevel(BigDecimal level){
		this.level = level;
	}
	
	private String grabServiceId;
	public String getGrabServiceId(){
		return this.grabServiceId;
	}
	public void setGrabServiceId(String grabServiceId){
		this.grabServiceId = grabServiceId;
	} 
	
	private String invokeId;
	public String getInvokeId(){
		return this.invokeId;
	}
	public void setInvokeId(String invokeId){
		this.invokeId = invokeId;
	}
	
	private String taskDefId;
	public String getTaskDefId(){
		return this.taskDefId;
	}
	public void setTaskDefId(String taskDefId){
		this.taskDefId = taskDefId;
	}
	
	private List<Grab_StepInstance> allStepInstances;
	public List<Grab_StepInstance> getAllStepInstances(){
		return this.allStepInstances;
	}
	public void setAllStepInstances(List<Grab_StepInstance> allStepInstances){
		this.allStepInstances = allStepInstances;
	}
	
}
