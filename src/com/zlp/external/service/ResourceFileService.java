package com.zlp.external.service;
  
import java.sql.SQLException;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import com.zlp.external.processor.IResourceFileProcessor; 
import com.zlp.platform.common.NcpActionSupport;
 
public class ResourceFileService extends NcpActionSupport implements IResourceFileService{

	private static final long serialVersionUID = 3224939166498956798L;
	
	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
	 
	private IResourceFileProcessor resourceFileProcessor;
	public void setResourceFileProcessor(IResourceFileProcessor resourceFileProcessor){
		this.resourceFileProcessor = resourceFileProcessor;
	}
	protected IResourceFileProcessor getResourceFileProcessor(){
		return this.resourceFileProcessor;
	}
}