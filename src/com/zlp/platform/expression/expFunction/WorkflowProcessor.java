package com.zlp.platform.expression.expFunction;
 
import java.math.BigDecimal; 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
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

//通用方法集合
public class WorkflowProcessor extends ExternalBase{ 
	
	//定时自动流转流程
	public void timingDriveDocument(BigDecimal processCount) throws Exception{ 
		List<DataRow> waitingRows = this.getWaitingTimingProcessDocument(processCount.intValue());
		for(DataRow waitingRow : waitingRows){
			this.driveDocument(waitingRow);
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
