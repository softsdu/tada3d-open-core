package com.zlp.platform.model.sysmodel;

public class DataFieldMap {
	private String id;
	private String parentId;
	private String sourceField;
	private String destField;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getSourceField() {
		return sourceField;
	}
	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}
	public String getDestField() {
		return destField;
	}
	public void setDestField(String destField) {
		this.destField = destField;
	}
}
