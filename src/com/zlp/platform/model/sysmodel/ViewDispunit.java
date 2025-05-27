package com.zlp.platform.model.sysmodel;

public class ViewDispunit {
	private String id;
	private String name; 
	private String parentId;
	private String label;
	private int colWidth;
	private Boolean colSortable;
	private Boolean colSearch;
	private Boolean colResizable;
	private Boolean editable;
	private Boolean nullable;
	private Boolean colVisible;
	private int colIndex;  
	
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
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getColWidth() {
		return colWidth;
	}
	public void setColWidth(int colWidth) {
		this.colWidth = colWidth;
	}
	public Boolean getColSearch() {
		return colSearch;
	}
	public void setColSearch(Boolean colSearch) {
		this.colSearch = colSearch;
	}
	public Boolean getColResizable() {
		return colResizable;
	}
	public void setColResizable(Boolean colResizable) {
		this.colResizable = colResizable;
	}
	public Boolean getColSortable() {
		return colSortable;
	}
	public void setColSortable(Boolean colSortable) {
		this.colSortable = colSortable;
	}
	public Boolean getEditable() {
		return editable;
	}
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	public Boolean getNullable() {
		return nullable;
	}
	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}
	public Boolean getColVisible() {
		return colVisible;
	}
	public void setColVisible(Boolean colVisible) {
		this.colVisible = colVisible;
	}
	public int getColIndex() {
		return colIndex;
	}
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}  
}
