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

//流程节点
public class Wf_Node {
	public void initByRow(DataRow nodeRow){
		this.id = nodeRow.getStringValue("id"); 
		this.actionPageUrl = nodeRow.getStringValue("actionpageurl"); 
		this.reviewPageUrl = nodeRow.getStringValue("reviewpageurl"); 
		this.backInDef = nodeRow.getStringValue("backindef"); 
		this.backInExp = nodeRow.getStringValue("backinexp"); 
		this.canBackFrom = nodeRow.getBooleanValue("canbackfrom"); 
		this.canBackTo = nodeRow.getBooleanValue("canbackto"); 
		this.description = nodeRow.getStringValue("description"); 
		this.groupId = nodeRow.getStringValue("groupid"); 
		this.inDef = nodeRow.getStringValue("indef"); 
		this.inExp = nodeRow.getStringValue("inexp"); 
		this.name = nodeRow.getStringValue("name"); 
		this.triggerType = nodeRow.getStringValue("triggertype"); 
		this.nodeType = nodeRow.getStringValue("nodetype"); 
		this.passDef = nodeRow.getStringValue("passdef"); 
		this.passExp = nodeRow.getStringValue("passexp");  
		this.positionX = nodeRow.getIntegerValue("positionx"); 
		this.positionY = nodeRow.getIntegerValue("positiony"); 
		this.statusName = nodeRow.getStringValue("statusname"); 
		this.ticketDef = nodeRow.getStringValue("ticketdef"); 
		this.ticketExp = nodeRow.getStringValue("ticketexp"); 
		this.timingDef = nodeRow.getStringValue("timingdef"); 
		this.timingExp = nodeRow.getStringValue("timingexp"); 
		this.aisId = nodeRow.getStringValue("aisid"); 
		this.aisName = nodeRow.getStringValue("aisname"); 
		this.aisParameterExp = nodeRow.getStringValue("aisparameterexp"); 
		this.aisParameterConfig = nodeRow.getStringValue("aisparameterconfig"); 
		this.userDef = nodeRow.getStringValue("userdef"); 
		this.userExp = nodeRow.getStringValue("userexp"); 	
	}

	public String insertToDB(Session dbSession, IDBParserAccess dBParserAccess, String workflowId){
		Data nData = DataCollection.getData("wf_Node");
		HashMap<String, Object> fvs = new HashMap<String, Object>(); 
		fvs.put("workflowid", workflowId);  
		fvs.put("actionpageurl", this.getActionPageUrl()); 
		fvs.put("reviewpageurl", this.getReviewPageUrl()); 
		fvs.put("backindef", this.getBackInDef()); 
		fvs.put("backinexp", this.getBackInExp()); 
		fvs.put("canbackfrom", this.getCanBackFrom() ? "Y" : "N"); 
		fvs.put("canbackto", this.getCanBackTo() ? "Y" : "N"); 
		fvs.put("description", this.getDescription()); 
		fvs.put("groupid", this.getGroupId()); 
		fvs.put("indef", this.getInDef()); 
		fvs.put("inexp", this.getInExp()); 
		fvs.put("name", this.getName()); 
		fvs.put("triggertype", this.getTriggerType()); 
		fvs.put("passdef", this.getPassDef()); 
		fvs.put("passexp", this.getPassExp());  
		fvs.put("positionx", this.getPositionX()); 
		fvs.put("positiony", this.getPositionY()); 
		fvs.put("statusname", this.getStatusName()); 
		fvs.put("ticketdef", this.getTicketDef()); 
		fvs.put("ticketexp", this.getTicketExp()); 
		fvs.put("timingdef", this.getTimingDef()); 
		fvs.put("timingexp", this.getTimingExp()); 
		fvs.put("aisid", this.getAisId()); 
		fvs.put("aisparameterexp", this.getAisParameterExp()); 
		fvs.put("userdef", this.getUserDef()); 
		fvs.put("userexp", this.getUserExp());
		fvs.put("nodetype", this.getNodeType());
		return dBParserAccess.insertByData(dbSession, nData, fvs);
	}

