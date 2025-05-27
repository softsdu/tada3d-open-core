package com.zlp.platform.service;
    

import java.sql.SQLException;
 
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
 
import com.zlp.platform.common.NcpActionSupport; 
import com.zlp.platform.dao.db.IDBParserAccess;
 
public class CustomService extends NcpActionSupport implements ICustomService{
	
	protected static Logger logger=Logger.getLogger(CustomService.class);

	protected HibernateTransactionManager transactionManager; 
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
	
	protected IDBParserAccess getDBParserAccess(){
		return this.dBParserAccess;
	} 
}
