package com.zlp.platform.asynService;
 
import java.math.BigDecimal; 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List; 
import org.apache.log4j.Logger;
import org.hibernate.Session; 
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil; 
import com.zlp.platform.expression.run.ExternalBase;
import com.zlp.platform.expression.run.IDatabaseAccess;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 

//异步调用处理
public class AsynInvokeServiceProcessor extends ExternalBase implements IAsynInvokeServiceProcessor{ 
	private static Logger logger=Logger.getLogger(AsynInvokeServiceProcessor.class);

	//启动processCount个异步任务(此方法已经配置了定义任务，即轮询定时执行)
	public void asynInvoke(BigDecimal processCount) throws Exception{ 
		List<DataRow> invokeRows = this.getTopNInvoke(processCount.intValue());
		for(DataRow invokeRow : invokeRows){
			this.invokeService(invokeRow);
		}
	}
	
	//启动checkCount个异步任务(此方法已经配置了定义任务，即轮询定时执行)
	public void checkAsynInvokeStatus(BigDecimal checkCount) throws Exception{ 
		List<DataRow> invokeRows = this.getTopNNeedCheck(checkCount.intValue());
		for(DataRow invokeRow : invokeRows){
			this.checkStatus(invokeRow);
		}
	}
	
	//执行一个异步任务
	private void invokeService(DataRow row) throws Exception{
		String invokeId = row.getStringValue("invokeid"); 
		String serviceName = row.getStringValue("servicename");
		InvokeResult invokeResult = null;
		Date invokeTime = new Date();

		if(ContextUtil.containsBean(serviceName)){
			Object invokeServiceBeanObj = ContextUtil.getBean(serviceName);
			
			if(IAsynInvokeBase.class.isInstance(invokeServiceBeanObj)){
				IAsynInvokeBase invokeServiceBean = (IAsynInvokeBase)invokeServiceBeanObj;
				invokeServiceBean.setInvokeId(invokeId);
				try{
					HashMap<String, Object> parameters = this.getInvokeParameters(invokeId);
					invokeServiceBean.setParameter(parameters);
				}
				catch(Exception ex){
					invokeResult = new InvokeResult(InvokeStatusType.error, "Invoke service error on setParameter。 " + ex.getMessage());
				}
				
				try{
					invokeResult = invokeServiceBean.run();
				}
				catch(Exception ex){
					invokeResult = new InvokeResult(InvokeStatusType.error, "Invoke service error on run。 " + ex.getMessage());
				}
				
				int checkWaitingSecond = 0;
				int timeoutWaitingSecond = 0;
				try{
					checkWaitingSecond = invokeServiceBean.getCheckWaitingSecond(); 
				}
				catch(Exception ex){
					invokeResult = new InvokeResult(InvokeStatusType.error, "Invoke service error on getCheckWaitingSecond。 " + ex.getMessage());
				}
				try{ 
					timeoutWaitingSecond = invokeServiceBean.getMaxRuningSecond();
				}
				catch(Exception ex){
					invokeResult = new InvokeResult(InvokeStatusType.error, "Invoke service error on getMaxRuningSecond。 " + ex.getMessage());
				}
				this.updateCheckAndOutTime(invokeId, checkWaitingSecond, timeoutWaitingSecond);
			}
			else{
				invokeResult = new InvokeResult( InvokeStatusType.error, serviceName + " must implement interface IAsynInvokeBase");
			}
		} 
		else{
			invokeResult = new InvokeResult( InvokeStatusType.error, "None bean declared, name = " + serviceName);
		}
		this.updateInvokeStatusOnInvoke(invokeId, invokeResult, invokeTime);
	}
	