	public void updateToDB(Session dbSession, IDBParserAccess dBParserAccess, String workflowId){
		Data nData = DataCollection.getData("wf_Node");
		HashMap<String, Object> fvs = new HashMap<String, Object>(); 
		fvs.put("workflowid", workflowId);  
		fvs.put("actionpageurl", this.getActionPageUrl()); 
		fvs.put("reviewpageurl", this.getReviewPageUrl()); 
		fvs.put("backindef", this.getBackInDef()); 
		fvs.put("backinexp", this.getBackInExp()); 
		fvs.put("canbackfrom", this.getCanBackFrom() ? "Y" : "N"); 
		fvs.put("canbackto", this.getCanBackTo() ? "Y" : "N"); 
		fvs.put("description", this.getDescription()); 
		fvs.put("groupid", this.getGroupId()); 
		fvs.put("indef", this.getInDef()); 
		fvs.put("inexp", this.getInExp()); 
		fvs.put("name", this.getName()); 
		fvs.put("triggertype", this.getTriggerType()); 
		fvs.put("passdef", this.getPassDef()); 
		fvs.put("passexp", this.getPassExp());  
		fvs.put("positionx", this.getPositionX()); 
		fvs.put("positiony", this.getPositionY()); 
		fvs.put("statusname", this.getStatusName()); 
		fvs.put("ticketdef", this.getTicketDef()); 
		fvs.put("ticketexp", this.getTicketExp()); 
		fvs.put("timingdef", this.getTimingDef()); 
		fvs.put("timingexp", this.getTimingExp()); 
		fvs.put("aisid", this.getAisId());  
		fvs.put("aisparameterexp", this.getAisParameterExp());  
		fvs.put("timingexp", this.getTimingExp()); 
		fvs.put("userdef", this.getUserDef()); 
		fvs.put("userexp", this.getUserExp());
		dBParserAccess.updateByData(dbSession, nData, fvs, this.getId());
	}
	
	public void initByJson(JSONObject nodeJson) throws Exception{
		this.id = (String)CommonFunction.getValueFromJson(nodeJson, "id", ValueType.String, false, false); 
		this.actionPageUrl = (String)CommonFunction.getValueFromJson(nodeJson, "actionPageUrl", ValueType.String, true, false); 
		this.reviewPageUrl = (String)CommonFunction.getValueFromJson(nodeJson, "reviewPageUrl", ValueType.String, true, false); 
		this.backInDef = (String)CommonFunction.getValueFromJson(nodeJson, "backInDef", ValueType.String, true, false); 
		this.backInExp = (String)CommonFunction.getValueFromJson(nodeJson, "backInExp", ValueType.String, true, false); 
		this.canBackFrom = (boolean)CommonFunction.getValueFromJson(nodeJson, "canBackFrom", ValueType.Boolean, false, false); 
		this.canBackTo = (boolean)CommonFunction.getValueFromJson(nodeJson, "canBackTo", ValueType.Boolean, false, false); 
		this.description = (String)CommonFunction.getValueFromJson(nodeJson, "description", ValueType.String, true, false); 
		this.groupId = (String)CommonFunction.getValueFromJson(nodeJson, "groupId", ValueType.String, false, false); 
		this.inDef = (String)CommonFunction.getValueFromJson(nodeJson, "inDef", ValueType.String, true, false); 
		this.inExp = (String)CommonFunction.getValueFromJson(nodeJson, "inExp", ValueType.String, true, false); 
		this.name = (String)CommonFunction.getValueFromJson(nodeJson, "name", ValueType.String, true, false); 
		this.triggerType = (String)CommonFunction.getValueFromJson(nodeJson, "triggerType", ValueType.String, false, false); 
		this.nodeType = (String)CommonFunction.getValueFromJson(nodeJson, "nodeType", ValueType.String, false, false); 
		this.passDef = (String)CommonFunction.getValueFromJson(nodeJson, "passDef", ValueType.String, true, false); 
		this.passExp = (String)CommonFunction.getValueFromJson(nodeJson, "passExp", ValueType.String, true, false);  
		this.positionX = ((Double)CommonFunction.getValueFromJson(nodeJson, "positionX", ValueType.Decimal, false, false)).intValue(); 
		this.positionY = ((Double)CommonFunction.getValueFromJson(nodeJson, "positionY", ValueType.Decimal, false, false)).intValue(); 
		this.statusName = (String)CommonFunction.getValueFromJson(nodeJson, "statusName", ValueType.String, true, false); 
		this.ticketDef = (String)CommonFunction.getValueFromJson(nodeJson, "ticketDef", ValueType.String, true, false); 
		this.ticketExp = (String)CommonFunction.getValueFromJson(nodeJson, "ticketExp", ValueType.String, true, false); 
		this.timingDef = (String)CommonFunction.getValueFromJson(nodeJson, "timingDef", ValueType.String, true, false); 
		this.timingExp = (String)CommonFunction.getValueFromJson(nodeJson, "timingExp", ValueType.String, true, false); 
		this.aisId = (String)CommonFunction.getValueFromJson(nodeJson, "aisId", ValueType.String, false, false); 
		this.aisParameterExp = (String)CommonFunction.getValueFromJson(nodeJson, "aisParameterExp", ValueType.String, true, false); 
		this.userDef = (String)CommonFunction.getValueFromJson(nodeJson, "userDef", ValueType.String, true, false); 
		this.userExp = (String)CommonFunction.getValueFromJson(nodeJson, "userExp", ValueType.String, true, false); 
	}
	
