package com.zlp.platform.model.sysmodel.dbStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zlp.platform.common.ValueConverter;

public class FieldCompare {
	public FieldCompare(String fieldName, List<PropertyCompare> propertyList){
		this.setFieldName(fieldName);
		for(PropertyCompare pc : propertyList){
			this.setPropertyCompare(pc.getPropertyType(), pc);
		}
	} 
	
	private String fieldName = null;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	} 
	 
	public Treatment getTreatment() { 
		boolean isNew = true;
		boolean isAlter = false;
		for(PropertyCompare pc : this.properties.values()){
			if(pc.getDestValue() != null){
				isNew = false; 
			}
			if(!pc.isSame()){
				isAlter = true;
			}
			
		}
		return isNew ? Treatment.Add : (isAlter ? Treatment.Alter : Treatment.None);
	} 
	
	private HashMap<FieldPropertyType, PropertyCompare> properties = new HashMap<FieldPropertyType, PropertyCompare>();

	public void setPropertyCompare(FieldPropertyType propertyType, PropertyCompare propertyCompareResult){
		this.properties.put(propertyType, propertyCompareResult);
	}
	public PropertyCompare getPropertyCompare(FieldPropertyType propertyType){
		return this.properties.get(propertyType);
	}
	
	public boolean isSame(){ 
		for(PropertyCompare pc : this.properties.values()){
			if(!pc.isSame()){
				return false;
			}
		}
		return true;
	}
	 
	public String getTreatmentDescription() { 
		List<String> messageList = new ArrayList<String>();
		Treatment t = this.getTreatment();
		switch(t){
			case Alter:{
					messageList.add("修改字段: " + this.getFieldName());
					for(PropertyCompare pc : this.properties.values()){ 
						if(!pc.isSame()){
							messageList.add("  " + pc.getPropertyType().toString() + "属性不同, " + pc.getSourceValue() + " vs "+ pc.getDestValue());
						}
					}  
				}
				break;
			case Add:{
					messageList.add("新增字段: " + this.getFieldName());
				}
				break;
			default:
				break;
		}
		return ValueConverter.arrayToString(messageList, "\n");
	} 
}