package com.zlp.platform.model.sysmodel;

public class Tree {
	private String id;		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	
	
	private String name; 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	//增加标题 added by ls 20190618
	private String title; 
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	private String labelField;
	public String getLabelField() {
		return labelField;
	}
	public void setLabelField(String labelField) {
		this.labelField = labelField;
	}

	private String parentPointerField;
	public String getParentPointerField() {
		return parentPointerField;
	}
	public void setParentPointerField(String parentPointerField) {
		this.parentPointerField = parentPointerField;
	}

	private String viewName;
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	private String isLeafField;
	public String getIsLeafField() {
		return isLeafField;
	}
	public void setIsLeafField(String isLeafField) {
		this.isLeafField = isLeafField;
	}

	private String sortField;
	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	} 	  
}
