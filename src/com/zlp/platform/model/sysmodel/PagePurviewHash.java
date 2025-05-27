package com.zlp.platform.model.sysmodel;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap; 

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.sys.Role;

//页面权限
public class PagePurviewHash  implements InitializingBean{
	private static Logger logger=Logger.getLogger(PagePurviewHash.class);


	//根据json获取数据
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
 
	//Data对象
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> pagePurviews ; 
	public static boolean isEnable(String pageUrl, List<Role> roles){
		if(PagePurviewHash.pagePurviews.containsKey(pageUrl)){
			ConcurrentHashMap<String, Boolean> rolePurview = PagePurviewHash.pagePurviews.get(pageUrl);
			for(Role role : roles){
				if(rolePurview.containsKey(role.getId())){
					return true;
				}
			}
			return false;
		}
		else{
			return true;
		} 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	}  
	public void initFromDB() throws Exception {
		logger.info("init page purview hash.");
		Session dbSession = null;			
		try{
			ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> purviews = new ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>>();
			
			SelectSqlParser pageSql = this.getDBParserAccess().getSqlParser("sys.purview.page");
			SelectSqlParser pageRoleSql = this.getDBParserAccess().getSqlParser("sys.purview.pagerole");

			dbSession = this.transactionManager.getSessionFactory().openSession();	
		   	DataTable pageDt = this.getDBParserAccess().getDtBySqlParser(dbSession, pageSql, -1, -1, null, "", "");
		   	DataTable pageRoleDt = this.getDBParserAccess().getDtBySqlParser(dbSession, pageRoleSql, -1, -1, null, "", "" ); 
		   	HashMap<String, String> idToPages = new HashMap<String, String>();
		    for(DataRow pRow : pageDt.getRows()){
		    	String menuId = pRow.getStringValue("id");
		    	String pageUrl = pRow.getStringValue("pageurl");
		    	ConcurrentHashMap<String, Boolean> roles = new ConcurrentHashMap<String, Boolean>(); 
		    	purviews.put(pageUrl, roles); 
		    	idToPages.put(menuId, pageUrl);
		    }	 
		   	 
		   	for(DataRow prRow : pageRoleDt.getRows()){ 
		   		String menuId = prRow.getStringValue("menuid");
		   		String roleId = prRow.getStringValue("roleid");
		   		ConcurrentHashMap<String, Boolean> roles  =  purviews.get(idToPages.get(menuId));
		   		roles.put(roleId, true);
		   	}
		   	PagePurviewHash.pagePurviews = purviews;
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init page purview hash error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}	 
}
