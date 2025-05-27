package com.zlp.platform.model.sysmodel;

import java.util.HashMap; 

//视图描述
public class View {
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	

	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	} 	
	
	//增加标题 added by ls 20190618
	private String title;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	} 	

	private String dataName; 
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	private HashMap<String, ViewDispunit> dispunits = new HashMap<String, ViewDispunit>(); 
	public HashMap<String, ViewDispunit> getDispunits() {
		return dispunits;
	} 
	public void setViewDispunit(String name,ViewDispunit viewDispunit) {
		this.dispunits.put(name, viewDispunit);
	}		
	
	public ViewDispunit getViewDispunit(String name){
		return this.dispunits.get(name);
	}
}
