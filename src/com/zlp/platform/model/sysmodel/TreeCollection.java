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

public class TreeCollection implements InitializingBean{
	private static Logger logger=Logger.getLogger(TreeCollection.class);

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
		TreeCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return TreeCollection.sqls;
	} 
	
	//Tree对象
	private static HashMap<String, Tree> treeObjects ; 
	public static Tree getTree(String name){
		return TreeCollection.treeObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public Tree reloadTreeFromDB(String treeId) throws Exception{
		SelectSqlParser sys_treeSql = this.getDBParserAccess().getSqlParser("sys.model.tree");
		 
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", treeId); 
		Session dbSession = null;
		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();		
		   	DataTable treeDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_treeSql, -1, -1, params, "t.id = " + SysConfig.getParamPrefix() + "id", "");//+SysConfig.getParamPrefix()+"treeId", "");
		   	List<DataRow> tRows = treeDt.getRows();
		   	if(tRows.size()==0){
		   		throw new Exception("none tree row, id = " + treeId);
		   	}
		   	else {
		   		Tree tree = this.createTreeObject(tRows.get(0)); 
	
		   		TreeCollection.treeObjects.put(tree.getName(), tree);		
		   		return tree;
		   	}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload down tree model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	} 
	
	public void initFromDB() throws Exception {
		logger.info("init Tree Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_treeSql = this.getDBParserAccess().getSqlParser("sys.model.tree");
 
		   	DataTable treeDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_treeSql, -1, -1, null, "", "");
		    	
		   	TreeCollection.treeObjects = new HashMap<String, Tree>(); 
		    for(DataRow tRow : treeDt.getRows()){
		    	Tree tree = createTreeObject(tRow); 
		    	TreeCollection.treeObjects.put(tree.getName(), tree); 
		    }	  
		    
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Tree Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}    
	}	 
	
	private Tree createTreeObject(DataRow tRow){
    	Tree tree = new Tree();
    	tree.setId( tRow.getStringValue("id"));
    	tree.setName( tRow.getStringValue("name")); 
    	tree.setTitle( tRow.getStringValue("title")); 
    	tree.setViewName( tRow.getStringValue("viewname")); 
   		tree.setLabelField( tRow.getStringValue("labelfield"));
   		tree.setSortField( tRow.getStringValue("sortfield"));
   		tree.setIsLeafField( tRow.getStringValue("isleaffield"));
   		tree.setParentPointerField( tRow.getStringValue("parentpointerfield"));
    	return tree;
    }
}
