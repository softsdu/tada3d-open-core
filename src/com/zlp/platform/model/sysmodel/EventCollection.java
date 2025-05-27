package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.common.SysConfig;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.SqlCollection; 

public class EventCollection implements InitializingBean  {

	private static Logger logger=Logger.getLogger(EventCollection.class);

	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}  

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}    

	//sql配置
	private static SqlCollection sqls; 
	public void setSqls(SqlCollection sqls){
		EventCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return EventCollection.sqls;
	} 
	
	//Event对象
	private static HashMap<String, Event> eventObjects ; 
	public static Event getEvent(String categoryDotName){
		return EventCollection.eventObjects.get(categoryDotName); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
	
	//重新加载某个data 
	public Event reloadDataFromDB(String eventId) throws Exception{		
		SelectSqlParser sys_eventSql = this.getDBParserAccess().getSqlParser("sys.model.event");
		SelectSqlParser sys_eventparameterSql = this.getDBParserAccess().getSqlParser("sys.model.eventparameter");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", eventId); 
		Session dbSession = null;		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();		
		   	DataTable eventDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventSql, 0, -1, params, "e.id = " + SysConfig.getParamPrefix() + "id", "");
		   	DataTable eventParameterDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventparameterSql, 0, -1, params, "ep.parentid = " + SysConfig.getParamPrefix() + "id", ""); 
		   	List<DataRow> dRows = eventDt.getRows();
		   	if(dRows.size()==0){
		   		throw new Exception("none event row, id = " + eventId);
		   	}
		   	else {
		   		Event event = this.createEventObject(dRows.get(0));

			    //读取DataField
			   	List<EventParameter> epList = new ArrayList<EventParameter>();
			   	for(DataRow epRow : eventParameterDt.getRows()){
			   		EventParameter ep = createEventParameterObject(epRow);  
			   		epList.add(ep);
			   	}
			   	event.setAllParameters(epList); 
			   	EventCollection.eventObjects.put(event.getCategory() + "." + event.getName(), event);
		   		return event;	
			}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload event model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}
	 
	//从数据库中加载全部data 
	public void initFromDB() throws Exception {
		logger.info("init Event Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_eventSql = this.getDBParserAccess().getSqlParser("sys.model.event");
			SelectSqlParser sys_eventparameterSql = this.getDBParserAccess().getSqlParser("sys.model.eventparameter");
 
		   	DataTable eventDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventSql, -1, -1, null, "", "");
		   	DataTable eventParameterDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventparameterSql, -1, -1, null, "", "");
		   		 
		   	HashMap<String, Event> idToEMap = new HashMap<String, Event>();
		    for(DataRow eRow : eventDt.getRows()){
		    	Event event = createEventObject(eRow);
		    	event.setAllParameters(new ArrayList<EventParameter>());
		    	idToEMap.put(event.getId(), event); 
		    }	  
		   	for(DataRow epRow : eventParameterDt.getRows()){
		   		EventParameter ep = createEventParameterObject(epRow);
		   		String parentId = ep.getParentId();
		   		Event event = idToEMap.get(parentId);
		   		event.getAllParameters().add(ep);
		   	}	   
		   	
		   	EventCollection.eventObjects = new HashMap<String, Event>();
		   	for(String id : idToEMap.keySet()){
		   		Event event = idToEMap.get(id);
		   		EventCollection.eventObjects.put(event.getCategory() + "." + event.getName(), event);	 
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Event Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}	 
	
	private Event createEventObject(DataRow eRow){
    	Event event = new Event();
    	event.setId(eRow.getStringValue("id")); 
    	event.setCategory(eRow.getStringValue("category")); 
    	event.setName(eRow.getStringValue("name")); 
    	event.setResultValueType(eRow.getStringValue("resultvaluetype")); 
    	event.setDescription(eRow.getStringValue("description")); 
    	return event;
    }
	
	private EventParameter createEventParameterObject(DataRow epRow){
		EventParameter ep = new EventParameter();
		ep.setId(epRow.getStringValue("id")); 
		ep.setName(epRow.getStringValue("name")); 
		ep.setDescription(epRow.getStringValue("description")); 
		ep.setValueType(epRow.getStringValue("valuetype")); 
		ep.setParentId(epRow.getStringValue("parentid")); 
    	return ep;
    } 
}