	//检验异步执行状态，例如调用爬取任务端服务来检验
	private void checkStatus(DataRow row){
		String invokeId = row.getStringValue("invokeid"); 
		String serviceName = row.getStringValue("servicename");
		InvokeResult invokeResult = null;

		if(ContextUtil.containsBean(serviceName)){
			Object invokeServiceBeanObj = ContextUtil.getBean(serviceName);
			
			if(IAsynInvokeBase.class.isInstance(invokeServiceBeanObj)){
				IAsynInvokeBase invokeServiceBean = (IAsynInvokeBase)invokeServiceBeanObj;
				invokeServiceBean.setInvokeId(invokeId);
				try{
					invokeResult = invokeServiceBean.check();
				}
				catch(Exception ex){
					invokeResult = new InvokeResult(InvokeStatusType.error, "Invoke service error on running。 " + ex.getMessage());
				}
				if(invokeResult.getStatusType() == InvokeStatusType.running){
					Date timeoutTime = row.getDateTimeValue("timeouttime");
					Date currentTime = new Date();
					if(currentTime.after(timeoutTime)){
						//超市了
						invokeResult = new InvokeResult(InvokeStatusType.timeout, "");
					}
					else {
						try{
							int second = invokeServiceBean.getCheckWaitingSecond();
							this.updateCheckTime(invokeId, second);
						}
						catch(Exception ex){
							invokeResult = new InvokeResult(InvokeStatusType.error, "GetCheckWaitingSecond error。 " + ex.getMessage());
						}
					}
				}
			}
			else{
				invokeResult = new InvokeResult( InvokeStatusType.error, serviceName + " must implement interface IAsynInvokeBase");
			}
		} 
		else{
			invokeResult = new InvokeResult( InvokeStatusType.error, "None bean declared, name = " + serviceName);
		}
		this.updateInvokeStatus(invokeId, invokeResult);
	}
	
	//更新检测状态时间
	private void updateCheckTime(String invokeId, int second){
		Calendar checkTime = Calendar.getInstance();
		checkTime.setTime(new Date());
		checkTime.add(Calendar.SECOND, second);
		String updateCheckTimeSql = "update ais_invoke set nextchecktime = " + SysConfig.getParamPrefix() + "nextchecktime"
			+" where id = " + SysConfig.getParamPrefix() + "invokeid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("nextchecktime", checkTime.getTime()); 
		p2vs.put("invokeid", invokeId);
	
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		dBParserAccess.update(dbSession, updateCheckTimeSql, p2vs);
	}
	
	//更新检验和超时时间
	private void updateCheckAndOutTime(String invokeId, int checkWaitingSecond, int timeoutWaitingSecond){

		Calendar nextCheckTime = Calendar.getInstance();
		nextCheckTime.setTime(new Date());
		nextCheckTime.add(Calendar.SECOND, checkWaitingSecond);
		
		Calendar outTime = Calendar.getInstance();
		outTime.setTime(new Date());
		outTime.add(Calendar.SECOND, timeoutWaitingSecond);
		String updateTimeSql = "update ais_invoke"
			+ " set timeouttime = " + SysConfig.getParamPrefix() + "timeouttime,"
			+ "nextchecktime = " + SysConfig.getParamPrefix() + "nextchecktime"
			+" where id = " + SysConfig.getParamPrefix() + "invokeid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("nextchecktime", nextCheckTime.getTime()); 
		p2vs.put("timeouttime", outTime.getTime()); 
		p2vs.put("invokeid", invokeId);
	
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		dBParserAccess.update(dbSession, updateTimeSql, p2vs);
	}
	
