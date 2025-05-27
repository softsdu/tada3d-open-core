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

public class ViewCollection  implements InitializingBean {
	private static Logger logger=Logger.getLogger(ViewCollection.class);

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
		ViewCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return ViewCollection.sqls;
	} 
	
	//Data对象
	private static HashMap<String, View> viewObjects ; 
	public static View getView(String name){
		return ViewCollection.viewObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public View reloadViewFromDB(String viewId) throws Exception{
		SelectSqlParser sys_viewSql = this.getDBParserAccess().getSqlParser("sys.model.view");
		SelectSqlParser sys_viewDispunitSql = this.getDBParserAccess().getSqlParser("sys.model.viewdispunit");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", viewId); 
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
		   	DataTable viewDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_viewSql, -1, -1, params, "v.id = " + SysConfig.getParamPrefix() + "id", "");
		   	DataTable viewDispunitDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_viewDispunitSql,  -1, -1, params, "vu.parentid = " + SysConfig.getParamPrefix() + "id", "");
		   	List<DataRow> dRows = viewDt.getRows();
		   	if(dRows.size()==0){
		   		throw new Exception("none view row, id = " + viewId);
		   	}
		   	else {
		   		View view = this.createViewObject(dRows.get(0));
		   		
			    //读取Sys_ViewDispunit 
			   	for(DataRow vuRow : viewDispunitDt.getRows()){
			   		ViewDispunit vu = createViewDispunitObject(vuRow);  
			   		view.setViewDispunit(vu.getName(), vu);
			   	} 
	
			   	ViewCollection.viewObjects.put(view.getName(), view);		
		   		return view;
		   	}
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init sheet Collection error.", ex);
        }  
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}     
	}
 
	public void initFromDB() throws Exception {
		logger.info("init View Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_viewSql = this.getDBParserAccess().getSqlParser("sys.model.view");
			SelectSqlParser sys_viewDispunitSql = this.getDBParserAccess().getSqlParser("sys.model.viewdispunit");
 
		   	DataTable viewDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_viewSql, -1, -1, null, "", "");
		   	DataTable viewDispunitDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_viewDispunitSql, -1, -1, null, "", ""); 
		   	HashMap<String, View> vMap = new HashMap<String, View>();
		    for(DataRow vRow : viewDt.getRows()){
		    	View view = createViewObject(vRow);
		    	vMap.put(view.getId(), view); 
		    }	 
		   	 
		   	for(DataRow vuRow : viewDispunitDt.getRows()){
		   		ViewDispunit vu = createViewDispunitObject(vuRow);
		   		vMap.get(vu.getParentId()).setViewDispunit(vu.getName(), vu);
		   	}	 
		   	 
		    
		   	ViewCollection.viewObjects = new HashMap<String, View>();
		   	for(String id : vMap.keySet()){
		   		View view = vMap.get(id);
		   		ViewCollection.viewObjects.put(view.getName(), view);
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init View Collection error.", ex);
        }   
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}	 
	
	private View createViewObject(DataRow vRow){
    	View view = new View();
    	view.setId( vRow.getStringValue("id"));
    	view.setName( vRow.getStringValue("name"));
    	view.setTitle( vRow.getStringValue("title"));
    	view.setDataName( vRow.getStringValue("dataname")); 
    	return view;
    }
	
	private ViewDispunit createViewDispunitObject(DataRow vuRow) throws Exception{
   		ViewDispunit vu = new ViewDispunit();
   		vu.setId( vuRow.getStringValue("id"));
   		vu.setParentId( vuRow.getStringValue("parentid")); 
   		vu.setName( vuRow.getStringValue("name"));
   		vu.setLabel( vuRow.getStringValue("label"));
   		vu.setColWidth( vuRow.getIntegerValue("colwidth")); 
   		vu.setColIndex( vuRow.getIntegerValue("colindex")); 
   		vu.setColSortable( vuRow.getBooleanValue("colsortable") );
   		vu.setColSearch( vuRow.getBooleanValue("colsearch") );
   		vu.setColResizable( vuRow.getBooleanValue("colresizable") );
   		vu.setEditable( vuRow.getBooleanValue("editable") );
   		vu.setNullable( vuRow.getBooleanValue("nullable") );
   		vu.setColVisible( vuRow.getBooleanValue("colvisible") );
   		return vu;
	} 	
}
