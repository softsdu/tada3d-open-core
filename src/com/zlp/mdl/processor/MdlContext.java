package com.zlp.mdl.processor;

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import com.zlp.platform.dao.sys.ContextUtil;

//系统初始化时，生成材质js和标准js added by ls 20230626
public class MdlContext {
	private static void InitData() throws Exception {
		Session dbSession = null;		
		try{
			HibernateTransactionManager transactionManager = (HibernateTransactionManager) ContextUtil.getBean("transactionManager");
			dbSession = transactionManager.getSessionFactory().openSession();		
			
			//材质
			IMaterialProcessor materialProcessor = (IMaterialProcessor)ContextUtil.getBean("materialProcessor");
			materialProcessor.setDBSession(dbSession);
			materialProcessor.generateAllStandardMaterialFiles();
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not init mdl data.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
		
		
	}

	public static void initContext() throws Exception {
		InitData();
	}

}
