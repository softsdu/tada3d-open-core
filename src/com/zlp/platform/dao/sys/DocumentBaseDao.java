package com.zlp.platform.dao.sys;
 
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException; 
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.asynService.IAsynInvokeServiceProcessor;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.expression.definition.RunAt;
import com.zlp.platform.expression.definition.ValidateResult;
import com.zlp.platform.expression.definition.Validator;
import com.zlp.platform.expression.run.IDatabaseAccess;
import com.zlp.platform.expression.run.RunResult;
import com.zlp.platform.expression.run.Runner;
import com.zlp.platform.expression.run.RuntimeUserParameter;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.model.sysmodel.DocType;
import com.zlp.platform.model.sysmodel.DocTypeCollection;
import com.zlp.platform.model.sysmodel.Sheet;
import com.zlp.platform.model.sysmodel.SheetCollection;
import com.zlp.platform.model.sysmodel.SheetPart;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.workflow.definition.NodeTypeLegend;
import com.zlp.platform.workflow.definition.OperateType;
import com.zlp.platform.workflow.definition.StepStatusType;
import com.zlp.platform.workflow.definition.TriggerType; 

public class DocumentBaseDao extends SheetBaseDao implements IDocumentBaseDao{  

	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}  

	private IAsynInvokeServiceProcessor asynInvokeServiceProcessor; 
	public void setAsynInvokeServiceProcessor(IAsynInvokeServiceProcessor asynInvokeServiceProcessor) {
		this.asynInvokeServiceProcessor = asynInvokeServiceProcessor;
	}  
	public IAsynInvokeServiceProcessor getAsynInvokeServiceProcessor() {
		return this.asynInvokeServiceProcessor;
	}  
	
	private Runner expRunner; 
	public void setExpRunner(Runner expRunner) {
		this.expRunner = expRunner;   
	}   
	
	private String timingDriveUserId = null;
	public String getTimingDriveUserId(){
		if(timingDriveUserId == null){
			timingDriveUserId = ConfigContext.getConfigMap().get(ZlpState.PLATFORM_WORKFLOW_TIMINGDRIVEUSERID);
		}
		return timingDriveUserId;
	}
	
	private Boolean checkTimingUser(String currentUserId){
		return currentUserId.equals(this.getTimingDriveUserId());
	}
	
	@Override
	public void setDBSession(Session dbSession){
		super.setDBSession(dbSession); 
		
		//执行表达式中的函数需要用到数据库连接，此处为这些函数设置数据库连接，以保证和DocumentBase里的方法使用的是同一个连接，以保证事务有效性
	    IDatabaseAccess databaseAccess = (IDatabaseAccess)ContextUtil.getBean("databaseAccess");
	    databaseAccess.setSession(dbSession);	 
	} 

	private Validator expValidator = null;
	public void setExpValidator(Validator expValidator){
		this.expValidator = expValidator;
	}

	@Override
	protected void beforeSave(INcpSession session, JSONObject requestObj) throws Exception{
		//查询回顾窗口不能编辑保存数据
		String windowType = requestObj.getString("windowType");
		switch(windowType){
			case "query":
			case "review":{
				throw new Exception("Can not save data in " + windowType + " window");
			}
		}
		
		//在定义单据（wf_docType）的时候，指定单据的orgid字段和userId字段
		
		//客户端开发函数，可以挂在菜单上，用于创建流程实例，例如createInstance(单据类型)
		//用户点击菜单后，createInstance执行，系统用户id获取其所在的orgid，
		//根据orgid（向上级递归）+单据类型，一级级获取到可以执行的流程id，让用户选择本次需要执行的流程（选择时显示出备注上流程所属组织）
		//保存时，将用户id和orgid保存到单据里，这需要在定义单据（wf_docType）的时候，指定单据的orgid字段和userId字段

		//创建流程实例
		//保存单据时，根据单据类型、单据data的id获得流程id，创建流程实例
	}
	
	private DocType getDocType(String docTypeName) throws Exception{
    	DocType docType = DocTypeCollection.getDocType(docTypeName);
    	if(docType == null){
    		throw new Exception("Not exist document type, docTypeName = " + docTypeName);
    	}
    	else {
    		return docType;
    	}
	}

	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{
 		//如果单据已经提交过(存在wf_workflow记录)
		//如果，存在有效的审批记录(==true)，但是当前节点是制单（即存在循环），那么提交时判断如果适用流程不等于当前所属流程，那么不允许提交；
		//如果，不存在有效审批记录，那么提交时判断如果适用流程不等于当前所属流程，那么不当前实例作废，重新创建实例
		//如果，不存任何审批记录（包括取回），那么直接update一下workflowid即可了，不用重新创建流程实例
		String sheetName = (String)requestObj.get("sheetName"); 
		Sheet sheet = SheetCollection.getSheet(sheetName);
		SheetPart mainPart = sheet.getMainPart();
		View view = ViewCollection.getView(mainPart.getViewName());
		String dataName = view.getDataName();
		String windowType = requestObj.getString("windowType");
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
    	DocType docType = this.getDocType(docTypeName); 
		Data docData = DataCollection.getData(dataName);
		String currentUserId = session.getUserId();
		
		if(windowType.equals("create")){		 
		    JSONObject id2RowIds = (JSONObject) resultHash.get("idValueToRowIds");
		    for(Object docIdObj : id2RowIds.keySet()){
		    	String docId = (String)docIdObj;
		        DataRow instRow = this.findAvailableInstance(docId, docType);
		        if(instRow != null){
		        	String userId = instRow.getStringValue("userid");
		        	//当前用户是单据创建人
		        	if(currentUserId.equals(userId)){
			        	String instanceId = instRow.getStringValue("instanceid");
			        	String workflowid = instRow.getStringValue("workflowid"); 
			        	//判断单据是否在开始节点上，如果是才允许修改
			        	if(this.checkIsOnNodeType(instanceId, NodeTypeLegend.start)){
				        	DataRow docRow = this.getDocDataInfo(docId, docData, docType);
				        	DataRow availableWorkflowRow = this.getAvailableWorkflow(docRow, docData, docType);
				        	String availableWorkflowId = availableWorkflowRow.getStringValue("workflowid");
				        	if(!workflowid.equals(availableWorkflowId)){
				        		//如果原来适用的workflowid和新的workflowid不同，那么重新创建instance
				        		this.disableInstance(instanceId);
					        	String orgId = docRow.getStringValue(docType.getOrgIdFieldName()); 
					        	DataTable nodeTable = this.getNodeTable(availableWorkflowId, NodeTypeLegend.start);
					        	DataRow beginNodeRow = nodeTable.getRows().get(0);
					        	String beginNodeId = beginNodeRow.getStringValue("id");
					        	String nodeStatus = beginNodeRow.getStringValue("statusname");			        	
					        	this.createInstance(docId, docType, orgId, availableWorkflowId, currentUserId, beginNodeId, nodeStatus);
					        	String abstractExp = availableWorkflowRow.getStringValue("abstractexp");
					        	DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, availableWorkflowId, docData, docId, docType);
					        	this.addStatusToResult(statusRow, resultHash);
				        	} 
				        	else{
				        		String abstractExp = availableWorkflowRow.getStringValue("abstractexp");
					        	DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, availableWorkflowId, docData, docId, docType);
					        	this.addStatusToResult(statusRow, resultHash);				        		
				        	}
			        	}
			        	else{
			        		throw new Exception("Document is not on begin node, instance userId = " + userId);
			        	}
		        	}
		        	else{
		        		throw new Exception("Can not update document, instance userId = " + userId);
		        	}
		        }
		        else {
		        	//新建的单据，需要新建wf_instance
		        	DataRow docRow = this.getDocDataInfo(docId, docData, docType);		        	
		        	DataRow availableWorkflowRow = this.getAvailableWorkflow(docRow, docData, docType);
		        	String availableWorkflowId = availableWorkflowRow.getStringValue("workflowid");
		        	String orgId = docRow.getStringValue(docType.getOrgIdFieldName()); 
		        	DataTable nodeTable = this.getNodeTable(availableWorkflowId, NodeTypeLegend.start);
		        	DataRow beginNodeRow = nodeTable.getRows().get(0);
		        	String beginNodeId = beginNodeRow.getStringValue("id");
		        	String nodeStatus = beginNodeRow.getStringValue("statusname");			        	
		        	String instanceId = this.createInstance(docId, docType, orgId, availableWorkflowId, currentUserId, beginNodeId, nodeStatus);
		        	String abstractExp = availableWorkflowRow.getStringValue("abstractexp");
		        	DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, availableWorkflowId, docData, docId, docType); 
		        	this.addStatusToResult(statusRow, resultHash);
		        } 
		    }
		} 
	}	  
	private void addStatusToResult(DataRow statusRow, HashMap<String, Object> resultHash) throws UnsupportedEncodingException{
		JSONObject instanceJson = new JSONObject();
		instanceJson.put("instanceId", statusRow.getStringValue("id"));
		instanceJson.put("docId", statusRow.getStringValue("docid"));
		instanceJson.put("abstractNote", CommonFunction.encode(statusRow.getStringValue("abstractnote")));
		instanceJson.put("currentStatus", CommonFunction.encode(statusRow.getStringValue("currentstatus")));
		instanceJson.put("currentNodes", statusRow.getStringValue("currentnodes")); 
		instanceJson.put("isEnd", statusRow.getStringValue("isend"));
		instanceJson.put("isBegin", statusRow.getStringValue("isbegin"));
		instanceJson.put("workflowId", statusRow.getStringValue("workflowid"));
		resultHash.put("instance", instanceJson);
	}

	@Override
	protected void beforeDelete(INcpSession session, JSONObject requestObj) throws Exception{ 
		//删除流程实例
		//需要查询审批记录，如果除创建人外，尚未有人处理过，那么允许删除
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		DocType docType = this.getDocType(docTypeName);
		String currentUserId = session.getUserId();

		JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");    
		for(Object docIdObj : rowIdToIdValues.values().toArray()){
			String docId = (String)docIdObj;
			//两个终端同时删除多条时，此处存在死锁风险
        	this.lockDocData(docId, docType); 
			DataRow instRow = this.findAvailableInstance(docId, docType);

	        if(instRow != null){
	        	String userId = instRow.getStringValue("userid");
	        	
	        	//当前用户是单据创建人
	        	if(currentUserId.equals(userId)){
		        	String instanceId = instRow.getStringValue("instanceid"); 
		        	//判断单据是否在开始节点上，如果是才允许修改
		        	if(this.checkIsOnNodeType(instanceId, NodeTypeLegend.start)){ 
		        		//可以继续操作
		        	}
		        	else{
		        		throw new Exception("Document is not on begin node, instance userId = " + userId);
		        	}
	        	}
	        	else{
	        		throw new Exception("Can not update document, instance userId = " + userId);
	        	}
	        }
	        else{
        		throw new Exception("Can not find instance, docId = " + docId +", docTypeName = " + docTypeName);
	        }
		}		 
	}
	
	@Override
	protected HashMap<String, Object> deleteCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		//不真正删除，只是打标记
		String sheetName = (String)requestObj.get("sheetName");  
		JSONObject deleteRowsJSON = requestObj.getJSONObject("deleteRows");   
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		DocType docType = this.getDocType(docTypeName);
		String isDeletedFieldName = docType.getIsDeletedFieldName();
		Sheet sheet = SheetCollection.getSheet(sheetName);
		SheetPart mainPart = sheet.getMainPart(); 
		View mainView = ViewCollection.getView(mainPart.getViewName());
		String mainDataName = mainView.getDataName();
		Data mainData = DataCollection.getData(mainDataName);  
		if(deleteRowsJSON.size() != 0){
			for(Object mainIdValueObj : deleteRowsJSON.values().toArray()){ 
				String mainIdValue = (String)mainIdValueObj;
				HashMap<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(isDeletedFieldName, "Y");
				this.dBParserAccess.updateByData(this.getDBSession(), mainData, fieldValues, mainIdValue);
				DataRow instRow = this.findAvailableInstance(mainIdValue, docType);
				String instanceId = instRow.getStringValue("instanceid");
				this.disableInstance(instanceId);
			}
		}  
	    HashMap<String, Object> resultHash=new HashMap<String, Object>();
	    return resultHash;
	}

	@Override
	protected void afterDelete(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{	

	} 

	//根据节点类型，获取流程节点
	private DataTable getNodeTable(String workflowId, NodeTypeLegend nodeType) throws SQLException{
		String nSql = "select n.id as id, n.statusname as statusname from wf_node n where n.workflowid = " + SysConfig.getParamPrefix() + "workflowid and n.nodetype = " +SysConfig.getParamPrefix() + "nodetype" ;

		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("workflowid", workflowId);
		p2vs.put("nodetype", nodeType.toString());

		List<String> alias = new ArrayList<String>(); 
		alias.add("id");
		alias.add("statusname");
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("statusname", ValueType.String); 
		
		DataTable nodeTable = this.dBParserAccess.selectList(this.getDBSession(), nSql, p2vs, alias, fieldValueTypes);
		return nodeTable;
	}
	
	//查询数据
	@Override
	public HashMap<String, Object> select(INcpSession session, JSONObject requestObj) throws Exception { 
		beforeSelect(session, requestObj);
		HashMap<String, Object> resultObj = selectCore(session, requestObj); 
		afterSelect(session, requestObj, resultObj); 
		return resultObj;  	
	}
 
	protected void beforeSelect(INcpSession session, JSONObject requestObj) throws Exception{ 	 
	}
 
	protected void afterSelect(INcpSession session, JSONObject requestObj,  HashMap<String,Object> resultHash) throws Exception{
 	}
	
	//查询单据时，动态增加Join Sql子句
	private String getDocJoinClause(INcpSession session, HashMap<String, Object> p2vs, Data data, SelectSqlParser sqlParser, String windowType, String docTypeId){
		switch(windowType){
			case "create":{  
				String docIdField = sqlParser.getSelectExpMap(data.getIdFieldName());
				String joinClause = "left outer join wf_instance inst on " + docIdField + " = inst.docdataid and inst.doctypeid = '" + docTypeId +"' "
						+ " left outer join wf_instancestep step on step.instanceid = inst.id and step.statustype in ('passed', 'active')"
						+ " left outer join wf_node node on node.id = step.nodeid";
				return joinClause;
			} 
			case "drive":{  
				String docIdField = sqlParser.getSelectExpMap(data.getIdFieldName());
				String joinClause = "left outer join wf_instance inst on " + docIdField + " = inst.docdataid and inst.doctypeid = '" + docTypeId +"'"
						+ " left outer join wf_instancestep step on step.instanceid = inst.id and step.statustype = 'active'"
						+ " left outer join wf_instanceuser instuser on instuser.stepid = step.id"
						+ " left outer join wf_node node on node.id = step.nodeid";
				return joinClause;
			} 
			case "query":{  
				String docIdField = sqlParser.getSelectExpMap(data.getIdFieldName());
				String joinClause = "left outer join wf_instance inst on " + docIdField + " = inst.docdataid and inst.doctypeid = '" + docTypeId +"'"
					+ " left outer join wf_instancestep step on step.instanceid = inst.id and step.statustype in ('passed', 'active')"
					+ " left outer join wf_node node on node.id = step.nodeid";
				return joinClause;
			} 
			case "review":{  
				String docIdField = sqlParser.getSelectExpMap(data.getIdFieldName());
				String joinClause = "left outer join wf_instance inst on " + docIdField + " = inst.docdataid and inst.doctypeid = '" + docTypeId +"'"
						+ " left outer join wf_instancestep step on step.instanceid = inst.id and step.statustype in ('passed', 'disabled')"
						+ " left outer join wf_node node on node.id = step.nodeid"
						+ " left outer join wf_instancelog instlog on instlog.stepid = step.id";
				return joinClause;
			} 
			default:
				return "";
		}
	}

	//查询单据时，动态增加Where Sql子句
	private String getDocWhereClause(INcpSession session, HashMap<String, Object> p2vs, Data data, SelectSqlParser sqlParser, String windowType, String docTypeId){
		String currentUserId = session.getUserId();
		switch(windowType){
			case "create":{   
				String docWhereClause = "inst.isdeleted = 'N' and node.nodetype = 'start' and inst.userid = " + SysConfig.getParamPrefix() +"runtimecurrentuserid";
				p2vs.put("runtimecurrentuserid", currentUserId);
				return docWhereClause;
			} 
			case "drive":{  
				String docWhereClause = "inst.isdeleted = 'N' and node.nodetype = 'active' and instuser.userid = " + SysConfig.getParamPrefix() +"runtimecurrentuserid";
				p2vs.put("runtimecurrentuserid", currentUserId);
				return docWhereClause;
			} 
			case "query":{  
				String docWhereClause = "inst.isdeleted = 'N' and node.nodetype = 'start'"; 
				return docWhereClause;
			} 
			case "review":{  
				String docWhereClause = "inst.isdeleted = 'N' and node.nodetype = 'active' and instlog.userid = " + SysConfig.getParamPrefix() +"runtimecurrentuserid";
				p2vs.put("runtimecurrentuserid", currentUserId);
				return docWhereClause;
			} 
			default:
				return "";
		}
	}

	//查询单据时，动态增加Orderby Sql子句
	private String getDocOrderbyClause(INcpSession session, String oldOrderBy, String windowType){
		if(oldOrderBy == null || oldOrderBy.isEmpty()){
			switch(windowType){
				case "create":{   
					String orderbyClause = "inst.createtime desc";
					return orderbyClause;
				} 
				case "drive":
				case "query":{  
					String orderbyClause = "inst.lastoperatetime desc";
					return orderbyClause;
				}  
				case "review":{  
					String orderbyClause = "instlog.processtime desc";
					return orderbyClause;
				} 
				default:
					return "";
			}
		}
		else{
			return oldOrderBy;
		}
	}
	
	//查询单据
	protected HashMap<String, Object> selectCore(INcpSession session, JSONObject requestObj) throws Exception{
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		DocType docType = DocTypeCollection.getDocType(docTypeName);
		if(docType == null){
			throw new Exception("不存在的单据类型, docTypeName = " + docTypeName);
		}
		else{		 
			String dataName = (String)requestObj.get("dataName");  
 			String windowType = requestObj.getString("windowType");
			Data data = DataCollection.getData(dataName);
			SelectSqlParser sqlParser = data.getDsSqlParser();
			int currentPage = requestObj.getIntValue("currentPage");
			boolean isGetCount = requestObj.getBoolean("isGetCount");
			//boolean isGetSum = requestObj.getBoolean("isGetSum");
			int pageSize = requestObj.getIntValue("pageSize");
			JSONArray userWhereArray = requestObj.getJSONArray("where");
			JSONArray sysWhereArray = requestObj.containsKey("sysWhere") ? requestObj.getJSONArray("sysWhere") : null;
			JSONArray orderbyArray = requestObj.getJSONArray("orderby");
			String docTypeId = docType.getId();
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			String userWhere = DataBaseDao.getWhere(this.dBParserAccess, sqlParser, userWhereArray, p2vs, "and");
			String sysWhere = DataBaseDao.getWhere(this.dBParserAccess, sqlParser, sysWhereArray, p2vs, "and");
			String orderby = DataBaseDao.getOrderBy(orderbyArray);
			
			String joinClause = this.getDocJoinClause(session, p2vs, data, sqlParser, windowType, docTypeId);
			String docWhereClause = this.getDocWhereClause(session, p2vs, data, sqlParser, windowType, docTypeId);
			sysWhere = sysWhere == null || sysWhere.isEmpty() ? docWhereClause : sysWhere + " and " + docWhereClause; 
			orderby = this.getDocOrderbyClause(session, orderby, windowType);
			 
			//获取数据权限过滤条件 
			String previousData = requestObj.containsKey("previousData")?requestObj.getString("previousData"):"";
			String previousField = requestObj.containsKey("previousField")?requestObj.getString("previousField"):"";
			String popDataField = requestObj.containsKey("popDataField")?requestObj.getString("popDataField"):"";  
			String dataPurviewFilter = DataBaseDao.getDataPurviewFilter(session, this.getDBSession(), data, previousData, previousField, popDataField, p2vs);
			String allWhere = userWhere + (sysWhere.isEmpty() ? "" :( userWhere.isEmpty()? "" :" and ") + sysWhere);  
			if(!dataPurviewFilter.isEmpty()){
				allWhere = (allWhere.isEmpty() ? "" : allWhere + " and ") + "(" + dataPurviewFilter + ")";
			}
	
			HashMap<String,Object> resultHash = new HashMap<String, Object>();
			//DataTable dt = this.dBParserAccess.getDtBySqlParser( this.getDBSession(), sqlParser, currentPage, pageSize, p2vs, joinClause, allWhere, orderby);
			DataTable dt =	this.getDtBySqlParser( this.getDBSession(), sqlParser, currentPage, pageSize, p2vs, joinClause, allWhere, orderby);
			resultHash.put("table", dt.toHashMap());
			
			DataTable instanceStepTable = this.getInstanceStepTable(dt, data, docTypeId);
			JSONArray instanceStepJsonArray = new JSONArray();
			if(instanceStepTable != null){
				List<DataRow> instanceStepRows = instanceStepTable.getRows();
				for(DataRow instanceStepRow : instanceStepRows){
					JSONObject instanceStepJson = new JSONObject();
					instanceStepJson.put("stepId", instanceStepRow.getStringValue("stepid"));
					instanceStepJson.put("instanceId", instanceStepRow.getStringValue("instanceid"));
					instanceStepJson.put("workflowId", instanceStepRow.getStringValue("workflowid"));
					instanceStepJson.put("sheetName", instanceStepRow.getStringValue("sheetname"));
					instanceStepJson.put("actionPageUrl", CommonFunction.encode(instanceStepRow.getStringValue("actionpageurl")));
					instanceStepJson.put("reviewPageUrl", CommonFunction.encode(instanceStepRow.getStringValue("reviewpageurl")));
					instanceStepJson.put("docId", instanceStepRow.getStringValue("docid"));
					instanceStepJson.put("abstractNote", CommonFunction.encode(instanceStepRow.getStringValue("abstractnote")));
					instanceStepJson.put("currentStatus", CommonFunction.encode(instanceStepRow.getStringValue("currentstatus")));
					instanceStepJson.put("currentNodes", instanceStepRow.getStringValue("currentnodes"));
					instanceStepJson.put("isEnd", instanceStepRow.getStringValue("isend"));
					instanceStepJson.put("isBegin", instanceStepRow.getStringValue("isbegin"));
					instanceStepJsonArray.add(instanceStepJson);
				}
			}
			resultHash.put("instanceStepTable", instanceStepJsonArray);
			
			if(isGetCount){
				int rowCount = this.dBParserAccess.getRecordCountBySqlParser( this.getDBSession(), sqlParser, p2vs, joinClause, allWhere);		
				resultHash.put("rowCount", rowCount);
			}
			
			JSONObject docTypeJson = new JSONObject();
			docTypeJson.put("id", docType.getId());
			docTypeJson.put("sheetName", docType.getSheetName());
			docTypeJson.put("createPageUrl", docType.getCreatePageUrl());
			docTypeJson.put("queryPageUrl", docType.getQueryPageUrl());
			resultHash.put("docType", docTypeJson);
			
			return resultHash;
		}
	}
	//根据select sql配置获取数据  
	public DataTable getDtBySqlParser(Session session, SelectSqlParser sqlParser, int currentPage, int pageSize, HashMap<String, Object> p2vs, String join, String where, String orderby) throws RuntimeException{
		try {  
			String sql = sqlParser.getSqlByParts("step.id as stepid", join, where,orderby);
			int fromIndex = -1;
			int onePageRowCount = 0;
			if(currentPage > 0){
				fromIndex = (currentPage-1) * pageSize;
				onePageRowCount =  pageSize; 
			}
			
			List<String> alias = sqlParser.getSelectAlias();
			List<String> aliasWithStepId = new ArrayList<String>();
			aliasWithStepId.addAll(alias);
			aliasWithStepId.add("stepid");
			
			Map<String, ValueType> fieldValueTypes = sqlParser.getFieldTypeMaps();
			Map<String, ValueType> fieldValueTypesWithStepId = new HashMap<String, ValueType>();
			fieldValueTypesWithStepId.putAll(fieldValueTypes);
			fieldValueTypesWithStepId.put("stepid", ValueType.String);
			 
		   	DataTable dt = this.dBParserAccess.selectList(session, sql, p2vs, aliasWithStepId, fieldValueTypesWithStepId, fromIndex, onePageRowCount);
 
			return dt;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException("can not get datatable by SelectSqlParser.\r\n" + ex.getMessage());
		}		
	}
	
	//设置当前单据的所在节点，可能是退回至、也可能是流转至的
	private String toNode(DataRow nodeRow, String instanceId, String createUserId, String docId, Data docData, DocType docType, NodeTypeLegend nodeType) throws Exception{
		switch(nodeType){
			case start: {
				String nodeId = nodeRow.getStringValue("id");
				return this.createStep(instanceId, nodeId, 1, new String[]{ createUserId }, null);
			}
			case active: {		
				
				Date timingProcessTime = this.runTimingExpNode(nodeRow, docData, docId, docType);
				
				int needTicketNum = this.runTicketExpNode(nodeRow, docData, docId, docType);
				
				String[] userIds = this.runUserExpNode(nodeRow, docData, docId, docType);
				if(userIds == null || userIds.length < needTicketNum){
					throw new Exception("Can not get more than " + needTicketNum + " user for wf_instanceuser");
				}
				else{
					String nodeId = nodeRow.getStringValue("id");
					return this.createStep(instanceId, nodeId, needTicketNum, userIds, timingProcessTime); 
				}
			}
			case endParallel:
			case end:
			case judge:
			case startParallel: {
				String nodeId = nodeRow.getStringValue("id");
				return this.createStep(instanceId, nodeId, 1, null, null);
			}
			default:{
				throw new Exception("Error node type, nodeType = " + nodeType.toString() + ", instanceId = " +instanceId);
			}
		}
	}
	
	//根据单据id和类型，获取instance信息，共获取单据列表信息时使用
	private DataTable getInstanceTable(DataTable docDt, Data docData, String docTypeId) throws SQLException{

		List<DataRow> docRows = docDt.getRows();
		
		if(docRows.size() > 0) {
			String instSql = "select inst.id as id,"
					+ " inst.workflowid as workflowid,"
					+ " inst.docdataid as docid,"
					+ " inst.abstractnote as abstractnote,"
					+ " inst.currentstatus as currentstatus,"
					+ " inst.currentnodes as currentnodes,"
					+ " inst.isend as isend,"
					+ " inst.isbegin as isbegin,"
					+ " inst.createtime as createtime" 
					+ " from wf_instance inst"
					+ " where doctypeid = " + SysConfig.getParamPrefix() + "doctypeid" ;
	
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
	
			List<String> alias = new ArrayList<String>(); 
			alias.add("id");
			alias.add("workflowid");
			alias.add("docid");
			alias.add("abstractnote");
			alias.add("currentstatus");
			alias.add("currentnodes");
			alias.add("isend");
			alias.add("isbegin"); 
			alias.add("createtime"); 
			
			Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
			fieldValueTypes.put("id", ValueType.String);
			fieldValueTypes.put("workflowid", ValueType.String);
			fieldValueTypes.put("docid", ValueType.String);
			fieldValueTypes.put("abstractnote", ValueType.String);
			fieldValueTypes.put("currentstatus", ValueType.String);
			fieldValueTypes.put("currentnodes", ValueType.String);
			fieldValueTypes.put("isbegin", ValueType.Boolean); 
			fieldValueTypes.put("createtime", ValueType.Time); 
			
			
			int rowCount = docRows.size();
			String idFieldName = docData.getIdFieldName();
			List<String> allDocIds = new ArrayList<String>();
			for(int i=0;i<rowCount;i++){
				DataRow docRow = docRows.get(i);
				String docId = docRow.getStringValue(idFieldName);
				allDocIds.add(docId);
			} 
	    	DataTable dt = null;  
	        StringBuilder docIdsStr = new StringBuilder();
	        for(int i=0; i<rowCount; i++){
	        	String docId = allDocIds.get(i);
	        	String pName = "docdataid" + i;
	        	docIdsStr.append((docIdsStr.length()==0? "":" or ") + "inst.docdataid = " + SysConfig.getParamPrefix() + pName);
	        	p2vs.put(pName, docId);
	        	//100个为一组，从数据库中取数
	        	if( (i != 0 &&i % 100 == 0) || (i == rowCount -1)){
	        		p2vs.put("doctypeid", docTypeId);
	        		String instQuerySql = instSql + " and (" + docIdsStr.toString() + ")";
	        		DataTable partDt = this.dBParserAccess.selectList(getDBSession(), 
	        				instQuerySql, 
	        				p2vs, 
	        				alias, 
	        				fieldValueTypes);
	        		if(dt == null){
	        			dt = partDt;
	        		}
	        		else{
	        			for(DataRow row : partDt.getRows()){
	        				dt.addRow(row);
	        			}
	        		}
	        		docIdsStr = new StringBuilder();
	        		p2vs.clear();
	        	}
	        }
	        return dt;
		}
		else{
			return null;
		}
	}
	
	//根据单据id和类型，获取instance信息，共获取单据列表信息时使用
	private DataTable getInstanceStepTable(DataTable docDt, Data docData, String docTypeId) throws SQLException{

		List<DataRow> docRows = docDt.getRows();
		
		if(docRows.size() > 0) {

			int rowCount = docRows.size(); 
			List<String> allStepIds = new ArrayList<String>();
			for(int i=0;i<rowCount;i++){
				DataRow docRow = docRows.get(i);
				String stepId = docRow.getStringValue("stepid");
				allStepIds.add(stepId);
			}  
	        StringBuilder stepIdsStr = new StringBuilder();
	        for(int i=0; i<rowCount; i++){
	        	String stepId = allStepIds.get(i); 
	        	stepIdsStr.append((stepIdsStr.length() == 0? "":", ") + "'" + stepId + "'");   
	        }
	        
			String instSql = "select inst.id as instanceid,"
					+ " step.id as stepid,"
					+ " node.actionpageurl as actionpageurl,"
					+ " node.reviewpageurl as reviewpageurl,"
					+ " inst.workflowid as workflowid,"
					+ " inst.docdataid as docid,"
					+ " inst.abstractnote as abstractnote,"
					+ " inst.currentstatus as currentstatus,"
					+ " inst.currentnodes as currentnodes,"
					+ " inst.isend as isend,"
					+ " inst.isbegin as isbegin,"
					+ " inst.createtime as createtime,"
					+ " dc.sheetname as sheetname"
					+ " from wf_instancestep step"
					+ " left outer join wf_instance inst on inst.id = step.instanceid"
					+ " left outer join wf_doctype dc on dc.id = inst.doctypeid"
					+ " left outer join wf_node node on node.id = step.nodeid"
					+ " where step.id in (" + stepIdsStr + ")" ;
	
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
	
			List<String> alias = new ArrayList<String>(); 
			alias.add("instanceid");
			alias.add("stepid");
			alias.add("actionpageurl");
			alias.add("reviewpageurl");
			alias.add("workflowid");
			alias.add("docid");
			alias.add("abstractnote");
			alias.add("currentstatus");
			alias.add("currentnodes");
			alias.add("isend");
			alias.add("isbegin"); 
			alias.add("createtime"); 
			alias.add("sheetname"); 
			
			Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
			fieldValueTypes.put("instanceid", ValueType.String);
			fieldValueTypes.put("stepid", ValueType.String);
			fieldValueTypes.put("actionpageurl", ValueType.String);
			fieldValueTypes.put("reviewpageurl", ValueType.String);
			fieldValueTypes.put("workflowid", ValueType.String);
			fieldValueTypes.put("docid", ValueType.String);
			fieldValueTypes.put("abstractnote", ValueType.String);
			fieldValueTypes.put("currentstatus", ValueType.String);
			fieldValueTypes.put("currentnodes", ValueType.String);
			fieldValueTypes.put("isbegin", ValueType.Boolean); 
			fieldValueTypes.put("createtime", ValueType.Time);
			fieldValueTypes.put("sheetname", ValueType.Time); 

			DataTable stepTable = this.dBParserAccess.selectList(this.getDBSession(), instSql, p2vs, alias, fieldValueTypes); 
	        return stepTable;
		}
		else{
			return null;
		}
	}
	
	//根据单据id和类型，获取instance信息，共获取单据列表信息时使用
	private DataRow getInstance(String instanceId) throws SQLException{  
		String instSql = "select inst.id as id,"
			+ " inst.workflowid as workflowid,"
			+ " inst.docdataid as docid,"
			+ " inst.doctypeid as doctypeid,"
			+ " inst.abstractnote as abstractnote,"
			+ " inst.currentstatus as currentstatus,"
			+ " inst.currentnodes as currentnodes,"
			+ " inst.isend as isend,"
			+ " inst.isbegin as isbegin "
			+ " from wf_instance inst"
			+ " where inst.id = " + SysConfig.getParamPrefix() + "instanceid" ;

		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("instanceid", instanceId);

		List<String> alias = new ArrayList<String>(); 
		alias.add("id");
		alias.add("workflowid");
		alias.add("docid");
		alias.add("doctypeid");
		alias.add("abstractnote");
		alias.add("currentstatus");
		alias.add("currentnodes");
		alias.add("isend");
		alias.add("isbegin"); 
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("workflowid", ValueType.String);
		fieldValueTypes.put("docid", ValueType.String);
		fieldValueTypes.put("doctypeid", ValueType.String);
		fieldValueTypes.put("abstractnote", ValueType.String);
		fieldValueTypes.put("currentstatus", ValueType.String);
		fieldValueTypes.put("currentnodes", ValueType.String);
		fieldValueTypes.put("isend", ValueType.Boolean);
		fieldValueTypes.put("isbegin", ValueType.Boolean); 
		 
		DataTable instTable = this.dBParserAccess.selectList(this.getDBSession(), instSql, p2vs, alias, fieldValueTypes);
		List<DataRow> instRows = instTable.getRows();
		if(instRows.size() == 0 ){
			return null;
		}
		else{
			return instRows.get(0);
		}
	}

	//获取instancestep
	private DataRow getStepRow(String stepId) throws Exception{
		String stepSql = "select ist.id as stepid,"
				+ " ist.instanceid as instanceid,"
				+ " ist.nodeid as nodeid,"
				+ " ist.statustype as statustype," 
				+ " ist.outtime as outtime,"
				+ " ist.timingprocesstime as timingprocesstime,"
				+ " ist.needticketnum as needticketnum"
				+ " from wf_instancestep ist"
				+ " where ist.id = " + SysConfig.getParamPrefix() + "stepid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("stepid", stepId); 

		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("instanceid");
		alias.add("nodeid");
		alias.add("statustype"); 
		alias.add("outtime");
		alias.add("timingprocesstime");
		alias.add("needticketnum");  
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("instanceid", ValueType.String);
		fieldValueTypes.put("nodeid", ValueType.String);
		fieldValueTypes.put("statustype", ValueType.String); 
		fieldValueTypes.put("outtime", ValueType.Time);
		fieldValueTypes.put("timingprocesstime", ValueType.Time);
		fieldValueTypes.put("needticketnum", ValueType.Decimal);  
		
		DataTable stepTable = this.dBParserAccess.selectList(this.getDBSession(), stepSql, p2vs, alias, fieldValueTypes);
		List<DataRow> stepRows = stepTable.getRows();
		if(stepRows.size() == 0){
			throw new Exception("Can not get instance step row, stepId = " + stepId);
		}
		else{
			return stepRows.get(0);
		} 
	}
	//已经获得了几票
	private int getStepExistTicketCount(String stepId) throws Exception{
		String stepLogSql = "select count(il.id) as steplogcount from wf_instancelog il where il.stepid = " + SysConfig.getParamPrefix() + "stepid and il.isdisabled = 'N'";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("stepid", stepId); 

		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("nodeid");
		alias.add("needticketnum");  
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("nodeid", ValueType.String);
		fieldValueTypes.put("needticketnum", ValueType.Decimal);  
		
		int ticketCount = ((BigInteger)this.dBParserAccess.selectOne(this.getDBSession(), stepLogSql, p2vs)).intValue();
		return ticketCount;
	}
	
	//判断单据是否还在此节点上
	private DataRow getActiveStepByNode(String instanceId, String nodeId) throws Exception{

		String stepSql = "select ist.id as stepid,"
				+ " ist.instanceid as instanceid,"
				+ " ist.nodeid as nodeid,"
				+ " ist.statustype as statustype," 
				+ " ist.outtime as outtime,"
				+ " ist.timingprocesstime as timingprocesstime,"
				+ " ist.needticketnum as needticketnum"
				+ " from wf_instancestep ist"
				+ " where ist.nodeid = " +SysConfig.getParamPrefix() + "nodeid"
				+ " and (ist.statustype ='active'or ist.statustype = 'suspended' or ist.statustype = 'asynProcessing' or ist.statustype = 'asynError' or ist.statustype = 'asynSucceed')" 
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("nodeid", nodeId);
		p2vs.put("instanceid", instanceId);

		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("instanceid");
		alias.add("nodeid");
		alias.add("statustype"); 
		alias.add("outtime");
		alias.add("timingprocesstime");
		alias.add("needticketnum");  
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("instanceid", ValueType.String);
		fieldValueTypes.put("nodeid", ValueType.String);
		fieldValueTypes.put("statustype", ValueType.String); 
		fieldValueTypes.put("outtime", ValueType.Time);
		fieldValueTypes.put("timingprocesstime", ValueType.Time);
		fieldValueTypes.put("needticketnum", ValueType.Decimal);  
		
		DataTable stepTable = this.dBParserAccess.selectList(this.getDBSession(), stepSql, p2vs, alias, fieldValueTypes);
		List<DataRow> stepRows = stepTable.getRows();
		if(stepRows.size() == 0){
			return null;
		}
		else{
			return stepRows.get(0);
		}  
	}
	
	//判断是否可以执行定时处理
	private boolean checkCanTimingDrive(String currentUserId, DataRow stepRow) throws Exception{
		if(this.checkTimingUser(currentUserId)){
			String statusTypeStr = stepRow.getStringValue("statustype");
			StepStatusType statusType = (StepStatusType)Enum.valueOf(StepStatusType.class, statusTypeStr);
			if(statusType == StepStatusType.asynSucceed){
				//异步执行完成，可以允许timingUser用户自动流转单据
				return true;
			}
			else {
				Date timingProcessTime = stepRow.getDateTimeValue("timingprocesstime");
				Date currentTime = new Date();
				if(timingProcessTime != null){
					return timingProcessTime.before(currentTime);
				}
				else{
					throw new Exception("It is not a timing drive step.");
				}
			}
		} 
		else{
			return false;
		}
	}
	
	//判断userid目前是在stepid环节上的审批人
	private boolean checkActiveUser(String instanceId, String stepId, String userId, TriggerType triggerType, NodeTypeLegend nodeType) throws Exception{
		switch(nodeType){
			case active:
			case start:{
				switch(triggerType){
					case clientManual:{
						String userSql = "select count(iu.id) as instusercount from wf_instanceuser iu where iu.stepid = " +SysConfig.getParamPrefix() + "stepid and iu.instanceid = " + SysConfig.getParamPrefix() + "instanceid and iu.userid = " + SysConfig.getParamPrefix() + "userid";
						HashMap<String, Object> p2vs = new HashMap<String, Object>();
						p2vs.put("stepid", stepId);
						p2vs.put("instanceid", instanceId);
						p2vs.put("userid", userId);
						int instUserCount = ((BigInteger)this.dBParserAccess.selectOne(this.getDBSession(), userSql, p2vs)).intValue();
						return instUserCount > 0;
					}
					
					//如果是自动节点，不需要判断当前用户和审批人是否匹配 
					case serverAuto:
						return true;
						
					//如果是外部调用，或者定时触发，那么需要专门指定一个用户用来运行定时和外部触发的流转，此处先不实现。
					case externalInvoke:
						throw new Exception("触发类型为" + triggerType.toString() + ", 不可手工调用!");
					default:
						return false;
				}
			} 
			default:{
				return true;
			}
		}
	}
	
	//获取单据内容信息
	private DataRow getDocDataInfo(String docId, Data docData, DocType docType) throws Exception{
		DataTable docDt = this.dBParserAccess.getDtById(this.getDBSession(), docData, docId);
		List<DataRow> docRows = docDt.getRows();
		if(docRows.size() == 0){
			throw new Exception("Can not find document info,  docId = " +docId +", dataName = " + docData.getName() +", docType = " +docType.getName());
		}
		else{
			DataRow docRow = docRows.get(0);
			return docRow;
		}
	}

	//判断边条件
	private boolean checkLinkCondition(DataRow linkRow,HashMap<String, ValueType> fieldTypes, List<RuntimeUserParameter> parameters) throws Exception{
		String linkId = linkRow.getStringValue("linkid");
		String toNodeId = linkRow.getStringValue("tonodeid");
		String conditionExp = linkRow.getStringValue("conditionexp");
		return this.checkLinkCondition(linkId, toNodeId, conditionExp, fieldTypes, parameters);
	}

	//判断边条件
	private void getRunParameters(DataRow docRow, Data docData, HashMap<String, ValueType> fieldTypes, List<RuntimeUserParameter> parameters) throws Exception{
		 HashMap<String, DataField> allFields = docData.getDataFields();
		 for(String fieldName : allFields.keySet()){
			 DataField field = allFields.get(fieldName);
			 fieldTypes.put(fieldName, field.getValueType());
			 RuntimeUserParameter p = new RuntimeUserParameter(fieldName, docRow.getValue(fieldName));
			 parameters.add(p);
		 }
	}

	//判断边条件
	private boolean checkLinkCondition(String linkId, String toNodeId, String conditionExp, HashMap<String, ValueType> fieldTypes, List<RuntimeUserParameter> parameters) throws Exception{
		if(conditionExp != null && !conditionExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(conditionExp, fieldTypes, RunAt.Server, ValueType.Boolean);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					return (boolean)runResult.getValue() == true;
				}
				else{
					throw new Exception("Run link expression error, linkid = " +linkId);
				}
			}
			else{
				throw new Exception("Validate link expression error, linkid = " +linkId);
			}
		}
		else{
			return true;
		}
	}  
	
	//刷新纪录下instance的当前状态，即修改wf_instance的currentstatus和currentnodes
	private DataRow refreshInstanceStatus(String instanceId, String workflowId, Data docData, String docId, DocType docType) throws Exception{
		DataRow workflowRow = this.getWorkflow(workflowId);
		String abstractExp = workflowRow.getStringValue("abstractexp");
		return refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docId, docType);
	}  
	
	//刷新纪录下instance的当前状态，即修改wf_instance的currentstatus和currentnodes
	private DataRow refreshInstanceStatus(String instanceId, String abstractExp, String workflowId, Data docData, String docId, DocType docType) throws Exception{
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		DataRow statusRow = new DataRow();
 
		String abstractNote = "";	
		{
			if(abstractExp != null && !abstractExp.trim().isEmpty()){
				ValidateResult validateResult = this.expValidator.validateExp(abstractExp, fieldTypes, RunAt.Server, ValueType.String);
				if(validateResult.getSucceed()){
					RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
					if(runResult.getSucceed()){
						abstractNote = (String)runResult.getValue();
					}
					else{
						throw new Exception("Run abstractExp expression error, workflowId = " +workflowId + ", instanceId = " + instanceId + ". \r\n" + runResult.getError());
					}
				}
				else{
					throw new Exception("Validate abstractExp expression error, workflowId = " +workflowId + ", instanceId = " + instanceId + ". \r\n" + validateResult.getError());
				}
			} 
		}

		String currentStatus = "";
		String currentNodes = "";
		boolean isBegin = false;
		boolean isEnd = false;
		{
			String currentStepSql = "select ist.id as stepid,"
				+ " n.id as nodeid,"
				+ " n.statusname as statusname,"
				+ " ist.statustype as statustype,"
				+ " n.nodetype as nodetype"
				+ " from wf_instancestep ist"
				+ " left outer join wf_node n on n.id = ist.nodeid"
				+ " where (ist.statustype ='active' or ist.statustype = 'suspended' or ist.statustype = 'asynProcessing' or ist.statustype = 'asynError' or ist.statustype = 'asynSucceed')" 
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("instanceid", instanceId); 
	
			List<String> alias = new ArrayList<String>();
			alias.add("stepid");
			alias.add("nodeid");
			alias.add("statusname");
			alias.add("statustype"); 
			alias.add("nodetype"); 
			
			HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
			fieldValueTypes.put("stepid", ValueType.String);
			fieldValueTypes.put("nodeid", ValueType.String);
			fieldValueTypes.put("statusname", ValueType.String); 
			fieldValueTypes.put("statustype", ValueType.String); 
			fieldValueTypes.put("nodetype", ValueType.String); 
			
			DataTable stepNodeTable = this.dBParserAccess.selectList(this.getDBSession(), currentStepSql, p2vs, alias, fieldValueTypes);
			List<DataRow> stepNodeRows = stepNodeTable.getRows();
			for(DataRow stepNodeRow : stepNodeRows){
				String statusName = stepNodeRow.getStringValue("statusname");
				String statusTypeStr = stepNodeRow.getStringValue("statustype");
				StepStatusType statusType = (StepStatusType)Enum.valueOf(StepStatusType.class, statusTypeStr);
				String statusDescription = "";
				switch(statusType){
					case suspended:
					case active:
					case passed:
					case disabled:
						break;
					case asynProcessing:{
						statusDescription = "(正在执行异步调用)";
					}
					break;
					case asynSucceed:{
						statusDescription = "(异步执行成功)";
					}
					case asynError:{
						statusDescription = "(异步执行出错)";
					}
				}
				
				currentStatus = currentStatus + (currentStatus.isEmpty() ? "" : ";" ) + statusName + statusDescription;
				
				String nodeId = stepNodeRow.getStringValue("nodeid");
				currentNodes =   currentNodes + nodeId + ";";

				String nodeTypeStr = stepNodeRow.getStringValue("nodetype");
				NodeTypeLegend nodeType = (NodeTypeLegend)Enum.valueOf(NodeTypeLegend.class, nodeTypeStr);
				isBegin = nodeType == NodeTypeLegend.start;
				isEnd = nodeType == NodeTypeLegend.end;
			}
		}
		
		{
			String updateInstanceSql = "update wf_instance set"
					+" abstractnote = " + SysConfig.getParamPrefix() + "abstractnote,"
					+" lastoperatetime = " + SysConfig.getParamPrefix() + "lastoperatetime,"
					+ " currentstatus = " + SysConfig.getParamPrefix() + "currentstatus,"
					+ " currentnodes = " + SysConfig.getParamPrefix() +"currentnodes,"
					+ " isbegin = " + SysConfig.getParamPrefix() + "isbegin,"
					+ " isend = " + SysConfig.getParamPrefix() + "isend,"
					+ " endtime = " + SysConfig.getParamPrefix() + "endtime "
					+ " where id = " + SysConfig.getParamPrefix() + "instanceid";
			Date nowTime = new Date(); 
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("abstractnote", abstractNote);
			p2vs.put("lastoperatetime", nowTime);
			p2vs.put("currentstatus", currentStatus);
			p2vs.put("currentnodes", currentNodes);
			p2vs.put("isbegin", isBegin ? "Y" : "N");
			p2vs.put("isend", isEnd ? "Y" : "N");
			p2vs.put("endtime", isEnd ? nowTime : null);
			p2vs.put("instanceid", instanceId);
			this.dBParserAccess.update(this.getDBSession(), updateInstanceSql, p2vs);

			statusRow.setValue("id", instanceId);
			statusRow.setValue("docid", docId);
			statusRow.setValue("abstractnote", abstractNote);
			statusRow.setValue("currentstatus", currentStatus);
			statusRow.setValue("currentnodes", currentNodes);
			statusRow.setValue("workflowid", workflowId);
			statusRow.setValue("isend", isEnd ? "Y" : "N");
			statusRow.setValue("isbegin", isBegin ? "Y" : "N");
			return statusRow;
		}		
	}
	
	//退回到之前的步骤时，将当前步骤的instancestep和instanceuser作废
	private void disableStep(String instanceId, String stepId, Data docData, String docId, DocType docType) throws Exception{		
		{
			//wf_instancestep作废
			String disableStepSql = "update wf_instancestep set" 
					+ " statustype = " + SysConfig.getParamPrefix() + "statustype,"
					+ " outtime = " + SysConfig.getParamPrefix() + "outtime"
					+ " where instanceid = " + SysConfig.getParamPrefix() + "instanceid"
					+ " and id = " + SysConfig.getParamPrefix() + "stepid";
			HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
			p2vs.put("statustype", StepStatusType.disabled.toString());
			p2vs.put("instanceid", instanceId);
			p2vs.put("stepid", stepId);
			p2vs.put("outtime", new Date());
			this.dBParserAccess.update(this.getDBSession(), disableStepSql, p2vs);
		}
		{
			//wf_instanceuser作废
			String deleteInstanceUserSql = "delete from wf_instanceuser"
					+ " where instanceid = " + SysConfig.getParamPrefix() + "instanceid"
					+ " and stepid = " + SysConfig.getParamPrefix() + "stepid";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("instanceid", instanceId);
			p2vs.put("stepid", stepId);
			this.dBParserAccess.delete(this.getDBSession(), deleteInstanceUserSql, p2vs);
		} 
		{
			//wf_instancelog作废
			String disableLogSql = "update wf_instancelog set isdisabled = 'Y'"
					+ " where instanceid = " + SysConfig.getParamPrefix() + "instanceid"
					+ " and stepid = " + SysConfig.getParamPrefix() + "stepid";
			HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
			p2vs.put("instanceid", instanceId);
			p2vs.put("stepid", stepId);
			this.dBParserAccess.update(this.getDBSession(), disableLogSql, p2vs);
		}
		{
			//如果尚有未执行的invoke任务，那么放弃任务
			String disableInvokeSql = "update ais_invoke set statustype = 'deleted'"
				+ " where fromname = 'workflow' and fromid = " +SysConfig.getParamPrefix() + "stepid and (statustype = 'waiting' or statustype = 'running')";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();  
			p2vs.put("stepid", stepId);
			this.dBParserAccess.update(this.getDBSession(), disableInvokeSql, p2vs);
		}
	}
	
	private String createInstanceLog(String instanceId, String stepId, String nodeId, String userId, String note, OperateType operateType){
		Data instLogData = DataCollection.getData("wf_InstanceLog");
		Date nowTime = new Date();
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("userid", userId);
		fieldValues.put("nodeid", nodeId);
		fieldValues.put("note", note);
		fieldValues.put("processtime", nowTime);
		fieldValues.put("stepid", stepId);
		fieldValues.put("operatetype", operateType.toString());
		fieldValues.put("isdisabled", "N");
		fieldValues.put("instanceid", instanceId);
		String instanceLogId = this.dBParserAccess.insertByData(this.getDBSession(), instLogData, fieldValues);
		
		String deleteInstanceUserSql = "delete from wf_instanceuser where stepid = " +SysConfig.getParamPrefix() + "stepid and userid = " + SysConfig.getParamPrefix() + "userid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("userid", userId);
		p2vs.put("stepid", stepId); 
		this.dBParserAccess.delete(this.getDBSession(), deleteInstanceUserSql, p2vs);
		
		return instanceLogId;
	}
	
	private void deleteInstanceUser(String stepId){
		String deleteInstanceUserSql = "delete from wf_instanceuser where stepid = " +SysConfig.getParamPrefix() + "stepid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
		p2vs.put("stepid", stepId); 
		this.dBParserAccess.delete(this.getDBSession(), deleteInstanceUserSql, p2vs);
	}
	
	//记录审批的log，并判断是否需要流转
	private boolean addInstanceLogAndCheckTicket(String docId, String workflowId, String instanceId, String stepId, String nodeId, String userId, int needTicketNum, String note, OperateType operateType) throws Exception{
		this.createInstanceLog(instanceId, stepId, nodeId, userId, note, operateType); 
		int existTicketCount = this.getStepExistTicketCount(stepId);
		if(needTicketNum == existTicketCount){
			//凑够票数，可以流转了
			this.deleteInstanceUser(stepId);
			return true;
		} 
		else{
			return false;
		}
	}
	
	//当流转的方式离开当前节点时，执行passExp以及让当前instancestep. = false
	private boolean beforeDriveLeaveNode(String instanceId, String stepId, DataRow fromNodeRow, Data docData, String docId, DocType docType) throws Exception{
		if(this.runPassExpNode(fromNodeRow, docData, docId, docType)) {
			//wf_instancestep作废，并记录离开时间
			//注意，如果是并行结束节点，会同时处理在此节点的多个step
			String nodeId = fromNodeRow.getStringValue("id");
			String disableStepSql = "update wf_instancestep set statustype = 'passed', "
					+ " outtime = " + SysConfig.getParamPrefix() + "outtime"
					+ " where instanceid = " + SysConfig.getParamPrefix() + "instanceid"
					+ " and nodeid = " + SysConfig.getParamPrefix() + "nodeid"
					+ " and (statustype = 'active' or statustype = 'suspended' or statustype = 'asynSucceed')";
			Date outTime = new Date();
			HashMap<String, Object> p2vs = new HashMap<String, Object>();  
			p2vs.put("outtime", outTime);
			p2vs.put("instanceid", instanceId);
			p2vs.put("nodeid", nodeId);
			this.dBParserAccess.update(this.getDBSession(), disableStepSql, p2vs);
			return true;
		} 
		else{
			throw new Exception("执行passExp返回false!");
		}
	}

	//运行timingExp表达式
	private Date runTimingExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		String nodeId = nodeRow.getStringValue("id");
		String timingExp = nodeRow.getStringValue("timingexp");
		if(timingExp != null && !timingExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(timingExp, fieldTypes, RunAt.Server, ValueType.Time);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					return (Date)runResult.getValue();
				}
				else{
					throw new Exception("Run timingExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate timingExp expression error, nodeId = " +nodeId + ".\r\n" + validateResult.getError());
			}
		}
		else{
			return null;
		}
	}

	//运行ticketExp表达式
	private int runTicketExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		String nodeId = nodeRow.getStringValue("id");
		String ticketExp = nodeRow.getStringValue("ticketexp");
		if(ticketExp != null && !ticketExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(ticketExp, fieldTypes, RunAt.Server, ValueType.Decimal);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					return ((BigDecimal)runResult.getValue()).intValue();
				}
				else{
					throw new Exception("Run ticketExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate ticketExp expression error, nodeId = " +nodeId + ".\r\n" + validateResult.getError());
			}
		}
		else{
			return 1;
		}
	}

	//运行userExp表达式
	private String[] runUserExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		String[] userIds = null;
		
		String nodeId = nodeRow.getStringValue("id");
		String userExp = nodeRow.getStringValue("userexp");
		
		//如果是并行开始、并行结束、开始、结束、判断节点，那么userExp必须为空
		if(userExp != null && !userExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(userExp, fieldTypes, RunAt.Server, ValueType.String);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){					
					String userStr = (String)runResult.getValue();
					if(userStr == null || userStr.isEmpty()){
						throw new Exception("无法获取下一个环节的处理人");
					}
					else{
						userIds = userStr.split(";");
					}
				}
				else{
					throw new Exception("Run userExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate userExp expression error, nodeId = " +nodeId + ".\r\n" + validateResult.getError());
			}
		}
		return userIds;
	}
	
	//运行backInExp表达式
	private boolean runBackInExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{	
		String nodeId = nodeRow.getStringValue("id");	
		String backInExp = nodeRow.getStringValue("backinexp");
		return this.runBackInExpNode(nodeId, backInExp, docData, docId, docType);
	}
	
	//运行backInExp表达式
	private boolean runBackInExpNode(String nodeId, String backInExp, Data docData, String docId, DocType docType) throws Exception{	 
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);

		if(backInExp != null && !backInExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(backInExp, fieldTypes, RunAt.Server, ValueType.Boolean);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					boolean resultValue = (boolean)runResult.getValue() == true;
					if(resultValue){
						return true;
					}
					else{
						throw new Exception("Run backInExp expression return false, nodeId = " +nodeId);
					}
				}
				else{
					throw new Exception("Run backInExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate backInExp expression error, nodeId = " +nodeId + ".\r\n" + validateResult.getError());
			}
		}
		else{
			return true;
		}
	} 
	
	//运行passExp
	private boolean runPassExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		String nodeId = nodeRow.getStringValue("id");
		String passExp = nodeRow.getStringValue("passexp");
		if(passExp != null && !passExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(passExp, fieldTypes, RunAt.Server, ValueType.Boolean);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					return (boolean)runResult.getValue() == true;
				}
				else{
					throw new Exception("Run passExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate passExp expression error, nodeId = " +nodeId+ ".\r\n" + validateResult.getError());
			}
		}
		else{
			return true;
		}
	}
	
	private HashMap<String, ValueType> getAisParameterValueTypes(String aisId) throws Exception{
		String getAisServiceSql = "select count(1) as rowCount from ais_service serv where serv.id = " + SysConfig.getParamPrefix() + "serviceid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("serviceid", aisId);
		int rowCount = ((BigInteger)this.dBParserAccess.selectOne(getDBSession(), getAisServiceSql, p2vs)).intValue();
		if(rowCount == 0){
			throw new Exception("Not exist aisService, id = " + aisId);
		}
		else{
			String getAisParameterSql = "select aissp.id as id, aissp.name as name, aissp.valuetype as valuetype"
				+ " from ais_serviceparameter aissp"
				+ " where aissp.serviceid = " + SysConfig.getParamPrefix() + "serviceid";
			HashMap<String, Object> pP2vs = new HashMap<String, Object>();
			pP2vs.put("serviceid", aisId);
			List<String> alias = new ArrayList<String>();
			alias.add("id");
			alias.add("name");
			alias.add("valuetype");
			HashMap<String, ValueType> pFieldValueTypes = new HashMap<String, ValueType>();
			pFieldValueTypes.put("id", ValueType.String);
			pFieldValueTypes.put("name", ValueType.String);
			pFieldValueTypes.put("valuetype", ValueType.String);
			DataTable pTable = this.dBParserAccess.selectList(getDBSession(), getAisParameterSql, pP2vs, alias, pFieldValueTypes);
			List<DataRow> pRows = pTable.getRows();
			HashMap<String, ValueType> pValueTypes = new HashMap<String, ValueType>();
			for(DataRow pRow: pRows){
				String pName = pRow.getStringValue("name");
				String valueTypeStr = pRow.getStringValue("valuetype");
				ValueType valueType =(ValueType)Enum.valueOf(ValueType.class, valueTypeStr);
				pValueTypes.put(pName, valueType);
			}
			return pValueTypes;
		}
		
	}
	
	//运行所有的aisParameterExp
	private HashMap<String, Object> runAisParameterExpNode(DataRow nodeRow, HashMap<String, ValueType> aisServiceParameterValueTypes, Data docData, String docId, DocType docType, String stepId) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		HashMap<String, Object> values = new HashMap<String, Object>();
		
		String aisParameterExp = nodeRow.getStringValue("aisparameterexp"); 
		try{
			JSONObject parameterExpObj = JSONProcessor.strToJSON(aisParameterExp);  
			for(Object pNameObj : parameterExpObj.keySet()){
				String pName = (String)pNameObj;
				ValueType pValueType = aisServiceParameterValueTypes.get(pName);
				String pExp = CommonFunction.decode(parameterExpObj.getString(pName));
				Object value = null;
				if(pExp != null && !pExp.trim().isEmpty()){
					ValidateResult validateResult = this.expValidator.validateExp(pExp, fieldTypes, RunAt.Server, pValueType);
					if(validateResult.getSucceed()){
						RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
						if(runResult.getSucceed()){
							value = runResult.getValue();
						}
						else{
							throw new Exception("Run passExp expression error, stepId = " + stepId + ".\r\n" + runResult.getError());
						}
					}
					else{
						throw new Exception("Validate passExp expression error, stepId = " + stepId + ".\r\n" + validateResult.getError());
					}
				} 
				values.put(pName, value);
			}
			return values;
		}
		catch(Exception ex){
			throw ex;
		}
	}
	
	//运行inExp
	private boolean runInExpNode(DataRow nodeRow, Data docData, String docId, DocType docType) throws Exception{		
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		
		String nodeId = nodeRow.getStringValue("id");
		String inExp = nodeRow.getStringValue("inexp");
		if(inExp != null && !inExp.trim().isEmpty()){
			ValidateResult validateResult = this.expValidator.validateExp(inExp, fieldTypes, RunAt.Server, ValueType.Boolean);
			if(validateResult.getSucceed()){
				RunResult runResult = expRunner.runExp(validateResult, parameters, this.getDBSession());
				if(runResult.getSucceed()){
					return (boolean)runResult.getValue() == true;
				}
				else{
					throw new Exception("Run inExp expression error, nodeId = " +nodeId + ".\r\n" + runResult.getError());
				}
			}
			else{
				throw new Exception("Validate inExp expression error, nodeId = " +nodeId + ".\r\n" + validateResult.getError());
			}
		}
		else{
			return true;
		}
	}
	
	//获取node信息
	private List<DataRow> getNodeRow(List<String> nodeIds) throws Exception{
		List<DataRow> nodeRows = new ArrayList<DataRow>();
		for(String nodeId : nodeIds){
			DataRow nodeRow = this.getNodeRow(nodeId);
			nodeRows.add(nodeRow);
		}
		return nodeRows;
	}
	
	//获取node信息
	private DataRow getNodeRow(String nodeId) throws Exception{		
		String nodeSql = "select n.id as id,"
			+ " n.name as name,"
			+ " n.nodetype as nodetype,"
			+ " n.statusname as statusname,"
			+ " n.canbackto as canbackto,"
			+ " n.triggertype as triggertype,"
			+ " n.userexp as userexp,"
			+ " n.actionpageurl as actionpageurl,"
			+ " n.ticketexp as ticketexp,"
			+ " n.workflowid as workflowid,"
			+ " n.passexp as passexp,"
			+ " n.backinexp as backinexp,"
			+ " n.inexp as inexp,"
			+ " n.timingexp as timingexp,"
			+ " n.canbackfrom as canbackfrom,"
			+ " n.aisid as aisid,"
			+ " n.aisparameterexp as aisparameterexp"
			+ " from wf_node n"
			+ " where n.id = " + SysConfig.getParamPrefix() + "nodeid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("nodeid", nodeId); 

		List<String> alias = new ArrayList<String>();
		alias.add("id");
		alias.add("name");
		alias.add("nodetype");
		alias.add("statusname");
		alias.add("canbackto");
		alias.add("triggertype");
		alias.add("userexp");
		alias.add("actionpageurl");
		alias.add("ticketexp");
		alias.add("workflowid");
		alias.add("passexp");
		alias.add("backinexp");
		alias.add("inexp");
		alias.add("timingexp");
		alias.add("canbackfrom"); 
		alias.add("aisid"); 
		alias.add("aisparameterexp"); 
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>(); 
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("name", ValueType.String);
		fieldValueTypes.put("nodetype", ValueType.String);
		fieldValueTypes.put("statusname", ValueType.String);
		fieldValueTypes.put("canbackto", ValueType.Boolean);
		fieldValueTypes.put("triggertype", ValueType.String);
		fieldValueTypes.put("userexp", ValueType.String);
		fieldValueTypes.put("actionpageurl", ValueType.String);
		fieldValueTypes.put("ticketexp", ValueType.String);
		fieldValueTypes.put("workflowid", ValueType.String);
		fieldValueTypes.put("passexp", ValueType.String);
		fieldValueTypes.put("backinexp", ValueType.String);
		fieldValueTypes.put("inexp", ValueType.String);
		fieldValueTypes.put("timingexp", ValueType.String);
		fieldValueTypes.put("canbackfrom", ValueType.Boolean); 
		fieldValueTypes.put("aisid", ValueType.String); 
		fieldValueTypes.put("aisparameterexp", ValueType.String); 
		
		DataTable nodeTable = this.dBParserAccess.selectList(this.getDBSession(), nodeSql, p2vs, alias, fieldValueTypes);
		List<DataRow> nodeRows = nodeTable.getRows();
		if(nodeRows.size() == 0){
			throw new Exception("Can not find node, nodeId = " + nodeId);
		}
		else{
			return nodeRows.get(0);
		}
	}
	
	//获取下一个节点
	private List<String> checkNextAvailableNodeIds(String docId, String instanceId, String workflowId, String stepId, String nodeId, NodeTypeLegend nodeType, DataRow docRow, Data docData) throws Exception{
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> parameters = new ArrayList<RuntimeUserParameter>();
		this.getRunParameters(docRow, docData, fieldTypes, parameters);
		List<String> toNodeIds = new ArrayList<String>();
				
		List<DataRow> linkRows = this.getNextLinkRows(workflowId, nodeId);
		switch(nodeType){
			case start:
			case active:
			case endParallel:{
				DataRow linkRow = linkRows.get(0);
				String toNodeId = linkRow.getStringValue("tonodeid");
				toNodeIds.add(toNodeId);
				return toNodeIds;
			} 
			case judge:{
				DataRow noExpLinkRow = null;
				for(DataRow linkRow : linkRows){ 
					String conditionExp = linkRow.getStringValue("conditionexp");
					if(conditionExp == null || conditionExp.trim().isEmpty()){
						noExpLinkRow = linkRow;
					}
					else{
						if(this.checkLinkCondition(linkRow, fieldTypes, parameters)){
							String toNodeId = linkRow.getStringValue("tonodeid");
							toNodeIds.add(toNodeId);
							return toNodeIds;
						}
					}
				}
				
				if(noExpLinkRow == null){
					throw new Exception("Can not get next available node, instanceId = " + instanceId); 
				}
				else{
					String toNodeId = noExpLinkRow.getStringValue("tonodeid");
					toNodeIds.add(toNodeId);	
					return toNodeIds;
				}				
			} 
			case startParallel:{ 
				for(DataRow linkRow : linkRows){ 
					String toNodeId = linkRow.getStringValue("tonodeid");
					String conditionExp = linkRow.getStringValue("conditionexp");
					if(conditionExp == null || conditionExp.trim().isEmpty()){
						toNodeIds.add(toNodeId);
					}
					else{
						if(this.checkLinkCondition(linkRow, fieldTypes, parameters)){
							toNodeIds.add(toNodeId);
						}
					}
				}
				return toNodeIds;
			}
		default:
			throw new Exception("Can not get next available node, instanceId = " + instanceId); 
		}
	}
	
	private List<DataRow> getNextLinkRows(String workflowId, String fromNodeId) throws SQLException{
		String linkSql = "select l.id as linkid, l.conditionexp as conditionexp, l.tonodeid as tonodeid from wf_link l where l.fromnodeid = " + SysConfig.getParamPrefix() +"fromnodeid and l.workflowid = " + SysConfig.getParamPrefix() + "workflowid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("fromnodeid", fromNodeId);
		p2vs.put("workflowid", workflowId);

		List<String> alias = new ArrayList<String>();
		alias.add("linkid");
		alias.add("conditionexp");
		alias.add("tonodeid");
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("linkid", ValueType.String);
		fieldValueTypes.put("conditionexp", ValueType.String);
		fieldValueTypes.put("tonodeid", ValueType.String);
		
		DataTable linkTable = this.dBParserAccess.selectList(this.getDBSession(), linkSql, p2vs, alias, fieldValueTypes);
		return linkTable.getRows();		
	}
	
	//instance作废
	private void disableInstance(String instanceId){
		{
			String disableInstanceSql = "update wf_instance set isdeleted = 'Y', lastoperatetime =" + SysConfig.getParamPrefix() + "lastoperatetime where id = " + SysConfig.getParamPrefix() +"instanceid";
			HashMap<String, Object> disableInstP2vs = new HashMap<String, Object>(); 
			disableInstP2vs.put("instanceid", instanceId);
			disableInstP2vs.put("lastoperatetime", new Date());
			this.dBParserAccess.update(this.getDBSession(), disableInstanceSql, disableInstP2vs);
		}

		{
			String disableInstanceStepSql = "update wf_instancestep set statustype = 'disabled' where instanceid = " + SysConfig.getParamPrefix() +"instanceid";
			HashMap<String, Object> disableInstStepP2vs = new HashMap<String, Object>(); 
			disableInstStepP2vs.put("instanceid", instanceId);
			this.dBParserAccess.update(this.getDBSession(), disableInstanceStepSql, disableInstStepP2vs);
		}

		{
			String deleteInstanceUserSql = "delete from wf_instanceuser where instanceid = " + SysConfig.getParamPrefix() +"instanceid";
			HashMap<String, Object> deleteInstanceUserP2vs = new HashMap<String, Object>(); 
			deleteInstanceUserP2vs.put("instanceid", instanceId);
			this.dBParserAccess.delete(this.getDBSession(), deleteInstanceUserSql, deleteInstanceUserP2vs);
		}

		{
			String disableInstanceLogSql = "update wf_instancelog set isdisabled = 'Y' where instanceid = " + SysConfig.getParamPrefix() +"instanceid";
			HashMap<String, Object> disableInstanceLogP2vs = new HashMap<String, Object>(); 
			disableInstanceLogP2vs.put("instanceid", instanceId);
			this.dBParserAccess.update(this.getDBSession(), disableInstanceLogSql, disableInstanceLogP2vs);
		}
	}
	
	//新建instance
	private String createInstance(String docId, DocType docType, String orgId, String workflowId, String userId, String nodeId, String nodeStatus) throws RuntimeException, Exception{
		Data instData = DataCollection.getData("wf_Instance");
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();  
		Date currentTime = new Date();
		fieldValues.put("userid", userId);
		fieldValues.put("createtime", currentTime);
		fieldValues.put("workflowid", workflowId);
		fieldValues.put("currentstatus", nodeStatus);
		fieldValues.put("currentnodes", nodeId);
		fieldValues.put("isend", 'N');
		fieldValues.put("isbegin", 'Y');
		fieldValues.put("orgid", orgId);
		fieldValues.put("isdeleted", 'N');
		fieldValues.put("lastoperatetime", currentTime);
		fieldValues.put("docdataid", docId);
		fieldValues.put("doctypeid", docType.getId());
		String instanceId = this.dBParserAccess.insertByData(this.getDBSession(), instData, fieldValues);
		
		this.createStep(instanceId, nodeId, 1, new String[]{ userId }, null);
		
		return instanceId;
	} 
	
	//新建instance
	private String createStep(String instanceId, String nodeId, int needTicketNum, String[] userIds, Date timingProcessTime) throws RuntimeException, Exception{
		Date currentTime =  new Date();
		Data instStepData = DataCollection.getData("wf_InstanceStep"); 
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("nodeid", nodeId);
		fieldValues.put("intime", currentTime);
		fieldValues.put("instanceid", instanceId);
		fieldValues.put("statustype", StepStatusType.active.toString()); 
		fieldValues.put("timingprocesstime", timingProcessTime);
		fieldValues.put("needticketnum", needTicketNum);
		String stepId = this.dBParserAccess.insertByData(this.getDBSession(), instStepData, fieldValues);	

		if(userIds != null){
			Data instUserData = DataCollection.getData("wf_InstanceUser");
			List<HashMap<String, Object>> allFieldValues = new ArrayList<HashMap<String, Object>>();
			for(String userId : userIds){
				HashMap<String, Object> userF2vs = new HashMap<String, Object>();
				userF2vs.put("instanceid", instanceId);
				userF2vs.put("nodeid", nodeId);
				userF2vs.put("userid", userId);
				userF2vs.put("stepid", stepId);
				userF2vs.put("createtime", currentTime);
				allFieldValues.add(userF2vs);
			}
			
			this.dBParserAccess.insertByData(this.getDBSession(), instUserData, allFieldValues);	
		}
		return stepId;
	} 

	//锁定instance，在处理单据前调用
	private void lockDocData(String docId, DocType docType) throws Exception{
		Data docData = docType.getMainData();
		String lockDataSql = "select " + docData.getIdFieldName() + " from " + docData.getSaveDest() + " where " +  docData.getIdFieldName() +" = " + SysConfig.getParamPrefix() +"docid for update";
		HashMap<String, Object> lockDataP2vs = new HashMap<String, Object>(); 
		lockDataP2vs.put("docid", docId);
		this.dBParserAccess.selectOne(this.getDBSession(), lockDataSql, lockDataP2vs);
	}

	//找到适合的流程
	private DataRow getAvailableWorkflow(String docId, Data docData, DocType docType) throws Exception{
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		return this.getAvailableWorkflow(docRow, docData, docType);
	}

	//找到适合的流程
	private DataRow getAvailableWorkflow(DataRow docRow, Data docData, DocType docType) throws Exception{ 		
		String docId = docRow.getStringValue(docData.getIdFieldName());
		
		String orgIdFieldName = docType.getOrgIdFieldName();
		String userIdFieldName = docType.getUserIdFieldName();
		
		String userId = docRow.getStringValue(userIdFieldName);
		String orgId = docRow.getStringValue(orgIdFieldName);

		if(userId == null || userId.isEmpty()){
			throw new Exception("Can not get userId from doc data row, userIdFieldName = " + userIdFieldName +",  docId = " +docId +", dataName = " + docData.getName() +", docType = " +docType.getName());
		}
		if(orgId == null || orgId.isEmpty()){
			throw new Exception("Can not get orgId from doc data row, userIdFieldName = " + userIdFieldName +",  docId = " +docId +", dataName = " + docData.getName() +", docType = " +docType.getName());
		}
		
		String availableWorkflowSql = "select wf.id as workflowid, wf.abstractexp as abstractexp from wf_workflow wf where wf.isactive = 'Y' and wf.isdeleted = 'N' and wf.doctypeid = " + SysConfig.getParamPrefix() + "doctypeid and wf.orgid = " + SysConfig.getParamPrefix() + "orgid";
		
		while(orgId != null && !orgId.isEmpty()){
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("orgid", orgId);
			p2vs.put("doctypeid", docType.getId());

			List<String> alias = new ArrayList<String>();
			alias.add("workflowid");
			alias.add("abstractexp");  
			
			HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
			fieldValueTypes.put("workflowid", ValueType.String);
			fieldValueTypes.put("abstractexp", ValueType.String);   
			
			DataTable workflowDt = this.dBParserAccess.selectList(this.getDBSession(), availableWorkflowSql, p2vs, alias, fieldValueTypes);
			List<DataRow> workflowRows = workflowDt.getRows();
			if(workflowRows.size() != 0){
				return workflowRows.get(0);
			}
			else{
				orgId = getParentOrgId(orgId);
			}
		}
		throw new Exception("Can not find available workflow, docId = " +docId +", dataName = " + docData.getName() +", docType = " +docType.getName());
	}
	//找到流程
	private DataRow getWorkflow(String workflowId) throws Exception{ 	 
		
		String availableWorkflowSql = "select wf.id as workflowid, wf.abstractexp as abstractexp from wf_workflow wf where wf.id = " + SysConfig.getParamPrefix() + "workflowid";
		 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("workflowid", workflowId); 

		List<String> alias = new ArrayList<String>();
		alias.add("workflowid");
		alias.add("abstractexp");  
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("workflowid", ValueType.String);
		fieldValueTypes.put("abstractexp", ValueType.String);   
		
		DataTable workflowDt = this.dBParserAccess.selectList(this.getDBSession(), availableWorkflowSql, p2vs, alias, fieldValueTypes);
		List<DataRow> workflowRows = workflowDt.getRows();
		if(workflowRows.size() != 0){
			return workflowRows.get(0);
		}
		else{
			throw new Exception("Can not find workflow, id = " + workflowId);
		}
	}

	//找到对应的可用的instance
	private DataRow findAvailableInstance(String docId, DocType docType) throws Exception{
		String availablInstanceSql = "select inst.id as instanceid, inst.workflowid as workflowid,inst.userid as userid from wf_instance inst where inst.isdeleted = 'N' and inst.doctypeid = " + SysConfig.getParamPrefix() + "doctypeid and inst.docdataid = " + SysConfig.getParamPrefix() + "docdataid";
		 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("docdataid", docId);
		p2vs.put("doctypeid", docType.getId());
		
		List<String> alias = new ArrayList<String>();
		alias.add("instanceid");
		alias.add("workflowid");
		alias.add("userid");
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("instanceid", ValueType.String);
		fieldValueTypes.put("workflowid", ValueType.String);
		fieldValueTypes.put("userid", ValueType.String);
		DataTable dt = this.dBParserAccess.selectList(this.getDBSession(), availablInstanceSql, p2vs, alias, fieldValueTypes);
		List<DataRow> rows = dt.getRows();
		int rowCount = rows.size();
		if(rowCount > 1){
			throw new Exception("Found " + rowCount + " instances, docId = " + docId +", docTypeId = " + docType.getId());
		}
		else{
			return rowCount == 0 ? null : rows.get(0);
		}
	}

	//判断instance是否在此类型的节点上
	private boolean checkIsOnNodeType(String instanceId, NodeTypeLegend nodeType) throws Exception{
		List<DataRow> activeStepRows = this.getAcitveStep(instanceId);
		for(DataRow stepRow : activeStepRows){
			String nodeTypeStr = stepRow.getStringValue("nodetype");
			if(nodeTypeStr.equals(nodeType.toString())){
				return true;
			}
		}
		return false;
	}


	//找到对应的可用的instancestep
	private List<DataRow> getAcitveStep(String instanceId) throws Exception{
		String activeStepSql = "select ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " ist.statustype as statustype,"
				+ " n.nodetype as nodetype,"
				+ " n.name as nodename," 
				+ " n.canbackfrom as canbackfrom"
				+ " from wf_instancestep ist"
				+ " left outer join wf_node n on n.id = ist.nodeid"
				+ " where (ist.statustype = 'suspended' or ist.statustype = 'active' or ist.statustype = 'asynSucceed' or ist.statustype = 'asynProcessing' or ist.statustype = 'asynError') and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid";
		 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("instanceid", instanceId); 
		
		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("nodeid"); 
		alias.add("statustype"); 
		alias.add("nodetype"); 
		alias.add("nodename"); 
		alias.add("canbackfrom"); 
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("nodeid", ValueType.String);  
		fieldValueTypes.put("statustype", ValueType.String);  
		fieldValueTypes.put("nodetype", ValueType.String);  
		fieldValueTypes.put("nodename", ValueType.String);  
		fieldValueTypes.put("canbackfrom", ValueType.Boolean);  
		
		DataTable dt = this.dBParserAccess.selectList(this.getDBSession(), activeStepSql, p2vs, alias, fieldValueTypes);
		List<DataRow> rows = dt.getRows();
		return rows;
	}

	//找到对应的可用的instancestep
	private boolean checkIsAcitveStep(String stepId) throws Exception{
		String activeStepSql = "select count(1) as stepCount"
				+ " from wf_instancestep ist"
				+ " where (ist.statustype = 'active' or ist.statustype = 'suspended' or ist.statustype = 'asynProcessing' or ist.statustype = 'asynSucceed' or ist.statustype = 'asynError') and ist.stepid = " + SysConfig.getParamPrefix() + "stepid";
		 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("stepid", stepId);  
		int stepCount = ((BigInteger)this.dBParserAccess.selectOne(this.getDBSession(), activeStepSql, p2vs)).intValue();
		return stepCount > 0;
	}
	
	//找到上级orgid
	private String getParentOrgId(String orgId) throws SQLException{
		String orgSql = "select org.parentid as parentid from d_org org where org.id = " +SysConfig.getParamPrefix() +"orgid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("orgid", orgId);
		String parentId = (String)this.dBParserAccess.selectOne(this.getDBSession(), orgSql, p2vs);
		return parentId;
	}
	
	//提交	
	@Override
	public HashMap<String, Object> submitWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException {  
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.submit(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}	
	}
	
	public HashMap<String, Object> submit(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			 
			beforeSubmit(session, requestObj);			
 
			HashMap<String, Object> resultHash = submitCore(session, requestObj);
 
			afterSubmit(session, requestObj, resultHash);  
			return resultHash;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
	
	protected HashMap<String, Object> submitCore(INcpSession session, JSONObject requestObj) throws Exception{   
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		String docId = requestObj.getString("docId"); 
		String note = CommonFunction.decode(requestObj.getString("note"));
		DocType docType = this.getDocType(docTypeName);
		Data docData = docType.getMainData();
		
		String currentUserId = session.getUserId();

		this.lockDocData(docId, docType);
        DataRow instRow = this.findAvailableInstance(docId, docType); 
    	String instanceId = instRow.getStringValue("instanceid");
    	String workflowId = instRow.getStringValue("workflowid");
    	String createUserId = instRow.getStringValue("userid");
    	
    	DataTable startNodeTable = this.getNodeTable(workflowId, NodeTypeLegend.start);
    	List<DataRow> startRows = startNodeTable.getRows();
    	DataRow startRow = startRows.get(0);
    	String startNodeId = startRow.getStringValue("id");
    	DataRow stepRow = this.getActiveStepByNode(instanceId, startNodeId);
    	if(stepRow == null){
    		throw new Exception("单据已不在制单节点");
    	}
    	else{
	    	String stepId = stepRow.getStringValue("stepid");
	    	String nodeId = stepRow.getStringValue("nodeid");
	    	int needTicketNum = stepRow.getIntegerValue("needticketnum");  
	
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			
	    	//判断当前用户是不是有效审批人（instanceuser里记录了单据创建人）
	    	if(this.checkActiveUser(instanceId, stepId, currentUserId, TriggerType.clientManual, NodeTypeLegend.start)){ 

				DataRow fromNodeRow = this.getNodeRow(nodeId);
				
	    		//如果是异步调用服务，那么调用之，或者检查已调用服务的状态，如果状态是asynSucceed，那么执行后续操作
	    		if(this.checkAsynInvokeService(fromNodeRow, stepRow, instanceId, workflowId, docData, currentUserId, docId, docType, stepId, resultHash)){   
		    		//增加instaneLog，并判断是否通过票数足够流转了
		    		boolean canPass = this.addInstanceLogAndCheckTicket(docId, workflowId, instanceId, stepId, nodeId, currentUserId, needTicketNum, note, OperateType.submit);
					if(canPass){				
		
						//运行passExp，并记录instanceStep.outtime
						if(this.beforeDriveLeaveNode(instanceId, stepId, fromNodeRow, docData, docId, docType)){		
							//进入之后的节点
							this.goToNextNodes(docId, docData, docType, instanceId, createUserId, workflowId, stepId, nodeId, NodeTypeLegend.start, currentUserId);
							
							DataRow workflowRow = this.getWorkflow(workflowId);
							String abstractExp = workflowRow.getStringValue("abstractexp");					
							DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docId, docType);
							this.addStatusToResult(statusRow, resultHash);		        	
						} 
					} 	
	    		}
	    	}
	    	else{
	    		throw new Exception("当前用户不是制单人, 需要制单人本人提交.");
	    	}
			return resultHash;
    	}
	}
	
	protected void beforeSubmit(INcpSession session, JSONObject requestObj) throws Exception{ 
	}
	
	protected void afterSubmit(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{ 
	}
	  
	//审批通过
	@Override
	public HashMap<String, Object> driveWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException {  
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.drive(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}	
	}
	
	public HashMap<String, Object> drive(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			 
			beforeDrive(session, requestObj);			

			HashMap<String, Object> resultHash = driveCore(session, requestObj);

			afterDrive(session, requestObj, resultHash);  
			return resultHash;
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
	
	protected HashMap<String, Object> driveCore(INcpSession session, JSONObject requestObj) throws Exception{  
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		String docId = requestObj.getString("docId");
		String stepId = requestObj.getString("stepId");
		String note = CommonFunction.decode(requestObj.getString("note"));
		DocType docType = this.getDocType(docTypeName);
		Data docData = docType.getMainData();
		
		String currentUserId = session.getUserId();
		this.lockDocData(docId, docType);
		this.driveInstance(docType, docData, docId, stepId, note, currentUserId);
		HashMap<String, Object> resultObj = new HashMap<String, Object>();
		return resultObj;
	}
	private void driveInstance(DocType docType, Data docData, String docId, String stepId, String note, String currentUserId) throws SQLException, Exception{
		this.driveInstance(docType, docData, docId, stepId, note, currentUserId, OperateType.drive);
	}
	private void driveInstance(DocType docType, Data docData, String docId, String stepId, String note, String currentUserId, OperateType operateType) throws SQLException, Exception{
		DataRow instRow = this.findAvailableInstance(docId, docType); 
    	String instanceId = instRow.getStringValue("instanceid");
    	String workflowId = instRow.getStringValue("workflowid"); 
    	String createUserId = instRow.getStringValue("userid");
    	
    	DataRow stepRow = this.getStepRow(stepId);
    	String nodeId = stepRow.getStringValue("nodeid");		
		DataRow fromNodeRow = this.getNodeRow(nodeId);
		TriggerType fromNodeTriggerType = (TriggerType)Enum.valueOf(TriggerType.class, fromNodeRow.getStringValue("triggertype"));
		NodeTypeLegend fromNodeType = (NodeTypeLegend)Enum.valueOf(NodeTypeLegend.class, fromNodeRow.getStringValue("nodetype"));
    	int needTicketNum = stepRow.getIntegerValue("needticketnum");  
    	
    	//判断当前用户是不是有效审批人
    	if(this.checkActiveUser(instanceId, stepId, currentUserId, fromNodeTriggerType, fromNodeType) || this.checkCanTimingDrive(currentUserId, stepRow)){
    		
    		//如果是异步调用服务，那么调用之，或者检查已调用服务的状态，如果状态是asynSucceed，那么执行后续操作
    		if(this.checkAsynInvokeService(fromNodeRow, stepRow, instanceId, workflowId, docData, currentUserId, docId, docType, stepId, null)){    		
	    		//增加instaneLog，并判断是否通过票数足够流转了
	    		boolean canPass = this.addInstanceLogAndCheckTicket(docId, workflowId, instanceId, stepId, nodeId, currentUserId, needTicketNum, note, operateType);
				if(canPass){
					//如果可以流转了，那把此step的其它审批人删除掉，这种情况发生在多个人审批在同一环节审批的情况，即会签，审批人数>可流转的票数
					this.deleteInstanceUser(stepId);
					
					//运行passExp，并记录instanceStep.outtime
					if(this.beforeDriveLeaveNode(instanceId, stepId, fromNodeRow, docData, docId, docType)){
						
						//进入之后的节点
						this.goToNextNodes(docId, docData, docType, instanceId, createUserId, workflowId, stepId, nodeId, fromNodeType, currentUserId);
	
						DataRow workflowRow = this.getWorkflow(workflowId);
						String abstractExp = workflowRow.getStringValue("abstractexp");					
						this.refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docId, docType);
					} 
				}  
    		}
    	}
    	else{
    		throw new Exception("当前用户不是有效审批人.");
    	}
	}
	
	//判断是否已经可以从异步节点流传到下一个节点
	private boolean checkAsynInvokeService(DataRow nodeRow, DataRow stepRow, String instanceId, String workflowId, Data docData, String userId, String docId, DocType docType, String stepId, HashMap<String, Object> resultHash) throws Exception{
		String aisId = nodeRow.getStringValue("aisid"); 
		if(aisId != null && !aisId.isEmpty()){
			String statusTypeStr = stepRow.getStringValue("statustype");
			StepStatusType statusType = (StepStatusType)Enum.valueOf(StepStatusType.class, statusTypeStr); 
			switch(statusType){
				case asynError://对于于异步执行出错的流程环节，可以允许用户再次提交
				case suspended:
				case active:{
					//执行参数表达式，获取参数值
					HashMap<String, ValueType> parameterValueTypes = this.getAisParameterValueTypes(aisId);
					HashMap<String, Object> parameterValues = this.runAisParameterExpNode(nodeRow, parameterValueTypes, docData, docId, docType, stepId);
					//调用异步操作
					this.asynInvokeServiceProcessor.getDatabaseAccess().setSession(this.getDBSession());
					String invokeId = this.asynInvokeServiceProcessor.createAsynInvoke(aisId, userId, "workflow", stepId, parameterValueTypes, parameterValues);
					
					//获取到的invokeid保存到数据库。
					String updateStepInvokeIdSql = "update wf_instancestep set "
						+ "invokeid = " + SysConfig.getParamPrefix() + "invokeid,"
						+ "statustype = " + SysConfig.getParamPrefix() + "statustype"
						+ " where id = " + SysConfig.getParamPrefix() + "stepid";
					HashMap<String, Object> p2vs = new HashMap<String, Object>();
					p2vs.put("invokeid", invokeId);
					p2vs.put("statustype", StepStatusType.asynProcessing.toString());				
					p2vs.put("stepid", stepId);					
					this.dBParserAccess.update(getDBSession(), updateStepInvokeIdSql, p2vs); 
					
					//刷新单据状态，系统会等待系统提供的轮询功能，流转单据
					DataRow statusRow = this.refreshInstanceStatus(instanceId, workflowId, docData, docId, docType);
					if(resultHash != null){
						this.addStatusToResult(statusRow, resultHash);
					}
					return false;
				}					
				case passed:
					throw new Exception("Passed step.");
				case disabled:
					throw new Exception("Disabled step.");
				case asynProcessing:
					throw new Exception("正在执行异步操作"); 
				case asynSucceed:
					//可以继续流转
					return true;
				default:
					throw new Exception("未知的状态类型, statusType = " + statusTypeStr);
			}
		}
		else{
			//如果非异步节点，那么直接通过
			return true;
		}
	}
	
	//进入之后的节点
	private void goToNextNodes(String docId, Data docData, DocType docType, String instanceId, String createUserId, String workflowId, String stepId, String nodeId, NodeTypeLegend fromNodeType, String currentUserId) throws Exception{
		DataRow docRow = this.getDocDataInfo(docId, docData, docType);
		
		//找到可以流转至的的节点
		List<String> nextNodeIds = this.checkNextAvailableNodeIds(docId, instanceId, workflowId, stepId, nodeId, fromNodeType, docRow, docData);
		for(String nextNodeId : nextNodeIds){	
			DataRow toNodeRow = this.getNodeRow(nextNodeId);
			
			//执行进入此节点的
			if(this.runInExpNode(toNodeRow, docData, docId, docType)){

				TriggerType toNodeTriggerType = (TriggerType)Enum.valueOf(TriggerType.class, toNodeRow.getStringValue("triggertype"));
				NodeTypeLegend toNodeType = (NodeTypeLegend)Enum.valueOf(NodeTypeLegend.class, toNodeRow.getStringValue("nodetype"));
				
				//创建新的step
				String toStepId = this.toNode(toNodeRow, instanceId, createUserId, docId, docData, docType, toNodeType);
				
				//判断新流转至的节点，是否继续自动流转
				boolean autoDriveNextNode = false; 
				//如果下一个节点是自动节点，并行开始节点，分支节点，那么继续执行程序自动通过此节点；
				//入股是并行结束节点，那么需要判断没有存在instancestep位于本并行分组内或子分组内 

				if(toNodeType == NodeTypeLegend.startParallel || toNodeType == NodeTypeLegend.judge){
					autoDriveNextNode = true;
				}
				else if(toNodeType == NodeTypeLegend.endParallel){
					//如果并行结束节点所在的分组内，除了处在此并行节点外的step，没有其它step. == true，那么继续流转之 
					String toNodeId = toNodeRow.getStringValue("id");
					String groupNodeId = toNodeRow.getStringValue("groupid");
					List<DataRow> inParallelStepRows = this.getActiveStepInParallel(instanceId, groupNodeId);
					int stepCountOnOtherNode = 0;
					for(DataRow checkStepRow : inParallelStepRows){
						if(!toNodeId.equals(checkStepRow.getStringValue("nodeid"))){
							stepCountOnOtherNode++;
						}
					}
					if(stepCountOnOtherNode == 0){
						autoDriveNextNode = true;
					}
				} 
				else if(toNodeType == NodeTypeLegend.end){
					autoDriveNextNode = false;
				}
				else if(toNodeTriggerType == TriggerType.serverAuto){
					autoDriveNextNode = true;
				}
				if(autoDriveNextNode){
					this.driveInstance(docType, docData, docId, toStepId, "", currentUserId, OperateType.autoDrive);
				} 
			}
			else{
				throw new Exception("执行inExp返回false!");
			}		
		}
	}
	
	//获取并行节点内的
	private List<DataRow> getActiveStepInParallel(String instanceId, String groupNodeId) throws Exception{
		List<DataRow> inParallelStepRows = new ArrayList<DataRow>();
		List<DataRow> allActiveSteps = this.getAcitveStep(instanceId);
		for(DataRow stepRow : allActiveSteps){
			String nodeId = stepRow.getStringValue("nodeid");
			if(this.checkInGroup(nodeId, groupNodeId)){
				inParallelStepRows.add(stepRow);
			}
		}
		return inParallelStepRows;
	}
	
	private boolean checkInGroup(String nodeId, String groupNodeId) throws Exception{
		DataRow nodeRow = this.getNodeRow(nodeId);
		String groupId = nodeRow.getStringValue("groupid");
		if(groupId == null || groupId.trim().isEmpty()){
			//那么说明不属于任何分组
			return false;
		}
		else if(groupId.equals(groupNodeId)){
			return false;
		}
		else{
			//递归判断
			return this.checkInGroup(groupId, groupNodeId);
		}
			
	}
	
	
	protected void beforeDrive(INcpSession session, JSONObject requestObj) throws Exception{  
	}
	
	protected void afterDrive(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{ 
	}

	  
	//取回
	@Override
	public HashMap<String, Object> getBackWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException {  
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.getBack(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}	
	}
	
	public HashMap<String, Object> getBack(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			 
			beforeDrive(session, requestObj);			

			HashMap<String, Object> resultHash = getBackCore(session, requestObj);

			afterDrive(session, requestObj, resultHash);  
			return resultHash;
		}
		catch(Exception ex){
    	ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
	
	protected HashMap<String, Object> getBackCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		String docTypeName =   CommonFunction.decode(requestObj.getString("docTypeName"));
		String docId = requestObj.getString("docId"); 
		String note = CommonFunction.decode(requestObj.getString("note"));
		DocType docType = this.getDocType(docTypeName);
		Data docData = docType.getMainData();
		
		String currentUserId = session.getUserId();
		this.lockDocData(docId, docType);
		HashMap<String, Object> resultObj = this.getBackInstance(docType, docData, docId, note, currentUserId); 
		return resultObj;
	}
	
	private DataRow getUserLastLogRow(String instanceId, String currentUserId) throws Exception{
		String currentUserLogSql ="select il.id as logid,"
				+ " ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " il.processtime as processtime,"
				+ " node.name as nodename,"
				+ " node.canbackfrom as canbackfrom,"
				+ " node.canbackto as canbackto,"
				+ " node.nodetype as nodetype,"
				+ " node.backinexp as backinexp,"
				+ " node.groupid as groupid"
				+ " from wf_instancelog il"
				+ " left outer join wf_instancestep ist on il.stepid = ist.id"
				+ " left outer join wf_node node on il.nodeid = node.id"
				+ " where il.isdisabled = 'N' and ist.statustype = 'passed'"
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid"
				+ " and il.userid = " + SysConfig.getParamPrefix() + "userid" 
				+ " and (il.operatetype = 'drive' or il.operatetype = 'submit')" 
				+ " and (node.nodetype = 'active' or node.nodetype = 'start')" 
				+ " order by il.processtime desc";
		
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("instanceid", instanceId);
		p2vs.put("userid", currentUserId);
		
		List<String> alias = new ArrayList<String>();
		alias.add("logid");
		alias.add("stepid");
		alias.add("nodeid");
		alias.add("processtime");
		alias.add("nodename");
		alias.add("canbackfrom");
		alias.add("canbackto");
		alias.add("nodetype");
		alias.add("backinexp");
		alias.add("groupid");
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();  
		fieldValueTypes.put("logid", ValueType.String);
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("nodeid", ValueType.String);
		fieldValueTypes.put("processtime", ValueType.Time);
		fieldValueTypes.put("nodename", ValueType.String);
		fieldValueTypes.put("canbackfrom", ValueType.Boolean);
		fieldValueTypes.put("canbackto", ValueType.Boolean);
		fieldValueTypes.put("nodetype", ValueType.String);
		fieldValueTypes.put("backinexp", ValueType.String);
		fieldValueTypes.put("groupid", ValueType.String);
		
		DataTable logTable = this.dBParserAccess.selectList(this.getDBSession(), currentUserLogSql, p2vs, alias, fieldValueTypes, 0, 1);
		
		List<DataRow> logRows = logTable.getRows();
		if(logRows.size() == 0){
			throw new Exception("不存在此用户的有效审批!");
		}
		else{
			DataRow logRow = logRows.get(0);
			return logRow;
		} 
	}
	
	private List<DataRow> getLogRowsAfterTime(String instanceId, Date processTime) throws Exception{
		String afterLogSql = "select il.id as logid,"
				+ " ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " il.processtime as processtime,"
				+ " node.name as nodename,"
				+ " node.canbackfrom as canbackfrom,"
				+ " node.canbackto as canbackto,"
				+ " node.nodetype as nodetype,"
				+ " node.backinexp as backinexp,"
				+ " node.groupid as groupid"
				+ " from wf_instancelog il"
				+ " left outer join wf_instancestep ist on il.stepid = ist.id"
				+ " left outer join wf_node node on il.nodeid = node.id"
				+ " where il.isdisabled = 'N' and ist.statustype = 'passed'"
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid "
				+ " and il.processtime > " + SysConfig.getParamPrefix() + "processtime"
				+ " order by il.processtime desc";
		
		HashMap<String, Object> afterP2vs = new HashMap<String, Object>();
		afterP2vs.put("instanceid", instanceId);
		afterP2vs.put("processtime", processTime);
		
		List<String> afterAlias = new ArrayList<String>();
		afterAlias.add("logid");
		afterAlias.add("stepid");
		afterAlias.add("nodeid");
		afterAlias.add("processtime");
		afterAlias.add("nodename");
		afterAlias.add("canbackfrom");
		afterAlias.add("canbackto");
		afterAlias.add("nodetype");
		afterAlias.add("backinexp");
		afterAlias.add("groupid");
		
		Map<String, ValueType> afterFieldValueTypes = new HashMap<String, ValueType>();  
		afterFieldValueTypes.put("logid", ValueType.String);
		afterFieldValueTypes.put("stepid", ValueType.String);
		afterFieldValueTypes.put("nodeid", ValueType.String);
		afterFieldValueTypes.put("processtime", ValueType.Time);
		afterFieldValueTypes.put("nodename", ValueType.String);
		afterFieldValueTypes.put("canbackfrom", ValueType.Boolean);
		afterFieldValueTypes.put("canbackto", ValueType.Boolean);
		afterFieldValueTypes.put("nodetype", ValueType.String);
		afterFieldValueTypes.put("backinexp", ValueType.String);
		afterFieldValueTypes.put("groupid", ValueType.String);
		
		DataTable afterLogTable = this.dBParserAccess.selectList(this.getDBSession(), afterLogSql, afterP2vs, afterAlias, afterFieldValueTypes);
					
		return afterLogTable.getRows();
	}
	
	private List<DataRow> getLogRows(String instanceId) throws Exception{
		String afterLogSql = "select il.id as logid,"
				+ " ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " il.processtime as processtime,"
				+ " il.operatetype as operatetype,"
				+ " il.note as note,"
				+ " il.userid as userid,"
				+ " u.name as username,"
				+ " node.name as nodename,"
				+ " node.canbackfrom as canbackfrom,"
				+ " node.canbackto as canbackto,"
				+ " node.nodetype as nodetype,"
				+ " node.backinexp as backinexp,"
				+ " node.groupid as groupid"
				+ " from wf_instancelog il"
				+ " left outer join wf_instancestep ist on il.stepid = ist.id"
				+ " left outer join wf_node node on il.nodeid = node.id"
				+ " left outer d_user u on il.userid = u.id"
				+ " where il.isdisabled = 'N' and ist.statustype = 'passed' "
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid" 
				+ " order by il.processtime desc";
		
		HashMap<String, Object> afterP2vs = new HashMap<String, Object>();
		afterP2vs.put("instanceid", instanceId); 
		
		List<String> afterAlias = new ArrayList<String>();
		afterAlias.add("logid");
		afterAlias.add("stepid");
		afterAlias.add("nodeid");
		afterAlias.add("processtime");
		afterAlias.add("operatetype");
		afterAlias.add("note");
		afterAlias.add("userid");
		afterAlias.add("username");
		afterAlias.add("nodename");
		afterAlias.add("canbackfrom");
		afterAlias.add("canbackto");
		afterAlias.add("nodetype");
		afterAlias.add("backinexp");
		afterAlias.add("groupid");
		
		Map<String, ValueType> afterFieldValueTypes = new HashMap<String, ValueType>();  
		afterFieldValueTypes.put("logid", ValueType.String);
		afterFieldValueTypes.put("stepid", ValueType.String);
		afterFieldValueTypes.put("nodeid", ValueType.String);
		afterFieldValueTypes.put("processtime", ValueType.Time);
		afterFieldValueTypes.put("operatetype", ValueType.String);
		afterFieldValueTypes.put("note", ValueType.String);
		afterFieldValueTypes.put("userid", ValueType.String);
		afterFieldValueTypes.put("username", ValueType.String);
		afterFieldValueTypes.put("nodename", ValueType.String);
		afterFieldValueTypes.put("canbackfrom", ValueType.Boolean);
		afterFieldValueTypes.put("canbackto", ValueType.Boolean);
		afterFieldValueTypes.put("nodetype", ValueType.String);
		afterFieldValueTypes.put("backinexp", ValueType.String);
		afterFieldValueTypes.put("groupid", ValueType.String);
		
		DataTable afterLogTable = this.dBParserAccess.selectList(this.getDBSession(), afterLogSql, afterP2vs, afterAlias, afterFieldValueTypes);
					
		return afterLogTable.getRows();
	}
	
	private List<DataRow> getCanBackToStepRows(String instanceId) throws Exception{		
		List<DataRow> currentStepRows = this.getAcitveStep(instanceId);
		for(DataRow stepRow : currentStepRows){
			boolean canBackFrom = "Y".equals(stepRow.getStringValue("canbackfrom"));
			if(!canBackFrom){
				return null;
			}
			
		}
		
		List<DataRow> allPassedStepRows = this.getAllPassedStepRows(instanceId);
		List<DataRow> canBackToStepRows = new ArrayList<DataRow>();
		for(DataRow stepRow : allPassedStepRows){
			StepStatusType statusType = (StepStatusType)Enum.valueOf(StepStatusType.class, stepRow.getStringValue("statustype"));
			if(statusType != StepStatusType.disabled){ 
				boolean canBackFrom = "Y".equals(stepRow.getStringValue("canbackfrom"));
				boolean canBackTo = "Y".equals(stepRow.getStringValue("canbackto"));
				if(canBackTo){
					canBackToStepRows.add(stepRow);
					if(!canBackFrom){
						break;
					}
				}
				else{
					break;
				}				
			}			
		}
		return canBackToStepRows;
	}
	
	private List<DataRow> getAllPassedStepRows(String instanceId) throws Exception{
		String passedStepSql = "selec ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " ist.outtime as outtime,"
				+ " ist. as ,"
				+ " ist.statustype as statustype,"
				+ " node.name as nodename,"
				+ " node.canbackfrom as canbackfrom,"
				+ " node.canbackto as canbackto,"
				+ " node.nodetype as nodetype,"
				+ " node.backinexp as backinexp,"
				+ " node.groupid as groupid"
				+ " from wf_instancestep ist"
				+ " left outer join wf_node node on ist.nodeid = node.id"
				+ " where ist.statustype = 'passed' and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid"
				+ " order by ist.outtime desc";
		
		HashMap<String, Object> passedP2vs = new HashMap<String, Object>();
		passedP2vs.put("instanceid", instanceId); 
		
		List<String> passedAlias = new ArrayList<String>(); 
		passedAlias.add("stepid");
		passedAlias.add("nodeid"); 
		passedAlias.add("outtime"); 
		passedAlias.add("statustype");
		passedAlias.add("nodename");
		passedAlias.add("canbackfrom");
		passedAlias.add("canbackto");
		passedAlias.add("nodetype");
		passedAlias.add("backinexp");
		passedAlias.add("groupid");
		
		Map<String, ValueType> passedFieldValueTypes = new HashMap<String, ValueType>();   
		passedFieldValueTypes.put("stepid", ValueType.String);
		passedFieldValueTypes.put("nodeid", ValueType.String);
		passedFieldValueTypes.put("outtime", ValueType.Time); 
		passedFieldValueTypes.put("statustype", ValueType.String);
		passedFieldValueTypes.put("nodename", ValueType.String);
		passedFieldValueTypes.put("canbackfrom", ValueType.Boolean);
		passedFieldValueTypes.put("canbackto", ValueType.Boolean);
		passedFieldValueTypes.put("nodetype", ValueType.String);
		passedFieldValueTypes.put("backinexp", ValueType.String);
		passedFieldValueTypes.put("groupid", ValueType.String);
		
		DataTable afterStepTable = this.dBParserAccess.selectList(this.getDBSession(), passedStepSql, passedP2vs, passedAlias, passedFieldValueTypes);
					
		return afterStepTable.getRows();
	}
	
	private List<String> checkCanBackTo(DataRow toRow, List<DataRow> afterLogRows, List<DataRow> activeStepRows) throws Exception{
		List<String> errors = new ArrayList<String>();
		for(DataRow activeStepRow : activeStepRows){
			boolean canBackFrom = "Y".equals(activeStepRow.getStringValue("canbackfrom"));
			String nodeName = activeStepRow.getStringValue("nodename");			
			if(!canBackFrom){
				errors.add("不允许从节点'" + nodeName + "'退回");
			}
		}
		
		for(DataRow afterLogRow : afterLogRows){
			boolean canBackTo = "Y".equals(afterLogRow.getStringValue("canbackto"));
			boolean canBackFrom = "Y".equals(afterLogRow.getStringValue("canbackfrom"));
			String nodeName = afterLogRow.getStringValue("nodename");			
			if(!canBackFrom){
				errors.add("不允许从节点'" + nodeName + "'退回");
			}
			if(!canBackTo){
				errors.add("不允许退回到节点'" + nodeName + "'");
			}
		}
		
		{
			boolean canBackTo = "Y".equals(toRow.getStringValue("canbackto")); 
			String nodeName = toRow.getStringValue("nodename");			
			if(!canBackTo){
				errors.add("不允许退回到节点'" + nodeName + "'");
			}
		}
		return errors;
	}
	
	private HashMap<String, Object> getBackInstance(DocType docType, Data docData, String docId, String note, String currentUserId) throws SQLException, Exception{
		
		DataRow instRow = this.findAvailableInstance(docId, docType); 

		String workflowId = instRow.getStringValue("workflowid");
		String createUserId = instRow.getStringValue("userid");
    	String instanceId = instRow.getStringValue("instanceid");     	
    	
    	List<DataRow> activeStepRows = this.getAcitveStep(instanceId);
    	
    	DataRow userLogRow = this.getUserLastLogRow(instanceId, currentUserId);
    	
    	Date processTime = userLogRow.getDateTimeValue("processtime");
    	
    	List<DataRow> afterLogRows = this.getLogRowsAfterTime(instanceId, processTime);
    	
    	List<String> checkCanBackErrors = this.checkCanBackTo(userLogRow, afterLogRows, activeStepRows);
    	if(checkCanBackErrors.size() == 0){ 
    		for(DataRow activeStepRow : activeStepRows){
    			String activeStepId = activeStepRow.getStringValue("stepid");  
    			String activeNodeId = activeStepRow.getStringValue("nodeid");  
    			this.createInstanceLog(instanceId, activeStepId, activeNodeId, currentUserId, note, OperateType.getBack);
				this.disableStep(instanceId, activeStepId, docData, docId, docType); 
    		}
    		
    		for(DataRow afterLogRow : afterLogRows){
    			String afterNodeId = afterLogRow.getStringValue("nodeid"); 
    			String afterStepId = afterLogRow.getStringValue("stepid"); 
    			String backInExp = afterLogRow.getStringValue("backinexp");  
    			this.runBackInExpNode(afterNodeId, backInExp, docData, docId, docType);
				this.disableStep(instanceId, afterStepId, docData, docId, docType);
    		}
    		
    		{
    			String toNodeId = userLogRow.getStringValue("nodeid"); 
    			String toStepId = userLogRow.getStringValue("stepid"); 
    			String backInExp = userLogRow.getStringValue("backinexp");  	
    			this.runBackInExpNode(toNodeId, backInExp, docData, docId, docType);
				this.disableStep(instanceId, toStepId, docData, docId, docType);

	    		DataRow toNodeRow = this.getNodeRow(toNodeId);
				NodeTypeLegend nodeType = (NodeTypeLegend)Enum.valueOf(NodeTypeLegend.class, toNodeRow.getStringValue("nodetype"));
	    		switch(nodeType){
		    		case start:
					case active: {
			    		this.toNode(toNodeRow, instanceId, createUserId, docId, docData, docType, nodeType);
						DataRow workflowRow = this.getWorkflow(workflowId);
						String abstractExp = workflowRow.getStringValue("abstractexp");					
						DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docId, docType);
						HashMap<String, Object> resultHash = new HashMap<String, Object>();
						this.addStatusToResult(statusRow, resultHash);
						return resultHash;
					}
					case endParallel:
					case end:
					case judge:
					case startParallel:					 
					default: {
						throw new Exception("Error node type, nodeType = " + nodeType.toString() + ", instanceId = " +instanceId);
					}
	    		}
    		}
    	}
    	else{
    		String errorStr = CommonFunction.listToString(checkCanBackErrors, ".");
    		throw new Exception(errorStr);
    	} 
	}
	
	protected void beforeGetBack(INcpSession session, JSONObject requestObj) throws Exception{ 
		//取回流程实例
		
		//先判断单据状态是否正确，即单据当前所在的点是否已经不是这个点了
		
		//找到当前用户最后一次审批的步骤，取回到此步骤
	}
	
	protected void afterGetBack(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{ 
	}

	  
	//退回
	@Override
	public HashMap<String, Object> sendBackWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException {  
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.sendBack(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}	
	}
	
	public HashMap<String, Object> sendBack(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			 
			beforeDrive(session, requestObj);			

			HashMap<String, Object> resultHash = sendBackCore(session, requestObj);

			afterDrive(session, requestObj, resultHash);  
			return resultHash;
		}
		catch(Exception ex){
    	ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
	
	protected HashMap<String, Object> sendBackCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
		String docId = requestObj.getString("docId");
		String currentStepId = requestObj.getString("currentStepId");
		String backToStepId = requestObj.getString("backToStepId");
		String note = CommonFunction.decode(requestObj.getString("note"));
		DocType docType = this.getDocType(docTypeName);
		Data docData = docType.getMainData();
		
		String currentUserId = session.getUserId();
		this.lockDocData(docId, docType);
		HashMap<String, Object> resultObj = this.sendBackInstance(docType, docData, docId, currentStepId, backToStepId, note, currentUserId); 
		return resultObj;	
	}
	
	private HashMap<String, Object> sendBackInstance(DocType docType, Data docData, String docId, String currentStepId, String backToStepId, String note, String currentUserId) throws SQLException, Exception{

		DataRow instRow = this.findAvailableInstance(docId, docType); 
    	String instanceId = instRow.getStringValue("instanceid");    
    	String workflowId = instRow.getStringValue("workflowid");
    	String createUserId = instRow.getStringValue("userid");
    	
		boolean currentStep = this.checkActiveUser(instanceId, currentStepId, currentUserId, TriggerType.clientManual, NodeTypeLegend.active);
		if(!currentStep){
			throw new Exception("您不是当前节点审批人");
		}
		else{
	    	List<DataRow> activeStepRows = this.getAcitveStep(instanceId);
	    	DataRow backToStepRow = this.getStepRow(backToStepId); 
	    	StepStatusType backToStepStatusType = (StepStatusType)Enum.valueOf(StepStatusType.class, backToStepRow.getStringValue("statustype"));
	    	if(backToStepStatusType == StepStatusType.disabled){
				throw new Exception("不能退回到此节点, 节点已作废");
	    	}
	    	else{ 
		    	Date backToStepOutTime = backToStepRow.getDateTimeValue("outtime");
		    	
		    	List<DataRow> afterLogRows = this.getLogRowsAfterTime(instanceId, backToStepOutTime);
		    	
		    	String backToNodeId = backToStepRow.getStringValue("nodeid");
		    	
		    	DataRow backToNodeRow = this.getNodeRow(backToNodeId);
		    	
		    	List<String> checkCanBackErrors = this.checkCanBackTo(backToNodeRow, afterLogRows, activeStepRows);
		    	if(checkCanBackErrors.size() == 0){ 
		    		for(DataRow activeStepRow : activeStepRows){ 
		    			String activeStepId = activeStepRow.getStringValue("stepid");  
		    			String activeNodeId = activeStepRow.getStringValue("nodeid");  
		    			this.createInstanceLog(instanceId, activeStepId, activeNodeId, currentUserId, note, OperateType.sendBack);
						this.disableStep(instanceId, activeStepId, docData, docId, docType); 
		    		}
		    		
		    		for(DataRow afterLogRow : afterLogRows){
		    			String afterNodeId = afterLogRow.getStringValue("nodeid"); 
		    			String afterStepId = afterLogRow.getStringValue("stepid"); 
		    			String backInExp = afterLogRow.getStringValue("backinexp");  
		    			this.runBackInExpNode(afterNodeId, backInExp, docData, docId, docType);
						this.disableStep(instanceId, afterStepId, docData, docId, docType);
		    		}
		    		
		    		{ 
		    			String backInExp = backToNodeRow.getStringValue("backinexp");  	
		    			this.runBackInExpNode(backToNodeId, backInExp, docData, docId, docType);
						this.disableStep(instanceId, backToStepId, docData, docId, docType); 
						NodeTypeLegend nodeType = (NodeTypeLegend)Enum.valueOf(NodeTypeLegend.class, backToNodeRow.getStringValue("nodetype"));
			    		switch(nodeType){
				    		case start:
							case active: {
					    		this.toNode(backToNodeRow, instanceId, createUserId, docId, docData, docType, nodeType);
								DataRow workflowRow = this.getWorkflow(workflowId);
								String abstractExp = workflowRow.getStringValue("abstractexp");					
								DataRow statusRow = this.refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docId, docType);
								HashMap<String, Object> resultHash = new HashMap<String, Object>();
								this.addStatusToResult(statusRow, resultHash);
								return resultHash;
							}
							case endParallel:
							case end:
							case judge:
							case startParallel:					 
							default:
								throw new Exception("Error node type, nodeType = " + nodeType.toString() + ", instanceId = " +instanceId);
			    		}
		    		} 
		    	}
		    	else{
		    		String errorStr = CommonFunction.listToString(checkCanBackErrors, ".");
		    		throw new Exception(errorStr);
		    	} 
	    	}
		}
	}
	
	protected void beforeSendBack(INcpSession session, JSONObject requestObj) throws Exception{ 
		//退回流程实例
		
		//先判断单据状态是否正确，即单据当前所在的点是否已经不是这个点了
		
		//先判断当前用户是不是待审批节点的审批人，如果是那么允许退回
		//查找到退回要经过的所有点，然后逐个点退回，执行backinexp
		//遇到并行结束节点时，需要仔细考虑处理方式，要根据退回到的那个step的审批时间，来判断其它并行分支是停留在并行结束点，还是所有并行分支节点都回退 
	}
	
	protected void afterSendBack(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{ 
	} 

	//获取可退回到的节点	
	@Override
	public HashMap<String, Object> getCanBackToSteps(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{

			String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
			String docId = requestObj.getString("docId");
			DocType docType = this.getDocType(docTypeName);
			DataRow instRow = this.findAvailableInstance(docId, docType); 
	    	String instanceId = instRow.getStringValue("instanceid");    
			List<DataRow> stepRows = this.getCanBackToStepRows(instanceId);
			
			JSONArray stepJsonArray = new JSONArray();
			for(DataRow stepRow : stepRows){
				String stepId = stepRow.getStringValue("stepid");
				String nodeName = stepRow.getStringValue("nodename"); 
				JSONObject stepJson = new JSONObject();
				stepJson.put("stepId", stepId);
				stepJson.put("nodeName", CommonFunction.encode(nodeName));
				stepJsonArray.add(stepJson);
			}
			
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("stepRows", stepJsonArray);
			
			return resultHash;
		}
		catch(Exception ex){
    	ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}

	//获取审批记录
	@Override
	public HashMap<String, Object> getAllUserLogs(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{

			String docTypeName =  CommonFunction.decode(requestObj.getString("docTypeName"));
			String docId = requestObj.getString("docId");
			DocType docType = this.getDocType(docTypeName);
			DataRow instRow = this.findAvailableInstance(docId, docType); 
	    	String instanceId = instRow.getStringValue("instanceid");    
			List<DataRow> logRows = this.getLogRows(instanceId);
			
			JSONArray logJsonArray = new JSONArray();
			for(DataRow logRow : logRows){
				String stepId = logRow.getStringValue("stepid");
				String operateType = logRow.getStringValue("operatetype");
				String logId = logRow.getStringValue("logid");
				String note = logRow.getStringValue("note"); 
				String userId = logRow.getStringValue("userid"); 
				String userName = logRow.getStringValue("username"); 
				Date processTime = logRow.getDateTimeValue("processtime"); 
				JSONObject logJson = new JSONObject();
				logJson.put("stepId", stepId);
				logJson.put("operateType", operateType);
				logJson.put("logId", logId);
				logJson.put("note", CommonFunction.encode(note));
				logJson.put("userId", userId);
				logJson.put("userName", CommonFunction.encode(userName));
				logJson.put("processTime", ValueConverter.convertToString(processTime, ValueType.Time));
				logJsonArray.add(logJson);
			}
			
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("logRows", logJsonArray);
			
			return resultHash;
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
	
	@Override	
	public void updateStepStatusTypeAfterEndAsyn(INcpSession session, String stepId, String instanceId, String workflowId, String docDataId, String docTypeName, StepStatusType newStatusType, NodeTypeLegend nodeType) throws Exception{
		if(this.checkActiveUser(instanceId, stepId, session.getUserId(), TriggerType.clientManual, nodeType) || this.checkTimingUser(session.getUserId())){
			//wf_instancestep作废
			String updateStepStatusSql = "update wf_instancestep set" 
					+ " statustype = " + SysConfig.getParamPrefix() + "statustype"
					+ " where id = " + SysConfig.getParamPrefix() + "stepid";
			HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
			p2vs.put("statustype", newStatusType.toString()); 
			p2vs.put("stepid", stepId);
			this.dBParserAccess.update(this.getDBSession(), updateStepStatusSql, p2vs);
			
			//更新单据状态
			DataRow workflowRow = this.getWorkflow(workflowId);
			String abstractExp = workflowRow.getStringValue("abstractexp");			
			DocType docType = this.getDocType(docTypeName);
			Data docData = docType.getMainData(); 	
			this.refreshInstanceStatus(instanceId, abstractExp, workflowId, docData, docDataId, docType);
		}
		else{
    		throw new Exception("当前用户不是有效审批人.");
		}
	}
	
	@Override
	public List<DataRow> getCurrentStepStatusRows(String instanceId) throws SQLException {
		String stepSql = "select ist.id as stepid,"
				+ " ist.nodeid as nodeid,"
				+ " node.name as nodename,"
				+ " node.statusname as statusname,"
				+ " ist.intime as intime,"
				+ " ist.statustype as statustype"
				+ " from wf_instancestep ist"
				+ " left outer join wf_node node on ist.nodeid = node.id" 
				+ " where (ist.statustype ='active'or ist.statustype = 'suspended' or ist.statustype = 'asynProcessing' or ist.statustype = 'asynError' or ist.statustype = 'asynSucceed')"
				+ " and ist.instanceid = " + SysConfig.getParamPrefix() + "instanceid" 
				+ " order by ist.intime desc";
		
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("instanceid", instanceId); 
		
		List<String> alias = new ArrayList<String>(); 
		alias.add("stepid");
		alias.add("nodeid"); 
		alias.add("nodename"); 
		alias.add("statusname"); 
		alias.add("intime");
		alias.add("statustype"); 
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();  
		fieldValueTypes.put("stepid", ValueType.String); 
		fieldValueTypes.put("nodeid", ValueType.String);
		fieldValueTypes.put("nodename", ValueType.String);
		fieldValueTypes.put("statusname", ValueType.String);
		fieldValueTypes.put("intime", ValueType.Time);
		fieldValueTypes.put("statustype", ValueType.String); 
		
		DataTable stepTable = this.dBParserAccess.selectList(this.getDBSession(), stepSql, p2vs, alias, fieldValueTypes);
					
		return stepTable.getRows();
	}
	
	@Override
	public List<DataRow> getInvokeRows(String stepId) throws SQLException {
		String invokeSql = "select invo.id as invokeid,"
				+ " invo.fromid as stepid,"
				+ " invo.invoketime as invoketime,"
				+ " invo.endtime as endtime,"
				+ " invo.statustype as statustype,"
				+ " invo.note as note,"
				+ " invo.serviceid as serviceid,"
				+ " serv.name as servicename,"
				+ " invo.createtime as createtime,"
				+ " invo.nextchecktime as nextchecktime,"
				+ " invo.timeouttime as timeouttime"
				+ " from ais_invoke invo"
				+ " left outer join ais_service serv on serv.id = invo.serviceid" 
				+ " where invo.fromname = 'workflow' and invo.fromid = " +SysConfig.getParamPrefix() + "stepid" 
				+ " order by invo.createtime desc";
		
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("stepid", stepId); 
		
		List<String> alias = new ArrayList<String>(); 
		alias.add("invokeid");
		alias.add("stepid"); 
		alias.add("invoketime"); 
		alias.add("endtime"); 
		alias.add("statustype");
		alias.add("note"); 
		alias.add("serviceid"); 
		alias.add("servicename"); 
		alias.add("createtime"); 
		alias.add("nextchecktime"); 
		alias.add("timeouttime"); 
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();  
		fieldValueTypes.put("invokeid", ValueType.String); 
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("invoketime", ValueType.Time);
		fieldValueTypes.put("endtime", ValueType.Time);
		fieldValueTypes.put("statustype", ValueType.String);
		fieldValueTypes.put("note", ValueType.String);
		fieldValueTypes.put("serviceid", ValueType.String); 
		fieldValueTypes.put("servicename", ValueType.String); 
		fieldValueTypes.put("createtime", ValueType.Time); 
		fieldValueTypes.put("nextchecktime", ValueType.Time); 
		fieldValueTypes.put("timeouttime", ValueType.Time);  
		
		DataTable invokeTable = this.dBParserAccess.selectList(this.getDBSession(), invokeSql, p2vs, alias, fieldValueTypes);
					
		return invokeTable.getRows();
	}
	
	@Override
	public List<DataRow> getInstanceStepUser(NcpSession session, String instanceId, String nodeId, String stepId) throws SQLException {
		String userSql = "select iu.id as id,"
				+ " iu.userid as userid,"
				+ " u.name as username"
				+ " from wf_instanceuser iu"
				+ " left outer join d_user u on u.id = iu.userid" 
				+ " where iu.instanceid = " + SysConfig.getParamPrefix() + "instanceid"
				+ " and iu.nodeid = " + SysConfig.getParamPrefix() + "nodeid"
				+ " and iu.stepid = " + SysConfig.getParamPrefix() + "stepid"
				+ " order by u.name asc";
		
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("stepid", stepId); 
		p2vs.put("nodeid", nodeId); 
		p2vs.put("instanceid", instanceId); 
		
		List<String> alias = new ArrayList<String>(); 
		alias.add("id");
		alias.add("userid"); 
		alias.add("username");  
		
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();  
		fieldValueTypes.put("id", ValueType.String); 
		fieldValueTypes.put("userid", ValueType.String); 
		fieldValueTypes.put("username", ValueType.String); 
		
		DataTable userTable = this.dBParserAccess.selectList(this.getDBSession(), userSql, p2vs, alias, fieldValueTypes);
					
		return userTable.getRows();
	}
	
	@Override
	public HashMap<String, Object> getDetailStatus(NcpSession session, JSONObject requestObj) {
		try{
			String instanceId = requestObj.getString("instanceId");
			List<DataRow> stepRows = this.getCurrentStepStatusRows(instanceId);
			JSONArray stepStatusJsonArray = new JSONArray();
			for(DataRow stepRow : stepRows){ 
				String stepId = stepRow.getStringValue("stepid");
				String nodeId = stepRow.getStringValue("nodeid");
				String nodeName = stepRow.getStringValue("nodename");
				String statusName = stepRow.getStringValue("statusname"); 
				Date inTime = stepRow.getDateTimeValue("intime"); 
				String statusType = stepRow.getStringValue("statustype");  
				JSONObject stepJson = new JSONObject();
				stepJson.put("stepId", stepId);
				stepJson.put("nodeId", nodeId);
				stepJson.put("nodeName", CommonFunction.encode(nodeName));
				stepJson.put("statusName", CommonFunction.encode(statusName));
				stepJson.put("inTime", ValueConverter.convertToString(inTime, ValueType.Time));
				stepJson.put("statusType", statusType);

				
				JSONArray invokeArray = new JSONArray();
				List<DataRow> invokeRows = this.getInvokeRows(stepId);
				for(DataRow invokeRow : invokeRows){ 
					String invokeId = invokeRow.getStringValue("invokeid");
					Date invokeTime = invokeRow.getDateTimeValue("invoketime");
					Date endTime = invokeRow.getDateTimeValue("endtime");
					String invokeStatusType = invokeRow.getStringValue("statustype");
					String note = invokeRow.getStringValue("note"); 
					String serviceid = invokeRow.getStringValue("serviceid"); 
					Date createTime = invokeRow.getDateTimeValue("createtime"); 
					Date nextCheckTime = invokeRow.getDateTimeValue("nextchecktime"); 
					Date timeoutTime = invokeRow.getDateTimeValue("timeouttime"); 
					JSONObject invoJson = new JSONObject();
					invoJson.put("invokeId", invokeId);
					invoJson.put("invokeTime", ValueConverter.convertToString(invokeTime, ValueType.Time));
					invoJson.put("endTime", ValueConverter.convertToString(endTime, ValueType.Time));
					invoJson.put("createTime", ValueConverter.convertToString(createTime, ValueType.Time));
					invoJson.put("nextCheckTime", ValueConverter.convertToString(nextCheckTime, ValueType.Time));
					invoJson.put("timeoutTime", ValueConverter.convertToString(timeoutTime, ValueType.Time));
					invoJson.put("invokeStatusType", invokeStatusType);
					invoJson.put("serviceid", serviceid);
					invoJson.put("note", CommonFunction.encode(note));
					invokeArray.add(invoJson);
				}
				stepJson.put("invokes", invokeArray);
				
				JSONArray userArray = new JSONArray();
				List<DataRow> userRows = this.getInstanceStepUser(session, instanceId, nodeId, stepId);
				for(DataRow userRow : userRows){ 
					String id = userRow.getStringValue("id");
					String userId = userRow.getStringValue("userid");
					String userName = userRow.getStringValue("username"); 
					JSONObject userJson = new JSONObject();
					userJson.put("id", id);
					userJson.put("userId", userId);
					userJson.put("userName", userName); 
					userArray.add(userJson);
				}
				stepJson.put("users", userArray); 

				stepStatusJsonArray.add(stepJson);
			}
			
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("detailStatus", stepStatusJsonArray);
			
			return resultHash;
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} 
	}
}
