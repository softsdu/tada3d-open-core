package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.List;

import com.zlp.platform.dao.db.SelectSqlParser;
 

//下拉
public class DownList {
	private String id;
	private String name;
	private String dsType;
	private String dsExp; 
	private List<DownListField> downListFields = new ArrayList<DownListField>();
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
	public List<DownListField> getDownListFields() {
		return downListFields;
	}
	public void addDownListField(DownListField downListField) {
		this.downListFields.add(downListField);
	} 
	public SelectSqlParser getDsSqlParser() {
		return dsSqlParser;
	}
	public void setDsSqlParser(SelectSqlParser dsSqlParser) {
		this.dsSqlParser = dsSqlParser;
	}
	
	public DownListField getDownListField(String fieldName){
		for(DownListField field : this.downListFields){
			if(field.getName().equals(fieldName)){
				return field;
			}
		}
		return null;
	}
}
