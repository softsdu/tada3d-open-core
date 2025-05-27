package com.zlp.platform.model.sysmodel; 

public class EventExpression {
	private String id;
	private String eventDescription; 
	private String parentId;
	private String eventId;
	private String eventName;
	private String eventResultValueType;
	private String eventCategory;
	private String exp;
	private String description; 
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEventDescription() {
		return eventDescription;
	}
	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}  
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	} 
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	} 
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	} 
	public String getEventResultValueType() {
		return eventResultValueType;
	}
	public void setEventResultValueType(String eventResultValueType) {
		this.eventResultValueType = eventResultValueType;
	} 
	public String getEventCategory() {
		return eventCategory;
	}
	public void setEventCategory(String eventCategory) {
		this.eventCategory = eventCategory;
	} 
	public String getExp() {
		return exp;
	}
	public void setExp(String exp) {
		this.exp = exp;
	} 
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	} 
}
