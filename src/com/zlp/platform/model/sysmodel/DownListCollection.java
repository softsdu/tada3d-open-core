package com.zlp.platform.model.sysmodel;

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
import com.zlp.platform.dao.db.ValueType;

public class DownListCollection  implements InitializingBean{

	private static Logger logger=Logger.getLogger(DownListCollection.class);
 
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess ;
	}  

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}    
	
	//sql配置
	private static SqlCollection sqls; 
	public void setSqls(SqlCollection sqls){
		DownListCollection.sqls = sqls;
	}
	public SqlCollection getSqls( ){
		return DownListCollection.sqls;
	}  
	
	//DownList对象
	private static HashMap<String, DownList> downListObjects ; 
	public static DownList getDownList(String name){
		return DownListCollection.downListObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	}
 
	public DownList reloadDownListFromDB(String downListId) throws Exception{
		SelectSqlParser sys_downlistSql = this.getDBParserAccess().getSqlParser("sys.model.downlist");
		SelectSqlParser sys_downlistfieldSql = this.getDBParserAccess().getSqlParser("sys.model.downlistfield");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", downListId); 
		Session dbSession = null;
		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();		
		   	DataTable downListDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_downlistSql, -1, -1, params, "l.id = " + SysConfig.getParamPrefix() + "id","");//+SysConfig.getParamPrefix()+"downListId", "");
		   	DataTable downListFieldDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_downlistfieldSql,  -1, -1, params, "lf.parentid = " + SysConfig.getParamPrefix() + "id","");//+SysConfig.getParamPrefix()+"downListId", "");
		   	List<DataRow> lRows = downListDt.getRows();
		   	if(lRows.size()==0){
		   		throw new Exception("none sheet row, id = " + downListId);
		   	}
		   	else {
		   		DownList l = this.createDownListObject(lRows.get(0));
		   		 
			   	for(DataRow lfRow : downListFieldDt.getRows()){
			   		DownListField lf = createDownListFieldObject(lfRow);  
			   		l.addDownListField(lf);
			   	}  
		   		initSqlParser(l);
			   	
		   		DownListCollection.downListObjects.put(l.getName(), l);		
		   		return l;
		   	}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload down list model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}
	
	private DownList createDownListObject(DataRow lRow){
    	DownList dl = new DownList();
    	dl.setId( lRow.getStringValue("id"));
    	dl.setDsExp( lRow.getStringValue("dsexp"));
    	dl.setDsType( lRow.getStringValue("dstype"));
    	dl.setName( lRow.getStringValue("name"));  
    	return dl;
	}
	
	private DownListField createDownListFieldObject(DataRow dlfRow) throws Exception{
   		DownListField dlf = new DownListField();
   		dlf.setId( dlfRow.getStringValue("id"));
   		dlf.setParentId( dlfRow.getStringValue("parentid")); 
   		dlf.setName(dlfRow.getStringValue("name"));
   		dlf.setValueType( dlfRow.getStringValue("valuetype"));
   		dlf.setDisplayName( dlfRow.getStringValue("displayname"));
   		dlf.setIsShow( dlfRow.getBooleanValue("isshow") );
   		dlf.setIsComma( dlfRow.getBooleanValue("iscomma") ); 
   		dlf.setDecimalnum(dlfRow.isNull("decimalnum") ? 0 : dlfRow.getIntegerValue("decimalnum")); 
   		dlf.setShowIndex( dlfRow.getIntegerValue("showindex")); 
   		dlf.setShowWidth( dlfRow.getIntegerValue("showwidth")); 
   		dlf.setDataPurviewFactor( dlfRow.getStringValue("datapurviewfactor"));
   		return dlf;
	}
	  
	public void initFromDB() throws Exception {
		logger.info("init DownList Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_downlistSql = this.getDBParserAccess().getSqlParser("sys.model.downlist");
			SelectSqlParser sys_downlistfieldSql = this.getDBParserAccess().getSqlParser("sys.model.downlistfield"); 

		   	DataTable downListDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_downlistSql, -1, -1, null, "", "");
		   	DataTable downListFieldDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_downlistfieldSql, -1, -1, null, "", "" ); 
		   	
		   	//读取DownList
		   	HashMap<String, DownList> dlMap = new HashMap<String, DownList>();
		    for(DataRow dlRow : downListDt.getRows()){
		    	DownList dl = this.createDownListObject(dlRow);
		    	dlMap.put(dl.getId(), dl); 
		    }	 
		   	
		    //读取DownListField 
		   	for(DataRow dlfRow : downListFieldDt.getRows()){
		   		DownListField dlf = this.createDownListFieldObject(dlfRow);
		   		dlMap.get(dlf.getParentId()).addDownListField(dlf);
		   	}	 
		   	 
		   	DownListCollection.downListObjects = new HashMap<String, DownList>();
		   	for(String id : dlMap.keySet()){
		   		DownList dl = dlMap.get(id); 
		   		initSqlParser(dl);
		   		DownListCollection.downListObjects.put(dl.getName(), dl);	 
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init downlist Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
	}	 
	private void initSqlParser(DownList dl) throws Exception{
    	if("sql".equals(dl.getDsType())) {
    		try
    		{
	    		SelectSqlParser sqlParser = new SelectSqlParser(dl.getDsExp());
				sqlParser.parser(); 
				dl.setDsSqlParser(sqlParser);
				HashMap<String, ValueType> fieldTypeMap = new HashMap<String, ValueType>();						
			    for(DownListField dataField : dl.getDownListFields()){   
		    		fieldTypeMap.put(dataField.getName(), dataField.getValueType());
			    }
			    sqlParser.setFieldTypeMap(fieldTypeMap);
    		}
    		catch(Exception ex){
            	ex.printStackTrace();
        	    throw new Exception("init DownList Collection error. downListId = " + dl.getId(), ex);
    		}
    	}
	}
}
