package com.zlp.platform.model.sysmodel;

import java.util.List;

public class SheetPart {
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
	
	private String parentId;
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}  
	
	private String labelField;
	public String getLabelField() {
		return labelField;
	}
	public void setLabelField(String labelField) {
		this.labelField = labelField;
	}
	
	private String viewName;
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	
	private String parentPartName;
	public String getParentPartName() {
		return parentPartName;
	}
	public void setParentPartName(String parentPartName) {
		this.parentPartName = parentPartName;
	}
	
	private String parentPointerField; 
	public String getParentPointerField() {
		return parentPointerField;
	}
	public void setParentPointerField(String parentPointerField) {
		this.parentPointerField = parentPointerField;
	} 
	
	//增加标题 added by ls 20190618
	private String title;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	private List<SheetPart> childParts = null;
	public List<SheetPart> getChildParts(){
		return this.childParts;
	}
	public void setChildParts(List<SheetPart> childParts){
		this.childParts = childParts;
	}
}
