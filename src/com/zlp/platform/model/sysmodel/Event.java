package com.zlp.platform.model.sysmodel;

import java.util.List;

//事件声明
public class Event {
	private String id;
	private String name;
	private String description;
	private String resultValueType;
	private String category;
	private List<EventParameter> allParameters;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getResultValueType() {
		return resultValueType;
	}
	public void setResultValueType(String resultValueType) {
		this.resultValueType = resultValueType;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public List<EventParameter> getAllParameters() {
		return allParameters;
	}
	public void setAllParameters(List<EventParameter> allParameters) {
		this.allParameters = allParameters;
	}
	
	
}
