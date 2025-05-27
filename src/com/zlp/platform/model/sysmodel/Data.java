package com.zlp.platform.model.sysmodel;

import java.util.HashMap;
import java.util.List;

import com.zlp.platform.dao.db.SelectSqlParser;

//视图描述
public class Data  {
	private String id;
	private String name;
	private String dsType;
	private String dsExp;
	private String saveType;
	private String saveDest;
	private String idFieldName;
	private HashMap<String, DataField> dataFields = new HashMap<String, DataField>();
	private HashMap<String, List<EventServerExpression>> eventToExpList = new HashMap<String, List<EventServerExpression>>();
	private SelectSqlParser dsSqlParser ;
	
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
	
	public String getDsType() {
		return dsType;
	}
	public void setDsType(String dsType) {
		this.dsType = dsType;
	}	
	
	public String getDsExp() {
		return dsExp;
	}
	public void setDsExp(String dsExp) {
		this.dsExp = dsExp;
	}
	
	public String getSaveType() {
		return saveType;
	}
	public void setSaveType(String saveType) {
		this.saveType = saveType;
	}	
	
	public String getSaveDest() {
		return saveDest;
	}
	public void setSaveDest(String saveDest) {
		this.saveDest = saveDest;
	}
	
	public SelectSqlParser getDsSqlParser() {
		return dsSqlParser;
	}
	
	public void setDsSqlParser(SelectSqlParser dsSqlParser) {
		this.dsSqlParser = dsSqlParser;
	}
	public HashMap<String, DataField> getDataFields() {
		return dataFields;
	}
	
	public void setDataField(String name,DataField dataField) {
		this.dataFields.put(name, dataField);
	}			
	public DataField getDataField(String name){
		return this.dataFields.get(name);
	}
	
	public String getIdFieldName() {
		return idFieldName;
	}
	public void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}

	public HashMap<String, List<EventServerExpression>> getEventToExpList() {
		return eventToExpList;
	}
	public void setEventToExps(String eventName,List<EventServerExpression> exps) {
		this.eventToExpList.put(eventName, exps);
	}	 
	public List<EventServerExpression> getEventToExps(String eventName) {
		return this.eventToExpList.containsKey(eventName) ? this.eventToExpList.get(eventName) : null;
	}	 
	/*
	public String toJsonStr() throws Exception{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", this.getId());
		jsonObj.put("name", this.getName());
		jsonObj.put("idFieldName", this.getIdFieldName());
		HashMap<String,JSONObject> vfs = new HashMap<String,JSONObject>();
		jsonObj.put("fields", vfs);
		for(DataField vf : this.getDataFields().values()){
			JSONObject vfObj = new JSONObject();
			vfObj.put("id", vf.getId());
			vfObj.put("name", vf.getName());
			vfObj.put("displayName", vf.getDisplayName());
			vfObj.put("valueType", vf.getValueType().toString());
			vfObj.put("isSave", vf.getIsSave());
			vfObj.put("inputHelpType", vf.getInputHelpType());
			vfObj.put("inputHelpName",vf.getInputHelpName());
			vfObj.put("foreignKeyName", vf.getForeignKeyName());
			vfObj.put("isComma", vf.getIsComma());
			vfObj.put("valueLength", vf.getValueLength());
			vfObj.put("decimalNum", vf.getDecimalNum());
			vfObj.put("isReadonly", vf.getIsReadonly());
			vfObj.put("isSum", vf.getIsSum());
			vfs.put(vf.getName(), vfObj);
			
			JSONArray vfmArray = new JSONArray();
			vfObj.put("maps", vfmArray); 
			for(DataFieldMap vfm: vf.getDataFieldMap()){
				JSONObject vfmObj = new JSONObject();
				vfmObj.put("id", vfm.getId());
				vfmObj.put("sourceField", vfm.getSourceField());
				vfmObj.put("destField", vfm.getDestField());
				vfmArray.add(vfmObj);
			}			
		}
		return JSONProcessor.JSONToStr(jsonObj);
	}*/
}
