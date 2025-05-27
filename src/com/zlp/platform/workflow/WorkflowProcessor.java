package com.zlp.platform.workflow;
 
import java.math.BigDecimal; 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.zlp.platform.asynService.InvokeStatusType;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IDocumentBaseDao;
import com.zlp.platform.expression.run.ExternalBase;
import com.zlp.platform.expression.run.IDatabaseAccess;
import com.zlp.platform.workflow.definition.NodeTypeLegend;
import com.zlp.platform.workflow.definition.StepStatusType;
import com.alibaba.fastjson.JSONObject;

//通用方法集合
public class WorkflowProcessor extends ExternalBase{ 
	
	//定时自动流转流程
	public void timingDriveDocument(BigDecimal processCount) throws Exception{ 
		List<DataRow> waitingRows = this.getWaitingTimingProcessDocument(processCount.intValue());
		for(DataRow waitingRow : waitingRows){
			this.driveDocument(waitingRow);
		}
	}
	
	//轮询查看并更新异步执行的状态，已经完成的step，需要执行流转操作
	public void driveEndAsynDocument(BigDecimal processCount) throws Exception{
		//获取之前的状态是asynProcessing和asynSucceed的step，其对应ais_invoke已经执行完成(endtime不为空)，那么可以流转单据了
		//如果step是asynProcessing状态，那么先要修改为asynSucceed或asynError状态
		String getAsynInvokeEndStepSql = "select ist.id as stepid,"
					+ " ist.instanceid as instanceid,"
					+ " ist.statustype as statustype,"
					+ " inst.doctypeid as doctypeid,"
					+ " inst.docdataid as docdataid,"
					+ " inst.workflowid as workflowid,"
					+ " doctype.name as doctypename,"
					+ " doctype.sheetname as sheetname,"
					+ " ist.invokeid as invokeid,"
					+ " invo.statustype as invokestatustype,"
					+ " invo.userid as invokeuserid,"
					+ " n.nodetype as nodetype"
					+ " from wf_instancestep ist"
					+ " left outer join wf_instance inst on inst.id = ist.instanceid"
					+ " left outer join wf_doctype doctype on doctype.id = inst.doctypeid"
					+ " left outer join ais_invoke invo on invo.id = ist.invokeid"
					+ " left outer join wf_node n on n.id = ist.nodeid"
					+ " where (ist.statustype = 'asynProcessing' or ist.statustype = 'asynSucceed') and invo.endtime is not null order by invo.endtime asc"; 
		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("instanceid");
		alias.add("statustype"); 
		alias.add("doctypeid"); 
		alias.add("docdataid"); 
		alias.add("workflowid"); 
		alias.add("doctypename"); 
		alias.add("sheetname"); 
		alias.add("invokeid"); 
		alias.add("invokestatustype"); 
		alias.add("invokeuserid"); 
		alias.add("nodetype"); 
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("instanceid", ValueType.String);
		fieldValueTypes.put("statustype", ValueType.String);
		fieldValueTypes.put("doctypeid", ValueType.String);
		fieldValueTypes.put("docdataid", ValueType.String);
		fieldValueTypes.put("workflowid", ValueType.String);
		fieldValueTypes.put("doctypename", ValueType.String);
		fieldValueTypes.put("sheetname", ValueType.String);
		fieldValueTypes.put("invokeid", ValueType.String);
		fieldValueTypes.put("invokestatustype", ValueType.String);
		fieldValueTypes.put("invokeuserid", ValueType.String);
		fieldValueTypes.put("nodetype", ValueType.String);
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		DataTable asynInvokeEndStepTable = dBParserAccess.selectList(dbSession, getAsynInvokeEndStepSql, null, alias, fieldValueTypes, 0, processCount.intValue());
		List<DataRow> asynInvokeEndStepRows = asynInvokeEndStepTable.getRows();
		for(DataRow asynInvokeEndStepRow : asynInvokeEndStepRows){
			
			String sheetName = asynInvokeEndStepRow.getStringValue("sheetname");
			IDocumentBaseDao documentDao = ContextUtil.containsBean(sheetName) ? (IDocumentBaseDao)ContextUtil.getBean(sheetName) :  (IDocumentBaseDao)ContextUtil.getBean("documentBaseDao");
			documentDao.setDBSession(this.getDatabaseAccess().getSession());
			 
			String invokeUserId = asynInvokeEndStepRow.getStringValue("invokeuserid"); 
			NcpSession session = new NcpSession(invokeUserId);	
			String docTypeName = asynInvokeEndStepRow.getStringValue("doctypename"); 
			String docDataId = asynInvokeEndStepRow.getStringValue("docdataid");
			String stepId = asynInvokeEndStepRow.getStringValue("stepid");
			String workflowId = asynInvokeEndStepRow.getStringValue("workflowid");


			String instanceId = asynInvokeEndStepRow.getStringValue("instanceid");
			
			String nodeTypeStr = asynInvokeEndStepRow.getStringValue("nodetype");
			NodeTypeLegend nodeType = Enum.valueOf(NodeTypeLegend.class, nodeTypeStr);
			
			String stepStatusTypeStr = asynInvokeEndStepRow.getStringValue("statustype");
			StepStatusType stepStatusType = Enum.valueOf(StepStatusType.class, stepStatusTypeStr);
			if(stepStatusType == StepStatusType.asynProcessing){
				String invokeStatusTypeStr = asynInvokeEndStepRow.getStringValue("invokestatustype");
				InvokeStatusType invokeStatusType = Enum.valueOf(InvokeStatusType.class, invokeStatusTypeStr);
				switch(invokeStatusType){ 
				case error: 
					case timeout:{
						stepStatusType = StepStatusType.asynError;
					}
					break;
					case succeed:{
						stepStatusType = StepStatusType.asynSucceed;
					}
					break; 
					default:{
						throw new Exception("The asynInvokeService is end, status type can not be " + invokeStatusTypeStr);
					}
				}
				documentDao.updateStepStatusTypeAfterEndAsyn(session, stepId, instanceId, workflowId, docDataId, docTypeName, stepStatusType, nodeType);
			}
			if(stepStatusType == StepStatusType.asynSucceed) {
				JSONObject requestObj = new JSONObject();
				requestObj.put("docTypeName", CommonFunction.encode(docTypeName));
				requestObj.put("docId", docDataId);
				requestObj.put("stepId", stepId);
				requestObj.put("note", CommonFunction.encode("处理完成")); 
				documentDao.driveWithTx(session, requestObj);
			}
		}		
	}
	
