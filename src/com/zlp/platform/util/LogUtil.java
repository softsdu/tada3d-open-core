package com.zlp.platform.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;

/**
 * @fuc：系统（数据库）日志功能
 * @author liyh
 * @date 2019/01/04
 */
public class LogUtil {
	
	private static Logger logger = Logger.getLogger(LogUtil.class);                 

    public static void log(String log_user_xid,String log_address,String log_event,String log_type) {
		Session dbSession = null;
		
		try {
			
			logger.info("log_type:" + log_type);
			logger.info("log_user_xid:" + log_user_xid);
			logger.info("log_event:" + log_event);
			
			//1-用户信息保存
			Data userData = DataCollection.getData("sys_loginfo");
			if(userData!=null){
				HashMap<String, Object> uFieldValues = new HashMap<String, Object>();
				
				//账号信息
				uFieldValues.put("log_user_xid", log_user_xid);
				uFieldValues.put("log_address", log_address);
				uFieldValues.put("log_event", log_event);
				uFieldValues.put("log_type", log_type);
				uFieldValues.put("log_time",  new Date());
				
				HibernateTransactionManager transactionManager = (HibernateTransactionManager)ContextUtil.getBean("transactionManager");  
				dbSession = transactionManager.getSessionFactory().openSession();
				IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
				dbParserAccess.insertByData(dbSession, userData, uFieldValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
    }
                           
}
