package com.zlp.platform.dao.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

//数据集
public class DataTable {
	
	//字段列表
	private List<Field> fields=new ArrayList<Field>();	
	public List<Field> getFields(){
		return this.fields;
	}
	
	//数据行
	private List<DataRow> rows = new ArrayList<DataRow>();
	public List<DataRow> getRows(){
		return this.rows;
	}
	
	//添加字段声明
	public void setField(String name, ValueType type){
		for(Field f:this.fields){
			if(f.getName()==name){
				f.setType(type);
				break;
			}
		}
		Field nf=new Field();
		nf.setName(name);
		nf.setType(type);
		this.fields.add(nf);
	}
	
	//添加行
	public void addRow(DataRow row){
		this.rows.add(row);
	}	
	
	public HashMap<Object, Object> toHashMap() throws Exception{
		JSONArray fieldArray = new JSONArray(); 
		for(Field field: this.getFields()){
			JSONObject fieldJson = new JSONObject();
			fieldJson.put("name", field.getName());
			try{
				fieldJson.put("valueType", field.getValueType().toString());
			}
			catch(Exception ex){
				throw ex;
			}
			fieldArray.add(fieldJson); 
		}
		
		
		JSONArray rowArray = new JSONArray();
		for(DataRow row : this.getRows()){
			JSONObject rowJson = new JSONObject();
			for(Field field: this.getFields()){ 
			  String valueStr = row.getStringValue(field.getName(), field.getValueType());
			  valueStr = DataTable.processQuotes(valueStr);
			  rowJson.put(field.getName(), valueStr);
			}  
			rowArray.add(rowJson);
		}
		
		HashMap<Object, Object> dtMap = new HashMap<Object, Object>();
		dtMap.put("fields", fieldArray);
		dtMap.put("rows", rowArray);
		return dtMap;
	}
	
	public static String processQuotes(String valueStr){
		if(valueStr != null){
			valueStr = valueStr.replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
		}
		return valueStr;
	}
}