	//更新状态
	private void updateInvokeStatusOnInvoke(String invokeId, InvokeResult invokeResult, Date invokeTime){

		String updateStatusSql = "";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("statustype", invokeResult.getStatusType().toString());
		p2vs.put("note", invokeResult.getMessage());
		p2vs.put("invoketime", invokeTime);
		p2vs.put("invokeid", invokeId);
		
		switch(invokeResult.getStatusType()){ 
			case running:{
				updateStatusSql = "update ais_invoke set statustype = " + SysConfig.getParamPrefix() + "statustype,"
					+ " note = " + SysConfig.getParamPrefix() + "note,"
					+ " invoketime = " + SysConfig.getParamPrefix() + "invoketime"
					+ " where id = " + SysConfig.getParamPrefix() + "invokeid";
			}
			break;
			case timeout:
			case succeed:
			case error:{
				updateStatusSql = "update ais_invoke set statustype = " + SysConfig.getParamPrefix() + "statustype,"
					+ " note = " + SysConfig.getParamPrefix() + "note,"
					+ " endtime = " + SysConfig.getParamPrefix() + "endtime,"
					+ " invoketime = " + SysConfig.getParamPrefix() + "invoketime"
					+ " where id = " + SysConfig.getParamPrefix() + "invokeid";
				p2vs.put("endtime", new Date());
			}
			break;
		default:
			break;
		} 
		
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		dBParserAccess.update(dbSession, updateStatusSql, p2vs);
		
		logger.info("AsynInvokeService, invokeId = " + invokeId + ", statustype = " + invokeResult.getStatusType() + ", note = " + invokeResult.getMessage());
	}
	
	//更新状态
	private void updateInvokeStatus(String invokeId, InvokeResult invokeResult){
		String updateStatusSql = "";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("statustype", invokeResult.getStatusType().toString());
		p2vs.put("note", invokeResult.getMessage());
		p2vs.put("invokeid", invokeId);
		
		switch(invokeResult.getStatusType()){ 
			case running:{
				updateStatusSql = "update ais_invoke set statustype = " + SysConfig.getParamPrefix() + "statustype,"
					+ " note = " + SysConfig.getParamPrefix() + "note "
					+ " where id = " + SysConfig.getParamPrefix() + "invokeid";
			}
			break;
			case timeout:
			case succeed:
			case error:{
				updateStatusSql = "update ais_invoke set statustype = " + SysConfig.getParamPrefix() + "statustype,"
					+ " note = " + SysConfig.getParamPrefix() + "note,"
					+ " endtime = " + SysConfig.getParamPrefix() + "endtime"
					+ " where id = " + SysConfig.getParamPrefix() + "invokeid";
				p2vs.put("endtime", new Date());
			}
			break;
		default:
			break;
		} 
		
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		dBParserAccess.update(dbSession, updateStatusSql, p2vs);
		
		logger.info("AsynInvokeService, invokeId = " + invokeId + ", statustype = " + invokeResult.getStatusType() + ", note = " + invokeResult.getMessage());
	}
	
	//创建异步调用
	public String createAsynInvoke(String serviceId, String userId, String fromName, String fromId, HashMap<String, ValueType> parameterValueTypes, HashMap<String, Object> parameterValues) throws Exception{

		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		
		Data invokeData = DataCollection.getData("ais_Invoke");
		Data invokeParameterData = DataCollection.getData("ais_InvokeParameter");
		
		HashMap<String, Object> invokeValues = new HashMap<String, Object>();
		invokeValues.put("serviceid", serviceId);
		invokeValues.put("userid", userId);
		invokeValues.put("fromname", fromName);
		invokeValues.put("fromid", fromId);
		invokeValues.put("createtime", new Date());
		invokeValues.put("statustype", InvokeStatusType.waiting.toString());
		
		String invokeId = dBParserAccess.insertByData(dbSession, invokeData, invokeValues);
		
		List<HashMap<String, Object>> allInvokeParameterValues = new ArrayList<HashMap<String, Object>>();
		for(String pName : parameterValueTypes.keySet()){
			ValueType valueType = parameterValueTypes.get(pName);
			Object valueObj = parameterValues.get(pName);
			String valueStr = ValueConverter.convertToString(valueObj, valueType);
			HashMap<String, Object> invokeParameterValues = new HashMap<String, Object>();
			invokeParameterValues.put("name", pName);
			invokeParameterValues.put("valuetype", valueType.toString());
			invokeParameterValues.put("value", valueStr);
			invokeParameterValues.put("invokeid", invokeId);
			allInvokeParameterValues.add(invokeParameterValues);
		}
		dBParserAccess.insertByData(dbSession, invokeParameterData, allInvokeParameterValues);
		
		return invokeId;
	}

	
	//初始化异步调用参数值
	private HashMap<String, Object> getInvokeParameters(String invokeId) throws Exception{
		String pSql = "select aisip.id as pid,"
					+ " aisip.name as name,"
					+ " aisip.valuetype as valuetype,"
					+ " aisip.value as value" 
					+ " from ais_invokeparameter aisip"
					+ " where aisip.invokeid = " + SysConfig.getParamPrefix() + "invokeid"; 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("invokeid", invokeId);
		List<String> alias = new ArrayList<String>();
		alias.add("pid");
		alias.add("name");
		alias.add("valuetype");  
		alias.add("value");   
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("pid", ValueType.String);
		fieldValueTypes.put("name", ValueType.String);
		fieldValueTypes.put("valuetype", ValueType.String); 
		fieldValueTypes.put("value", ValueType.String); 
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		DataTable pTable = dBParserAccess.selectList(dbSession, pSql, p2vs, alias, fieldValueTypes);
		List<DataRow> pRows = pTable.getRows();
		HashMap<String, Object> pValues = new HashMap<String, Object>();
		for(DataRow pRow : pRows){
			String name = pRow.getStringValue("name");
			String valueStr = pRow.getStringValue("value");
			String valueTypeStr = pRow.getStringValue("valuetype");
			Object value = ValueConverter.convertToString(valueStr, valueTypeStr);
			pValues.put(name, value);
		}
		
		return pValues;		
	}
	
