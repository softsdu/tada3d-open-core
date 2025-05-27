package com.zlp.platform.model.sysmodel.dbStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zlp.platform.common.ValueConverter;

public class TableCompare { 
	private String tableName = null;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	 
	public Treatment getTreatment() { 
		boolean isAllNewField = true;
		boolean hasNewField = false;
		boolean hasAlterField = false;
		for(FieldCompare fcResult : this.fields.values()){
			Treatment ft = fcResult.getTreatment();
			if(ft != Treatment.Add){
				isAllNewField = false; 
			}
			if(ft == Treatment.Add){
				hasNewField = true;
			}
			if(ft == Treatment.Alter){
				hasAlterField = true;
			}
		}
		return isAllNewField ? Treatment.Add : (hasNewField || hasAlterField ? Treatment.Alter : Treatment.None);
	} 

	protected HashMap<String, FieldCompare> fields = new HashMap<String, FieldCompare>();

	public void setFieldCompare(String fieldName, FieldCompare fieldCompare){
		this.fields.put(fieldName, fieldCompare);
	}
	public FieldCompare getFieldCompare(String fieldName){
		return this.fields.get(fieldName);
	}
	
	public boolean isSame(){ 
		for(FieldCompare fcResult : this.fields.values()){
			if(!fcResult.isSame()){
				return false;
			}
		}
		return true;
	}
	
	public String getTreatmentDescription() throws Exception { 
		List<String> messageList = new ArrayList<String>();
		Treatment t = this.getTreatment();
		switch(t){
			case Alter:{
					messageList.add("修改表: " + this.getTableName());
					for(FieldCompare fcResult : this.fields.values()){ 
						String description = fcResult.getTreatmentDescription();
						if(description != null && description.length() > 0){
							messageList.add(description);
						} 
					}  
				}
				break;
			case Add:{
					messageList.add("新建表: " + this.getTableName());
				}
				break;
			default:
				break;
		}
		return ValueConverter.arrayToString(messageList, "\n"); 
	}
}
