package com.zlp.platform.model.sysmodel;

import java.util.HashMap; 
 
public class ParamWin {
	private String id;
	private String name; 
	private HashMap<String, ParamWinUnit> units = new HashMap<String, ParamWinUnit>(); 
	
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
	public HashMap<String, ParamWinUnit> getUnits() {
		return units;
	} 
	public void setUnit(String name,ParamWinUnit unit) {
		this.units.put(name, unit);
	}		
	
	public ParamWinUnit getUnit(String name){
		return this.units.get(name);
	}
}