	//获取钱processCount个需要异步调用的任务
	private List<DataRow> getTopNInvoke(int processCount) throws SQLException{
		String getWaitingSql = "select invo.id as invokeid,"
					+ " invo.serviceid as serviceid,"
					+ " serv.name as servicename" 
					+ " from ais_invoke invo"
					+ " left outer join ais_service serv on serv.id = invo.serviceid"
					+ " where (invo.statustype = 'waiting')"
					+ " order by invo.createtime asc"; 
		List<String> alias = new ArrayList<String>();
		alias.add("invokeid");
		alias.add("serviceid");
		alias.add("servicename");   
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("invokeid", ValueType.String);
		fieldValueTypes.put("serviceid", ValueType.String);
		fieldValueTypes.put("servicename", ValueType.String); 
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		DataTable waitingTable = dBParserAccess.selectList(dbSession, getWaitingSql, null, alias, fieldValueTypes, 0, processCount);
		return waitingTable.getRows();		
	}
	
	//获取钱checkCount个需要检测状态的任务
	private List<DataRow> getTopNNeedCheck(int checkCount) throws SQLException{
		String getWaitingSql = "select invo.id as invokeid,"
					+ " invo.serviceid as serviceid,"
					+ " serv.name as servicename,"
					+ " invo.nextchecktime as nextchecktime,"
					+ " invo.timeouttime as timeouttime"
					+ " from ais_invoke invo"
					+ " left outer join ais_service serv on serv.id = invo.serviceid"
					+ " where invo.statustype = 'running' and invo.nextchecktime < " + SysConfig.getParamPrefix() +"currenttime"
					+ " order by invo.nextchecktime asc"; 
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("currenttime", new Date());
		List<String> alias = new ArrayList<String>();
		alias.add("invokeid");
		alias.add("serviceid");
		alias.add("servicename");   
		alias.add("nextchecktime");   
		alias.add("timeouttime");   
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("invokeid", ValueType.String);
		fieldValueTypes.put("serviceid", ValueType.String);
		fieldValueTypes.put("servicename", ValueType.String); 
		fieldValueTypes.put("nextchecktime", ValueType.Time); 
		fieldValueTypes.put("timeouttime", ValueType.Time); 
		IDatabaseAccess databaseAccess = this.getDatabaseAccess();
		Session dbSession = databaseAccess.getSession();
		IDBParserAccess dBParserAccess = databaseAccess.getDBParserAccess();
		DataTable waitingTable = dBParserAccess.selectList(dbSession, getWaitingSql, p2vs, alias, fieldValueTypes, 0, checkCount);
		return waitingTable.getRows();		
	}
}
