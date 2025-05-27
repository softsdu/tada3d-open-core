package com.zlp.platform.grab;

import java.util.List;

public class Grab_TaskDef {
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

	private String dataName;
	public String getDataName(){
		return this.dataName;
	}
	public void setDataName(String dataName){
		this.dataName = dataName;
	}
	
	private String description;
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	private List<Grab_StepDef> allStepDefs;
	public List<Grab_StepDef> getAllStepDefs(){
		return this.allStepDefs;
	}
	public void setAllStepDefs(List<Grab_StepDef> allStepDefs){
		this.allStepDefs = allStepDefs;
	}
}
