package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.List;

import com.zlp.platform.dao.db.ValueType;

public class DataField {
	private String id;
	private String name; 
	private String parentId;
	private String displayName;
	private ValueType valueType;
	private Boolean isSave;
	private String inputHelpType;
	private String inputHelpName;
	private String foreignKeyName;
	private Boolean isComma;
	private int valueLength;
	private int decimalNum;	
	private Boolean isReadonly;	
	private Boolean isSum;	
	private Boolean isMulti;	
	private String dataPurviewFactor;	
	private List<DataFieldMap> dataFieldMap = new ArrayList<DataFieldMap>();
	
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
	public List<DataFieldMap> getDataFieldMap() {
		return dataFieldMap;
	}
	public void addDataFieldMap(DataFieldMap dataFieldMap) {
		this.dataFieldMap.add(dataFieldMap);
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = Enum.valueOf(ValueType.class, valueType);
	}
	public Boolean getIsSave() {
		return isSave;
	}
	public void setIsSave(Boolean isSave) {
		this.isSave = isSave;
	}
	public String getInputHelpType() {
		return inputHelpType;
	}
	public void setInputHelpType(String inputHelpType) {
		this.inputHelpType = inputHelpType;
	}
	public String getInputHelpName() {
		return inputHelpName;
	}
	public void setInputHelpName(String inputHelpName) {
		this.inputHelpName = inputHelpName;
	}
	public String getForeignKeyName() {
		return foreignKeyName;
	}
	public void setForeignKeyName(String foreignKeyName) {
		this.foreignKeyName = foreignKeyName;
	}
	public Boolean getIsComma() {
		return isComma;
	}
	public void setIsComma(Boolean isComma) {
		this.isComma = isComma;
	}
	public int getValueLength() {
		return valueLength;
	}
	public void setValueLength(int valueLength) {
		this.valueLength = valueLength;
	}
	public int getDecimalNum() {
		return decimalNum;
	}
	public void setDecimalNum(int decimalNum) {
		this.decimalNum = decimalNum;
	}
	public Boolean getIsReadonly() {
		return isReadonly;
	}
	public void setIsReadonly(Boolean isReadonly) {
		this.isReadonly = isReadonly;
	}
	public Boolean getIsSum() {
		return isSum;
	}
	public void setIsSum(Boolean isSum) {
		this.isSum = isSum;
	}
	public Boolean getIsMulti() {
		return isMulti;
	}
	public void setIsMulti(Boolean isMulti) {
		this.isMulti = isMulti;
	}
	public String getDataPurviewFactor() {
		return dataPurviewFactor;
	}
	public void setDataPurviewFactor(String dataPurviewFactor) {
		this.dataPurviewFactor = dataPurviewFactor;
	} 
}