	private void driveDocument(DataRow row) throws Exception{
		String docTypeName = row.getStringValue("doctypename");
		String sheetName = row.getStringValue("sheetname");
		String docDataId = row.getStringValue("docdataid");
		String stepId = row.getStringValue("stepid");
		IDocumentBaseDao documentDao = ContextUtil.containsBean(sheetName) ? (IDocumentBaseDao)ContextUtil.getBean(sheetName) :  (IDocumentBaseDao)ContextUtil.getBean("documentBaseDao");
		documentDao.setDBSession(this.getDatabaseAccess().getSession());
		JSONObject requestObj = new JSONObject();
		requestObj.put("docTypeName", CommonFunction.encode(docTypeName));
		requestObj.put("docId", docDataId);
		requestObj.put("stepId", stepId);
		requestObj.put("note", CommonFunction.encode("自动处理")); 
		String userId = documentDao.getTimingDriveUserId();
		NcpSession session = new NcpSession(userId);				
		documentDao.driveWithTx(session, requestObj);		
	}
	
	private List<DataRow> getWaitingTimingProcessDocument(int processCount) throws SQLException{
		String getWaitingSql = "select ist.id as stepid,"
					+ " ist.instanceid as instanceid,"
					+ " ist.statustype as statustype,"
					+ " inst.doctypeid as doctypeid,"
					+ " inst.docdataid as docdataid,"
					+ " doctype.name as doctypename,"
					+ " doctype.sheetname as sheetname"
					+ " from wf_instancestep ist"
					+ " left outer join wf_instance inst on inst.id = ist.instanceid"
					+ " left outer join wf_doctype doctype on doctype.id = inst.doctypeid"
					+ " where ist.statustype = 'active' and ist.timingprocesstime < " + SysConfig.getParamPrefix() + "currenttime and ist.timingprocesstime is not null order by ist.timingprocesstime asc";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("currenttime", new Date());
		List<String> alias = new ArrayList<String>();
		alias.add("stepid");
		alias.add("instanceid");
		alias.add("statustype"); 
		alias.add("doctypeid"); 
		alias.add("docdataid"); 
		alias.add("doctypename"); 
		alias.add("sheetname"); 
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("stepid", ValueType.String);
		fieldValueTypes.put("instanceid", ValueType.String);
		fieldValueTypes.put("statustype", ValueType.String);
		fieldValueTypes.put("doctypeid", ValueType.String);
		fieldValueTypes.put("docdataid", ValueType.String);
		fieldValueTypes.put("doctypename", ValueType.String);
		fieldValueTypes.put("sheetname", ValueType.String);
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		DataTable waitingTable = dBParserAccess.selectList(dbSession, getWaitingSql, p2vs, alias, fieldValueTypes, 0, processCount);
		return waitingTable.getRows();		
	}
}
