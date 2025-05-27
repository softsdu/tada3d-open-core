package com.zlp.platform.workflow.definition;
 
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.hibernate.Session;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 

//流程
public class Wf_Workflow { 
	
	//从数据库中初始化流程
	public void initByDB(DataRow workflowRow, List<DataRow> allNodeRows, List<DataRow> allLinkRows) throws Exception{
		this.id = workflowRow.getStringValue("id");
		this.code = workflowRow.getStringValue("code");
		this.name = workflowRow.getStringValue("name");  
		this.description = workflowRow.getStringValue("description");
		this.docTypeId = workflowRow.getStringValue("doctypeid");
		this.isActive = (Boolean)ValueConverter.convertToObject(workflowRow.getStringValue("isactive"), ValueType.Boolean);
		this.isDeleted =  (Boolean)ValueConverter.convertToObject(workflowRow.getStringValue("isdeleted"), ValueType.Boolean);
		this.orgId = workflowRow.getStringValue("orgid");		
		this.createTime = workflowRow.getDateTimeValue("createtime");
		this.modifyTime = workflowRow.getDateTimeValue("modifytime");
		this.createUserCode = workflowRow.getStringValue("createusercode");
		this.createUserId = workflowRow.getStringValue("createuserid");
		this.createUserName = workflowRow.getStringValue("createusername");
		this.docTypeName = workflowRow.getStringValue("doctypename");
		this.orgCode = workflowRow.getStringValue("orgcode");
		this.orgName = workflowRow.getStringValue("orgname");
		this.abstractExp = workflowRow.getStringValue("abstractexp");

		List<Wf_Node> allNodes = new ArrayList<Wf_Node>(); 
		int nodeCount = allNodeRows.size();
		for(int i=0;i<nodeCount;i++){
			DataRow nodeRow = allNodeRows.get(i);
			Wf_Node node = new Wf_Node();
			node.initByRow(nodeRow);
			allNodes.add(node);
		}
		this.setNodes(allNodes);		

		List<Wf_Link> allLinks = new ArrayList<Wf_Link>(); 
		int linkCount = allLinkRows.size();
		for(int i=0;i<linkCount;i++){
			DataRow linkRow = allLinkRows.get(i);
			Wf_Link link = new Wf_Link();
			link.initByRow(linkRow);
			allLinks.add(link);
		}
		this.setLinks(allLinks);	
	}

	//将流程信息保存到数据库
	public String insertToDB(INcpSession ncpSession, Session dbSession, IDBParserAccess dBParserAccess){
		Data wfData = DataCollection.getData("wf_Workflow");
		HashMap<String, Object> fvs = new HashMap<String, Object>();  

    	UUID uuid = UUID.randomUUID();  
		fvs.put("code", uuid.toString());
		fvs.put("isdeleted", "N");
		
		Date currentTime = new Date();
		fvs.put("createtime", currentTime); 
		fvs.put("modifytime", currentTime); 
		
		fvs.put("name", this.getName());  
		fvs.put("description", this.getDescription());
		fvs.put("doctypeid", this.getDocTypeId());
		fvs.put("isactive", this.getIsActive() ? "Y" : "N");
		fvs.put("orgid", this.getOrgId());		
		fvs.put("createuserid", ncpSession.getUserId()); 
		fvs.put("abstractexp", this.getAbstractExp()); 
		String workflowId = dBParserAccess.insertByData(dbSession, wfData, fvs);
		return workflowId;
	}
	
	//将流程信息更新到数据库
	public void updateToDB(INcpSession ncpSession, Session dbSession, IDBParserAccess dBParserAccess){
		Data wfData = DataCollection.getData("wf_Workflow");
		HashMap<String, Object> fvs = new HashMap<String, Object>();  
  
		fvs.put("isdeleted", "N");
		fvs.put("modifytime", new Date()); 
		
		fvs.put("name", this.getName());  
		fvs.put("description", this.getDescription());
		fvs.put("doctypeid", this.getDocTypeId());
		fvs.put("isactive", this.getIsActive() ? "Y" : "N");
		fvs.put("orgid", this.getOrgId());		
		fvs.put("createuserid", ncpSession.getUserId()); 
		fvs.put("abstractexp", this.getAbstractExp()); 
		dBParserAccess.updateByData(dbSession, wfData, fvs, this.getId());  
	}
	
