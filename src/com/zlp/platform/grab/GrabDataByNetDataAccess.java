package com.zlp.platform.grab;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.zlp.platform.asynService.AsynInvokeBase;
import com.zlp.platform.asynService.InvokeResult;
import com.zlp.platform.asynService.InvokeStatusType;
import com.zlp.platform.common.HttpPostRequest;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dataManagement.importExportDefinition.UpdateType;
import com.zlp.platform.expression.definition.RunAt;
import com.zlp.platform.expression.definition.ValidateResult;
import com.zlp.platform.expression.definition.Validator;
import com.zlp.platform.expression.run.RunResult;
import com.zlp.platform.expression.run.Runner;
import com.zlp.platform.expression.run.RuntimeUserParameter;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.schedule.JobInstanceRunner;

public class GrabDataByNetDataAccess extends AsynInvokeBase{
	private static Logger logger=Logger.getLogger(GrabDataByNetDataAccess.class);

	//默认fromName，用于记录在爬取工具，爬取任务的来源是哪里
	private String defaultFromName;
	public String getDefaultFromName(){
		return this.defaultFromName;
	}
	public void setDefaultFromName(String defaultFromName){
		this.defaultFromName = defaultFromName;
	} 
	
	//爬取文件存储根目录
	private String fileRootFolder;
	public String getFileRootFolder(){
		return this.fileRootFolder;
	}
	public void setFileRootFolder(String fileRootFolder){
		this.fileRootFolder = fileRootFolder;
	} 
	
	//爬取服务路径
	private String grabServiceUrl;
	public String getGrabServiceUrl(){
		return this.grabServiceUrl;
	}
	public void setGrabServiceUrl(String grabServiceUrl){
		this.grabServiceUrl = grabServiceUrl;
	} 
	
	//添加爬取任务的服务名称
	private String createTaskMethodName;
	public String getCreateTaskMethodName(){
		return this.createTaskMethodName;
	}
	public void setCreateTaskMethodName(String createTaskMethodName){
		this.createTaskMethodName = createTaskMethodName;
	}
	
	//删除爬取任务的服务名称
	private String deleteTaskMethodName;
	public String getDeleteTaskMethodName(){
		return this.deleteTaskMethodName;
	}
	public void setDeleteTaskMethodName(String deleteTaskMethodName){
		this.deleteTaskMethodName = deleteTaskMethodName;
	}
	
	//获取爬取任务列表的服务名称
	private String getTaskListMethodName;
	public String getGetTaskListMethodName(){
		return this.getTaskListMethodName;
	}
	public void setGetTaskListMethodName(String getTaskListMethodName){
		this.getTaskListMethodName = getTaskListMethodName;
	}
	
	//获取爬取任务详细信息的服务名称
	private String getDetailInfoByTaskIdMethodName;
	public String getGetDetailInfoByTaskIdMethodName(){
		return this.getDetailInfoByTaskIdMethodName;
	}
	public void setGetDetailInfoByTaskIdMethodName(String getDetailInfoByTaskIdMethodName){
		this.getDetailInfoByTaskIdMethodName = getDetailInfoByTaskIdMethodName;
	}	
	
	//更改任务级别的服务名称
	private String changeTaskLevelMethodName;
	public String getChangeTaskLevelMethodName(){
		return this.changeTaskLevelMethodName;
	}
	public void setChangeTaskLevelMethodName(String changeTaskLevelMethodName){
		this.changeTaskLevelMethodName = changeTaskLevelMethodName;
	}	
	
