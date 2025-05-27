package com.zlp.platform.model.sysmodel.dbStructure;

public class PropertyCompare {
	
	public PropertyCompare(FieldPropertyType propertyType, String sourceValue, String destValue){
		this.setPropertyType(propertyType);
		this.setSourceValue(sourceValue);
		this.setDestValue(destValue);
	}
	
	private FieldPropertyType propertyType = null;
	public FieldPropertyType getPropertyType(){
		return this.propertyType;
	}
	public void setPropertyType(FieldPropertyType propertyType){
		this.propertyType = propertyType;
	}
	
	public boolean isSame(){
		if(this.getDestValue() == null){
			return this.getSourceValue() == null || this.getSourceValue().length() == 0;
		}
		else{
			return this.getDestValue().equals(this.getSourceValue());
		}
	}
	
	private String destValue = "";
	public String getDestValue(){
		return this.destValue;
	}
	public void setDestValue(String destValue){
		this.destValue = destValue;
	}
	
	private String sourceValue = "";
	public String getSourceValue(){
		return this.sourceValue;
	}
	public void setSourceValue(String sourceValue){
		this.sourceValue = sourceValue;
	}
}