	public JSONObject toJson() throws Exception{
		JSONObject wfJson = new JSONObject(); 
		wfJson.put("id", this.id);
		wfJson.put("code", this.code);
		wfJson.put("name", CommonFunction.encode(this.name));  
		wfJson.put("description", CommonFunction.encode(this.description));
		wfJson.put("docTypeId", this.docTypeId);
		wfJson.put("isActive", this.isActive ? "Y" : "N");
		wfJson.put("isDeleted", this.isDeleted ? "Y" : "N"); 
		wfJson.put("orgId", this.orgId);		
		wfJson.put("createTime", ValueConverter.convertToString(this.createTime, ValueType.Time));
		wfJson.put("modifyTime", ValueConverter.convertToString(this.modifyTime, ValueType.Time));
		wfJson.put("createUserCode", CommonFunction.encode(this.createUserCode));
		wfJson.put("createUserId", this.createUserId);
		wfJson.put("createUserName", CommonFunction.encode(this.createUserName));
		wfJson.put("docTypeName", CommonFunction.encode(this.docTypeName));
		wfJson.put("orgCode", CommonFunction.encode(this.orgCode));
		wfJson.put("orgName", CommonFunction.encode(this.orgName));
		wfJson.put("abstractExp", CommonFunction.encode(this.abstractExp)); 

		JSONArray allNodeJsons = new JSONArray(); 
		int nodeCount = this.nodes.size();
		for(int i=0;i<nodeCount;i++){
			Wf_Node node = this.nodes.get(i); 
			JSONObject nodeJson = node.toJson();
			allNodeJsons.add(nodeJson);
		}
		wfJson.put("nodes", allNodeJsons);		

		JSONArray allLinkJsons = new JSONArray(); 
		int linkCount = this.links.size();
		for(int i=0;i<linkCount;i++){
			Wf_Link link = this.links.get(i); 
			JSONObject linkJson = link.toJson();
			allLinkJsons.add(linkJson);
		}
		wfJson.put("links", allLinkJsons);		

		
		return wfJson;
	}
	
	public void initByJson(JSONObject workflowJson) throws Exception{
		this.id = (String)CommonFunction.getValueFromJson(workflowJson, "id", ValueType.String, false, false);
		this.code = (String)CommonFunction.getValueFromJson(workflowJson, "code", ValueType.String, false, false);
		this.name = (String)CommonFunction.getValueFromJson(workflowJson, "name", ValueType.String, true, true);  
		this.description = (String)CommonFunction.getValueFromJson(workflowJson, "description", ValueType.String, true, false);
		this.docTypeId = (String)CommonFunction.getValueFromJson(workflowJson, "docTypeId", ValueType.String, false, false);
		this.isActive = (boolean)CommonFunction.getValueFromJson(workflowJson, "isActive", ValueType.Boolean, false, false);
		this.orgId = (String)CommonFunction.getValueFromJson(workflowJson, "orgId", ValueType.String, false, false);
		this.abstractExp = (String)CommonFunction.getValueFromJson(workflowJson, "abstractExp", ValueType.String, true, false);

		List<Wf_Node> allNodes = new ArrayList<Wf_Node>();
		JSONArray nodeJsonArray = workflowJson.getJSONArray("nodes");
		int nodeCount = nodeJsonArray.size();
		for(int i=0;i<nodeCount;i++){
			JSONObject nodeJson = nodeJsonArray.getJSONObject(i);
			Wf_Node node = new Wf_Node();
			node.initByJson(nodeJson);
			allNodes.add(node);
		}
		this.setNodes(allNodes);		

		List<Wf_Link> allLinks = new ArrayList<Wf_Link>();
		JSONArray linkJsonArray = workflowJson.getJSONArray("links");
		int linkCount = linkJsonArray.size();
		for(int i=0;i<linkCount;i++){
			JSONObject linkJson = linkJsonArray.getJSONObject(i);
			Wf_Link link = new Wf_Link();
			link.initByJson(linkJson);
			allLinks.add(link);
		}
		this.setLinks(allLinks);		
	} 
	
