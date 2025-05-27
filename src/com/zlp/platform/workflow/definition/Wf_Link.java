package com.zlp.platform.workflow.definition;

import java.util.HashMap;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 

//流程边
public class Wf_Link {
	public void initByRow(DataRow linkRow){
		this.id = linkRow.getStringValue("id"); 
		this.conditionDef = linkRow.getStringValue("conditiondef");  
		this.conditionExp = linkRow.getStringValue("conditionexp");  
		this.description = linkRow.getStringValue("description");  
		this.fromNodeId = linkRow.getStringValue("fromnodeid");  
		this.lineType = linkRow.getStringValue("linetype");  
		this.name = linkRow.getStringValue("name");  
		this.toNodeId = linkRow.getStringValue("tonodeid");		
	}

	public String insertToDB(Session dbSession, IDBParserAccess dBParserAccess, String workflowId){
		Data lData = DataCollection.getData("wf_Link");
		HashMap<String, Object> fvs = new HashMap<String, Object>(); 
		fvs.put("workflowid", workflowId); 
		fvs.put("conditiondef",  this.getConditionDef()); 
		fvs.put("conditionexp", this.getConditionExp());  
		fvs.put("description", this.getDescription());  
		fvs.put("fromnodeid", this.getFromNodeId());  
		fvs.put("linetype", this.getLineType()); 
		fvs.put("name", this.getName()); 
		fvs.put("tonodeid", this.getToNodeId()); 
		return dBParserAccess.insertByData(dbSession, lData, fvs);
	}

	public void updateToDB(Session dbSession, IDBParserAccess dBParserAccess, String workflowId){
		Data lData = DataCollection.getData("wf_Link");
		HashMap<String, Object> fvs = new HashMap<String, Object>(); 
		fvs.put("workflowid", workflowId); 
		fvs.put("conditiondef",  this.getConditionDef()); 
		fvs.put("conditionexp", this.getConditionExp());  
		fvs.put("description", this.getDescription());  
		fvs.put("fromnodeid", this.getFromNodeId());  
		fvs.put("linetype", this.getLineType()); 
		fvs.put("name", this.getName()); 
		fvs.put("tonodeid", this.getToNodeId()); 
		dBParserAccess.updateByData(dbSession, lData, fvs, this.getId());
	}
	
	public void initByJson(JSONObject linkJson) throws Exception{
		this.id = (String)CommonFunction.getValueFromJson(linkJson, "id", ValueType.String, false, false); 
		this.conditionDef = (String)CommonFunction.getValueFromJson(linkJson, "conditionDef", ValueType.String, true, false); 
		this.conditionExp = (String)CommonFunction.getValueFromJson(linkJson, "conditionExp", ValueType.String, true, false); 
		this.description = (String)CommonFunction.getValueFromJson(linkJson, "description", ValueType.String, true, false); 
		this.fromNodeId = (String)CommonFunction.getValueFromJson(linkJson, "fromNodeId", ValueType.String, false, false); 
		this.lineType = (String)CommonFunction.getValueFromJson(linkJson, "lineType", ValueType.String, false, false); 
		this.name = (String)CommonFunction.getValueFromJson(linkJson, "name", ValueType.String, true, false); 
		this.toNodeId = (String)CommonFunction.getValueFromJson(linkJson, "toNodeId", ValueType.String, false, false); 		
	}
	
	public JSONObject toJson() throws Exception{
		JSONObject linkJson = new JSONObject(); 
		linkJson.put("id", this.id); 
		linkJson.put("conditionDef", CommonFunction.encode(this.conditionDef)); 
		linkJson.put("conditionExp", CommonFunction.encode(this.conditionExp));  
		linkJson.put("description", CommonFunction.encode(this.description));  
		linkJson.put("fromNodeId", CommonFunction.encode(this.fromNodeId));  
		linkJson.put("lineType", this.lineType); 
		linkJson.put("name", CommonFunction.encode(this.name)); 
		linkJson.put("toNodeId", this.toNodeId); 
		return linkJson;
	}

	private String id = null;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}

	private String name = null;
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	 
	private String fromNodeId = null;
	public String getFromNodeId(){
		return this.fromNodeId;
	}
	public void setFromNodeId(String fromNodeId){
		this.fromNodeId = fromNodeId;
	}

	private String toNodeId = null;
	public String getToNodeId(){
		return this.toNodeId;
	}
	public void setToNodeId(String toNodeId){
		this.toNodeId = toNodeId;
	} 

	private String conditionDef = null;
	public String getConditionDef(){
		return this.conditionDef;
	}
	public void setConditionDef(String conditionDef){
		this.conditionDef = conditionDef;
	} 

	private String conditionExp = null;
	public String getConditionExp(){
		return this.conditionExp;
	}
	public void setConditionExp(String conditionExp){
		this.conditionExp = conditionExp;
	} 

	private String lineType = null;
	public String getLineType(){
		return this.lineType;
	}
	public void setLineType(String lineType){
		this.lineType = lineType;
	}  

	private String description = null;
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
}
