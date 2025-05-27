package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.List;

import com.zlp.platform.dao.db.ValueType;

public class ParamWinUnit {
	private String id;
	private String parentId;
	private String name;  
	private String label;
	private ValueType valueType;
	private String defaultValue;
	private Boolean isMultiValue;
	private String inputHelpType;
	private String inputHelpName;
	private Boolean isNullable;
	private Boolean isEditable;
	private int valueLength;
	private int decimalNum;
	private List<ParamWinUnitMap> paramWinUnitMap = new ArrayList<ParamWinUnitMap>();
	public void addParamWinUnitMap(ParamWinUnitMap paramWinUnitMap) {
		this.paramWinUnitMap.add(paramWinUnitMap);
	}
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Boolean getIsMultiValue() {
		return isMultiValue;
	}
	public void setIsMultiValue(Boolean isMultiValue) {
		this.isMultiValue = isMultiValue;
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
	public Boolean getIsNullable() {
		return isNullable;
	}
	public void setIsNullable(Boolean isNullable) {
		this.isNullable = isNullable;
	}
	public Boolean getIsEditable() {
		return isEditable;
	}
	public void setIsEditable(Boolean isEditable) {
		this.isEditable = isEditable;
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
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public List<ParamWinUnitMap> getParamWinUnitMap() {
		return paramWinUnitMap;
	} 
	 
}