	private String id = null;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	 
	private String code = null;
	public String getCode(){
		return this.code;
	}
	public void setCode(String code){
		this.code = code;
	}
	 
	private String name = null;
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	 
	private String createUserId = null;
	public String getCreateUserId(){
		return this.createUserId;
	}
	public void setCreateUserId(String createUserId){
		this.createUserId = createUserId;
	}
	 
	private String createUserCode = null;
	public String getCreateUserCode(){
		return this.createUserCode;
	}
	public void setCreateUserCode(String createUserCode){
		this.createUserCode = createUserCode;
	}
	 
	private String createUserName = null;
	public String getCreateUserName(){
		return this.createUserName;
	}
	public void setCreateUserName(String createUserName){
		this.createUserName = createUserName;
	}

	private Date createTime = null;
	public Date getCreateTime(){
		return this.createTime;
	}
	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	private Date modifyTime = null;
	public Date getModifyTime(){
		return this.modifyTime;
	}
	public void setModifyTime(Date modifyTime){
		this.modifyTime = modifyTime;
	}
	 
	private String docTypeId = null;
	public String getDocTypeId(){
		return this.docTypeId;
	}
	public void setDocTypeId(String docTypeId){
		this.docTypeId = docTypeId;
	}

	private String docTypeName = null;
	public String getDocTypeName(){
		return this.docTypeName;
	}
	public void setDocTypeName(String docTypeName){
		this.docTypeName = docTypeName;
	} 

	private boolean isDeleted = false;
	public boolean getDeleted(){
		return this.isDeleted;
	}
	public void setIsDeleted(boolean isDeleted){
		this.isDeleted= isDeleted;
	} 

	private boolean isActive = false;
	public boolean getIsActive(){
		return this.isActive;
	}
	public void setIsActive(boolean isActive){
		this.isActive = isActive;
	} 
	 
	private String orgId = null;
	public String getOrgId(){
		return this.orgId;
	}
	public void setOrgId(String orgId){
		this.orgId = orgId;
	}
	 
	private String orgCode = null;
	public String getOrgCode(){
		return this.orgCode;
	}
	public void setOrgCode(String orgCode){
		this.orgCode = orgCode;
	}
	 
	private String orgName = null;
	public String getOrgName(){
		return this.orgName;
	}
	public void setOrgName(String orgName){
		this.orgName = orgName;
	}

	private String description = null;
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}	

	private String abstractExp = null;
	public String getAbstractExp(){
		return this.abstractExp;
	}
	public void setAbstractExp(String abstractExp){
		this.abstractExp = abstractExp;
	}	
	
	//节点
	private List<Wf_Node> nodes = null;
	public List<Wf_Node> getNodes(){
		return this.nodes;
	}
	public void setNodes(List<Wf_Node> nodes){
		this.nodes = nodes;
	}	
	
	public Wf_Node getNode(String nodeId){
		for(int i=0;i<this.nodes.size();i++){
			Wf_Node node = this.nodes.get(i);
			if(node.getId().equals(nodeId)){
				return node;
			}
		}
		return null;
	}
	 
	//边
	private List<Wf_Link> links = null;
	public List<Wf_Link> getLinks(){
		return this.links;
	}
	public void setLinks(List<Wf_Link> links){
		this.links = links;
	}	
	
	public Wf_Link getLink(String linkId){
		for(int i=0;i<this.links.size();i++){
			Wf_Link link = this.links.get(i);
			if(link.getId().equals(linkId)){
				return link;
			}
		}
		return null;
	}
}
