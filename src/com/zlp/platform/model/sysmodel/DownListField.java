package com.zlp.platform.model.sysmodel;

import com.zlp.platform.dao.db.ValueType;

public class DownListField {
	private String id;
	private String parentId;
	private String name;
	private ValueType valueType;
	private String displayName;
	private Boolean isShow;
	private int showWidth;
	private Boolean isComma;
	private int decimalNum;
	private int showIndex;
	private String dataPurviewFactor;	
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = Enum.valueOf(ValueType.class, valueType);
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Boolean getIsShow() {
		return isShow;
	}
	public void setIsShow(Boolean isShow) {
		this.isShow = isShow;
	}
	public int getShowWidth() {
		return showWidth;
	}
	public void setShowWidth(int showWidth) {
		this.showWidth = showWidth;
	}
	public Boolean getIsComma() {
		return isComma;
	}
	public void setIsComma(Boolean isComma) {
		this.isComma = isComma;
	}
	public int getDecimalNum() {
		return decimalNum;
	}
	public void setDecimalnum(int decimalNum) {
		this.decimalNum = decimalNum;
	}
	public int getShowIndex() {
		return showIndex;
	}
	public void setShowIndex(int showIndex) {
		this.showIndex = showIndex;
	}
	public String getDataPurviewFactor() {
		return dataPurviewFactor;
	}
	public void setDataPurviewFactor(String dataPurviewFactor) {
		this.dataPurviewFactor = dataPurviewFactor;
	}
}