	public JSONObject toJson() throws Exception{
		JSONObject nodeJson = new JSONObject(); 
		nodeJson.put("id", this.id);
		nodeJson.put("actionPageUrl", CommonFunction.encode(this.actionPageUrl)); 
		nodeJson.put("reviewPageUrl", CommonFunction.encode(this.reviewPageUrl)); 
		nodeJson.put("backInDef", CommonFunction.encode(this.backInDef)); 
		nodeJson.put("backInExp", CommonFunction.encode(this.backInExp)); 
		nodeJson.put("canBackFrom", this.canBackFrom ? "true" : "false"); 
		nodeJson.put("canBackTo", this.canBackTo ? "true" : "false"); 
		nodeJson.put("description", CommonFunction.encode(this.description)); 
		nodeJson.put("groupId", this.groupId); 
		nodeJson.put("inDef", CommonFunction.encode(this.inDef)); 
		nodeJson.put("inExp", CommonFunction.encode(this.inExp)); 
		nodeJson.put("name", CommonFunction.encode(this.name)); 
		nodeJson.put("triggerType", this.triggerType); 
		nodeJson.put("nodeType", this.nodeType); 
		nodeJson.put("passDef", CommonFunction.encode(this.passDef)); 
		nodeJson.put("passExp", CommonFunction.encode(this.passExp));  
		nodeJson.put("positionX", this.positionX); 
		nodeJson.put("positionY", this.positionY); 
		nodeJson.put("statusName",CommonFunction.encode( this.statusName)); 
		nodeJson.put("ticketDef", CommonFunction.encode(this.ticketDef)); 
		nodeJson.put("ticketExp", CommonFunction.encode(this.ticketExp)); 
		nodeJson.put("timingDef", CommonFunction.encode(this.timingDef)); 
		nodeJson.put("timingExp", CommonFunction.encode(this.timingExp)); 
		nodeJson.put("userDef", CommonFunction.encode(this.userDef)); 
		nodeJson.put("userExp", CommonFunction.encode(this.userExp));
		nodeJson.put("aisId", this.aisId); 
		nodeJson.put("aisName", CommonFunction.encode(this.aisName)); 
		nodeJson.put("aisParameterExp", CommonFunction.encode(this.aisParameterExp)); 
		nodeJson.put("aisParameterConfig", CommonFunction.encode(this.aisParameterConfig)); 
		return nodeJson;
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
	 
	private int positionX = 0;
	public int getPositionX(){
		return this.positionX;
	}
	public void setPositionX(int positionX){
		this.positionX = positionX;
	}
	 
	private int positionY = 0;
	public int getPositionY(){
		return this.positionY;
	}
	public void setPositionY(int positionY){
		this.positionY = positionY;
	} 

	private String triggerType = null;
	public String getTriggerType(){
		return this.triggerType;
	}
	public void setTriggerType(String triggerType){
		this.triggerType = triggerType;
	}
	
	public TriggerType getTriggerTypeEnum(){		
		return this.getTriggerType() == null || this.getTriggerType().isEmpty() ? null : (TriggerType)Enum.valueOf(TriggerType.class, this.getTriggerType());
	}

	private String nodeType = null;
	public String getNodeType(){
		return this.nodeType;
	}
	public void setNodeType(String nodeType){
		this.nodeType = nodeType;
	}

	private String statusName = null;
	public String getStatusName(){
		return this.statusName;
	}
	public void setStatusName(String statusName){
		this.statusName = statusName;
	}

	private boolean canBackFrom = true;
	public boolean getCanBackFrom(){
		return this.canBackFrom;
	}
	public void setCanBackFrom(boolean canBackFrom){
		this.canBackFrom = canBackFrom;
	}

	private boolean canBackTo = true;
	public boolean getCanBackTo(){
		return this.canBackTo;
	}
	public void setCanBackTo(boolean canBackTo){
		this.canBackTo = canBackTo;
	}


	private String actionPageUrl = null;
	public String getActionPageUrl(){
		return this.actionPageUrl;
	}
	public void setActionPageUrl(String actionPageUrl){
		this.actionPageUrl = actionPageUrl;
	}
	private String reviewPageUrl = null;
	public String getReviewPageUrl(){
		return this.reviewPageUrl;
	}
	public void setReviewPageUrl(String reviewPageUrl){
		this.reviewPageUrl = reviewPageUrl;
	}

	private String userDef = null;
	public String getUserDef(){
		return this.userDef;
	}
	public void setUserDef(String userDef){
		this.userDef = userDef;
	}

	private String userExp = null;
	public String getUserExp(){
		return this.userExp;
	}
	public void setUserExp(String userExp){
		this.userExp = userExp;
	}

	private String backInDef = null;
	public String getBackInDef(){
		return this.backInDef;
	}
	public void setBackInDef(String backInDef){
		this.backInDef = backInDef;
	}
	
	private String backInExp = null;
	public String getBackInExp(){
		return this.backInExp;
	}
	public void setBackInExp(String backInExp){
		this.backInExp = backInExp;
	}
	
	private String inDef = null;
	public String getInDef(){
		return this.inDef;
	}
	public void setInDef(String inDef){
		this.inDef = inDef;
	}
	
	private String inExp = null;
	public String getInExp(){
		return this.inExp;
	}
	public void setInExp(String inExp){
		this.inExp = inExp;
	}
	
	private String passDef = null;
	public String getPassDef(){
		return this.passDef;
	}
	public void setPassDef(String passDef){
		this.passDef = passDef;
	}
	
	private String passExp = null;
	public String getPassExp(){
		return this.passExp;
	}
	public void setPassExp(String passExp){
		this.passExp = passExp;
	}
	
	private String ticketDef = null;
	public String getTicketDef(){
		return this.ticketDef;
	}
	public void setTicketDef(String ticketDef){
		this.ticketDef = ticketDef;
	}
	
	private String ticketExp = null;
	public String getTicketExp(){
		return this.ticketExp;
	}
	public void setTicketExp(String ticketExp){
		this.ticketExp = ticketExp;
	}
	
	private String timingExp = null;
	public String getTimingExp(){
		return this.timingExp;
	}
	public void setTimingExp(String timingExp){
		this.timingExp = timingExp;
	}
	
	private String timingDef = null;
	public String getTimingDef(){
		return this.timingDef;
	}
	public void setTimingDef(String timingDef){
		this.timingDef = timingDef;
	}
	
	private String aisId = null;
	public String getAisId(){
		return this.aisId;
	}
	public void setAisId(String aisId){
		this.aisId = aisId;
	}
	
	private String aisName = null;
	public String getAisName(){
		return this.aisName;
	}
	public void setAisName(String aisName){
		this.aisName = aisName;
	}
	
	private String aisParameterConfig = null;
	public String getAisParameterConfig(){
		return this.aisParameterConfig;
	}
	public void setAisParameterConfig(String aisParameterConfig){
		this.aisParameterConfig = aisParameterConfig;
	}
	
	private String aisParameterExp = null;
	public String getAisParameterExp(){
		return this.aisParameterExp;
	}
	public void setAisParameterExp(String aisParameterExp){
		this.aisParameterExp = aisParameterExp;
	} 

	private String description = null;
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}	

	private String groupId = null;
	public String getGroupId(){
		return this.groupId;
	}
	public void setGroupId(String groupId){
		this.groupId = groupId;
	}
}