	public GrabDataByNetDataAccess(){
	}

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	} 
	
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	
	private String taskDefName = ""; 
	public String getTaskDefName()
	{
		return this.taskDefName;
	}
	
	private String docDataId = ""; 
	public String getDocDataId()
	{
		return this.docDataId;
	}
	
	private String fromName = ""; 
	public String getFromName()
	{
		return this.fromName;
	}
	
	
	@Override
	public void setParameter(HashMap<String, Object> parameterValues) { 
		this.taskDefName = (String)parameterValues.get("taskDefName");
		this.docDataId = (String)parameterValues.get("docDataId");
		this.fromName = (String)parameterValues.get("fromName");
	}
	
	@Override	
	public InvokeResult run(){ 
		Session dbSession = null;
		Transaction tx = null;
		try { 
			dbSession = this.openDBSession(); 
			tx = dbSession.beginTransaction();
			Grab_TaskInstance taskInstanceObj = this.createTaskInstanceObj(dbSession);			
			taskInstanceObj.setId(this.insertIntoDB(dbSession, taskInstanceObj));			
			this.invokeGrabServiceCreateTask(dbSession, taskInstanceObj);			
			tx.commit();
			return new InvokeResult(InvokeStatusType.running, "异步执行中...");	
		}
		catch(Exception ex){
			tx.rollback();
			return new InvokeResult(InvokeStatusType.error, ex);		
		}	
		finally{
			if(dbSession != null){
				dbSession.close();  
			}
		}
	}
	
	//在数据库中创建抓取任务实例
	private Grab_TaskInstance createTaskInstanceObj(Session dbSession) throws Exception{
		Grab_TaskDef taskDefObj = this.getTaskDef(dbSession, this.getTaskDefName());
		Grab_TaskInstance taskInstanceObj = new Grab_TaskInstance();
		taskInstanceObj.setInsertTime(new Date());
		taskInstanceObj.setDescription(taskDefObj.getDescription());
		taskInstanceObj.setLevel(new BigDecimal(1));
		taskInstanceObj.setName(taskDefObj.getName()); 
		taskInstanceObj.setInvokeId(this.getInvokeId()); 
		taskInstanceObj.setTaskDefId(taskDefObj.getId()); 

		HashMap<String, ValueType> runExpParamTypes = new HashMap<String, ValueType>();
		List<RuntimeUserParameter> runExpParameters = new ArrayList<RuntimeUserParameter>();
		
		Data docData = DataCollection.getData(taskDefObj.getDataName());
		
		if(docData != null){
			DataTable docTable = this.dBParserAccess.getDtById(dbSession, docData, this.getDocDataId());
			List<DataRow> docDataRows =docTable.getRows();
			if(docDataRows.size() == 0){
				throw new Exception("Can not find doc data. dataName = " + taskDefObj.getDataName() + ", docDataId = " + this.getDocDataId());
			}
			else{
				DataRow docDataRow = docDataRows.get(0);
				for(DataField field : docData.getDataFields().values()){
					String fieldName = field.getName();
					runExpParamTypes.put(fieldName, field.getValueType());
					runExpParameters.add(new RuntimeUserParameter(fieldName, docDataRow.getValue(fieldName)));
				}
			}
			
		}
		
		List<Grab_StepInstance> allStepInstanceObjects = new ArrayList<Grab_StepInstance>(); 
		
		for(Grab_StepDef stepDefObj : taskDefObj.getAllStepDefs()){
			Grab_StepInstance  stepInstanceObj = new Grab_StepInstance();
			stepInstanceObj.setGroupName(stepDefObj.getGroupName());
			stepInstanceObj.setProjectName(stepDefObj.getProjectName());
			stepInstanceObj.setRunIndex(stepDefObj.getRunIndex());

			//运行表获取各个路径、参数的表达式
			String parameters = this.runExp(dbSession, stepDefObj.getParametersExp(), runExpParamTypes, runExpParameters);
			stepInstanceObj.setParameters(parameters);
			 
			String inputDir = this.getAbsolutePath(this.runExp(dbSession, stepDefObj.getInputDirExp(), runExpParamTypes, runExpParameters));
			stepInstanceObj.setInputDir(inputDir);
			
			String listFilePath = this.getAbsolutePath(this.runExp(dbSession, stepDefObj.getListFilePathExp(), runExpParamTypes, runExpParameters));
			stepInstanceObj.setListFilePath(listFilePath);
			
			String middleDir = this.getAbsolutePath(this.runExp(dbSession, stepDefObj.getMiddleDirExp(), runExpParamTypes, runExpParameters));
			stepInstanceObj.setMiddleDir(middleDir);
			
			String outputDir = this.getAbsolutePath(this.runExp(dbSession, stepDefObj.getOutputDirExp(), runExpParamTypes, runExpParameters));
			stepInstanceObj.setOutputDir(outputDir);
			
			allStepInstanceObjects.add(stepInstanceObj);
		}
		taskInstanceObj.setAllStepInstances(allStepInstanceObjects);
		
		return taskInstanceObj;
	}
	
	private String getAbsolutePath(String relativePath){
		return this.fileRootFolder + relativePath;
	}
	
	private String insertIntoDB(Session dbSession, Grab_TaskInstance taskInstance) throws RuntimeException, Exception{
		Data taskInstanceData = DataCollection.getData("grab_TaskInstance");	
		Data stepInstanceData = DataCollection.getData("grab_StepInstance");		 
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("description", taskInstance.getDescription());
		fieldValues.put("invokeid", taskInstance.getInvokeId()); 
		fieldValues.put("level", taskInstance.getLevel());
		fieldValues.put("name", taskInstance.getName());
		fieldValues.put("inserttime", taskInstance.getInsertTime());
		fieldValues.put("taskdefid", taskInstance.getTaskDefId());		
		
		String taskInstanceId = this.dBParserAccess.insertByData(dbSession, taskInstanceData, fieldValues);
		List<HashMap<String, Object>> allStepFieldValues = new ArrayList<HashMap<String, Object>>();
		for(Grab_StepInstance stepInstance : taskInstance.getAllStepInstances()){			
			stepInstance.setTaskInstanceId(taskInstanceId);			
			HashMap<String, Object> stepFieldValues = new HashMap<String, Object>();
			stepFieldValues.put("groupname", stepInstance.getGroupName());
			stepFieldValues.put("inputdir", stepInstance.getInputDir());
			stepFieldValues.put("listfilepath", stepInstance.getListFilePath());
			stepFieldValues.put("middledir", stepInstance.getMiddleDir());
			stepFieldValues.put("outputdir", stepInstance.getOutputDir());
			stepFieldValues.put("parameters", stepInstance.getParameters());
			stepFieldValues.put("projectname", stepInstance.getProjectName());
			stepFieldValues.put("runindex", stepInstance.getRunIndex());
			stepFieldValues.put("taskinstanceid", stepInstance.getTaskInstanceId());
			allStepFieldValues.add(stepFieldValues);
		}
		this.dBParserAccess.insertByData(dbSession, stepInstanceData, allStepFieldValues);
		return taskInstanceId;
	}
	
	private void invokeGrabServiceCreateTask(Session dbSession, Grab_TaskInstance taskInstance) throws Exception{
		String taskInstanceId = taskInstance.getId();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document requestDoc = builder.newDocument();
        requestDoc.setXmlVersion("1.0");
        Element rootElement = requestDoc.createElement("createTask");
        requestDoc.appendChild(rootElement);
        rootElement.setAttribute("name", taskInstance.getName());
        rootElement.setAttribute("level", taskInstance.getLevel().toString());
        rootElement.setAttribute("description", taskInstance.getDescription());
        rootElement.setAttribute("serialNumber", taskInstanceId);
        
        Element stepListElement = requestDoc.createElement("steps");
        rootElement.appendChild(stepListElement);
        for(Grab_StepInstance stepInstance : taskInstance.getAllStepInstances()){
            Element stepElement = requestDoc.createElement("step");
            stepListElement.appendChild(stepElement);
            stepElement.setAttribute("groupName", stepInstance.getGroupName());
            stepElement.setAttribute("projectName", stepInstance.getProjectName());
            stepElement.setAttribute("listFilePath", stepInstance.getListFilePath());
            stepElement.setAttribute("inputDir", stepInstance.getInputDir());
            stepElement.setAttribute("middleDir", stepInstance.getMiddleDir());
            stepElement.setAttribute("outputDir", stepInstance.getOutputDir());
            stepElement.setAttribute("parameters", stepInstance.getParameters());
            stepElement.setAttribute("runIndex", stepInstance.getRunIndex().toString());        	
        }
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty("encoding","utf-8");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.transform(new DOMSource(requestDoc), new StreamResult(bos));
        String xmlStr = bos.toString(); 
        
		String url = this.getGrabServiceUrl() + this.getCreateTaskMethodName();
		String responseString = HttpPostRequest.doPost(url, xmlStr); 
		this.updateTaskInstance(dbSession, taskInstanceId, responseString);
	}
	private String updateTaskInstance(Session dbSession, String taskInstanceId, String taskInfoString) throws Exception{
		SAXReader reader = new SAXReader(); 
		org.dom4j.Document responseDoc = reader.read(new StringReader(taskInfoString)); 
		org.dom4j.Element responseRootNode = responseDoc.getRootElement();  
		String responseCode = responseRootNode.attribute("code").getValue();
		if("000".equals(responseCode)){
			//任务创建成功
			String createTimeStr = responseRootNode.attributeValue("createTime");
			String statusType = responseRootNode.attributeValue("statusType");
			String grabServiceId = responseRootNode.attributeValue("id");
			Date createTime = (Date)ValueConverter.convertToObject(createTimeStr, ValueType.Time);
			HashMap<String, Object> updateTaskInstanceFieldValues = new HashMap<String, Object>();
			updateTaskInstanceFieldValues.put("createtime", createTime);
			updateTaskInstanceFieldValues.put("statustype", statusType);
			updateTaskInstanceFieldValues.put("grabserviceid", grabServiceId);
			updateTaskInstanceFieldValues.put("createtime", createTime);
			
			Data taskInstanceData = DataCollection.getData("grab_TaskInstance"); 		
			
			this.dBParserAccess.updateByData(dbSession, taskInstanceData, updateTaskInstanceFieldValues, taskInstanceId);
			
			List<org.dom4j.Element> allStepNodes = responseRootNode.selectSingleNode("steps").selectNodes("step");
			
			String updateStepSql = "update grab_StepInstance set" 
				+ " starttime = " + SysConfig.getParamPrefix() +"starttime,"
				+ " endtime = " + SysConfig.getParamPrefix() +"endtime,"
				+ " statustype = " + SysConfig.getParamPrefix() +"statustype,"
				+ " message = " + SysConfig.getParamPrefix() +"message"
				+ " where taskinstanceid = " + SysConfig.getParamPrefix() +"taskinstanceid " 
				+ " and runindex = " + SysConfig.getParamPrefix() +"runindex" ;
			for(org.dom4j.Element stepNode : allStepNodes){ 
                String groupName = stepNode.attributeValue("groupName");
                String projectName = stepNode.attributeValue("projectName");
                String listFilePath = stepNode.attributeValue("listFilePath");
                String inputDir = stepNode.attributeValue("inputDir");
                String middleDir = stepNode.attributeValue("middleDir");
                String outputDir = stepNode.attributeValue("outputDir");
                String parameters = stepNode.attributeValue("parameters");
                int runIndex = Integer.parseInt(stepNode.attributeValue("runIndex"));

                String endTimeStr = stepNode.attributeValue("endTime");
                Date endTime = endTimeStr == null || endTimeStr.isEmpty() ? null : ValueConverter.convertToTime(endTimeStr, "yyyy-MM-dd HH:mm:ss");
                String message = stepNode.attributeValue("message");
                String startTimeStr = stepNode.attributeValue("startTime");
                Date startTime = startTimeStr == null || startTimeStr.isEmpty() ? null : ValueConverter.convertToTime(startTimeStr, "yyyy-MM-dd HH:mm:ss");
                String stepStatusType = stepNode.attributeValue("statusType"); 
 
                
    			HashMap<String, Object> updateStepInstanceFieldValues = new HashMap<String, Object>();
    			updateStepInstanceFieldValues.put("starttime", startTime);
    			updateStepInstanceFieldValues.put("endtime", endTime);
    			updateStepInstanceFieldValues.put("statustype", statusType); 
    			updateStepInstanceFieldValues.put("message", message);
    			updateStepInstanceFieldValues.put("taskinstanceid", taskInstanceId);
    			updateStepInstanceFieldValues.put("runindex", runIndex);
    			this.dBParserAccess.update(dbSession, updateStepSql, updateStepInstanceFieldValues);    			
			}
			return statusType;
		}
		else{
			//任务创建失败
			String responseMessage = responseRootNode.attribute("message").getValue();
			throw new Exception("抓取程序报错。Message = " + responseMessage);
		} 

	}
	
	private String runExp(Session dbSession, String exp, HashMap<String, ValueType> parameterValueType, List<RuntimeUserParameter> parameterValues) throws Exception{
		try{ 
			Runner expRunner = (Runner)ContextUtil.getBean("expRunner");
			Validator expValidator = (Validator)ContextUtil.getBean("expValidator");
			ValidateResult validateResult = expValidator.validateExp(exp, parameterValueType, RunAt.Server, ValueType.String);
			if(validateResult.getSucceed()){ 
				RunResult runResult = expRunner.runExp(validateResult, parameterValues, dbSession);
				if(runResult.getSucceed()){
					return (String)runResult.getValue();
				}
				else{
					throw new Exception("执行表达式出错. exp = " + exp + ", errors = " + runResult.getError());
				} 
			}
			else{
				throw new Exception("验证表达式出错. exp = " + exp + ", errors = " + ValueConverter.arrayToString(validateResult.getErrors(), ". "));
			}
		}
		catch(Exception ex){			
			ex.printStackTrace();
			String error = ex.getMessage();
			logger.error(error); 
			throw ex;			
		} 
	}
	
	private Grab_TaskDef getTaskDef(Session dbSession, String taskDefName) throws Exception{
		Data taskDefData = DataCollection.getData("grab_TaskDef");	
		Data stepDefData = DataCollection.getData("grab_StepDef");		
		DataTable taskDefTable = this.dBParserAccess.getDtByFieldValue(dbSession, taskDefData, "name", "=", taskDefName);
		List<DataRow> taskDefRows = taskDefTable.getRows();
		if(taskDefRows.size() == 0){
			throw new Exception("Can not find taskDef named = " + taskDefName);
		}
		else {
			DataRow taskDefRow = taskDefRows.get(0);
			Grab_TaskDef taskDefObj = new Grab_TaskDef();
			taskDefObj.setId(taskDefRow.getStringValue("id"));
			taskDefObj.setName(taskDefRow.getStringValue("name"));
			taskDefObj.setDataName(taskDefRow.getStringValue("dataname"));
			taskDefObj.setDescription(taskDefRow.getStringValue("description"));
			List<Grab_StepDef> allStepDefObjects = new ArrayList<Grab_StepDef>();
			
			DataTable stepDefTable = this.dBParserAccess.getDtByFieldValue(dbSession, stepDefData, "taskdefid", "=", taskDefObj.getId());
			List<DataRow> stepDefRows = stepDefTable.getRows();
			for(DataRow stepDefRow : stepDefRows){
				Grab_StepDef stepDefObj = new Grab_StepDef();
				stepDefObj.setGroupName(stepDefRow.getStringValue("groupname"));
				stepDefObj.setId(stepDefRow.getStringValue("id"));
				stepDefObj.setInputDirExp(stepDefRow.getStringValue("inputdirexp"));
				stepDefObj.setListFilePathExp(stepDefRow.getStringValue("listfilepathexp"));
				stepDefObj.setMiddleDirExp(stepDefRow.getStringValue("middledirexp"));
				stepDefObj.setOutputDirExp(stepDefRow.getStringValue("outputdirexp"));
				stepDefObj.setParametersExp(stepDefRow.getStringValue("parametersexp"));
				stepDefObj.setProjectName(stepDefRow.getStringValue("projectname"));
				stepDefObj.setRunIndex(stepDefRow.getBigDecimalValue("runindex"));
				stepDefObj.setTaskDefId(stepDefRow.getStringValue("taskdefid"));
				allStepDefObjects.add(stepDefObj);
			}
			taskDefObj.setAllStepDefs(allStepDefObjects);	
			return taskDefObj;		
		}
	}  
	//调用抓取服务，获取任务状态
	private InvokeResult accessGrabServiceToGetTaskStatus() throws Exception{
		Session dbSession = null; 
		try { 
			dbSession = this.openDBSession(); 
			String invokeId = this.getInvokeId();
			
			String getTaskIdSql = "select t.grabserviceid as grabserviceid, t.id as taskinstanceid from grab_taskinstance t where t.invokeid = " + SysConfig.getParamPrefix() + "invokeid";
			HashMap<String, Object> getTaskIdP2vs = new HashMap<String, Object>();
			getTaskIdP2vs.put("invokeid", invokeId);
			String[] fieldNames = new String[]{"grabserviceid", "taskinstanceid"};
			ValueType[] fieldTypes = new ValueType[]{ValueType.String, ValueType.String};
			DataTable taskInstanceTable = this.dBParserAccess.getMultiLineValues(dbSession, getTaskIdSql, getTaskIdP2vs, fieldNames, fieldTypes);
			List<DataRow> taskInstanceRows = taskInstanceTable.getRows();
			if(taskInstanceRows.size() == 0){
				return new InvokeResult(InvokeStatusType.error, "不存在的grab_taskinstance. invokeId = " + invokeId);
			}
			else{			
				DataRow taskInstanceRow = taskInstanceRows.get(0);
				String grabServiceId = taskInstanceRow.getStringValue("grabserviceid"); 
				String taskInstanceId = taskInstanceRow.getStringValue("taskinstanceid"); 
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        Document requestDoc = builder.newDocument();
		        requestDoc.setXmlVersion("1.0");
		        Element rootElement = requestDoc.createElement("createTask");
		        requestDoc.appendChild(rootElement);
		        rootElement.setAttribute("taskId", grabServiceId);  
		        
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer t = tf.newTransformer();
		        t.setOutputProperty("encoding","utf-8");
		        ByteArrayOutputStream bos = new ByteArrayOutputStream();
		        t.transform(new DOMSource(requestDoc), new StreamResult(bos));
		        String xmlStr = bos.toString(); 
		        
				String url = this.getGrabServiceUrl() + this.getGetDetailInfoByTaskIdMethodName();
				String responseString = HttpPostRequest.doPost(url, xmlStr); 
				String statusType = this.updateTaskInstance(dbSession, taskInstanceId, responseString);
				if("Deleted".equals(statusType)){
					return new InvokeResult(InvokeStatusType.deleted, "");
				}
				else if("Error".equals(statusType)){
					return new InvokeResult(InvokeStatusType.error, "");
				}
				else if("Succeed".equals(statusType)){
					return new InvokeResult(InvokeStatusType.succeed, "");
				}
				else if("Running".equals(statusType)){
					return new InvokeResult(InvokeStatusType.running, "");
				}
				else if("Waiting".equals(statusType)){
					//这里返回的是invoke的状态，虽然爬取任务还没开始，但是invoke已经调用了，所以返回的是running状态
					return new InvokeResult(InvokeStatusType.running, "");
				}
				else{ 
					return new InvokeResult(InvokeStatusType.error, "未知的状态 " + statusType);
				}
			}		
		}
		catch(Exception ex){			
			ex.printStackTrace();
			String error = ex.getMessage();
			logger.error(error); 
			throw ex;			
		} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
	}

	@Override
	public InvokeResult check() throws Exception { 
		return this.accessGrabServiceToGetTaskStatus();  
	}

	@Override
	public int getCheckWaitingSecond() { 
		return 10;
	}

	//最长执行时间，超过这个时间，则认为超时出错
	@Override
	public int getMaxRuningSecond() { 
		return 60 * 60 * 24;
	}
}
