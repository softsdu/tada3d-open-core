package com.zlp.platform.common.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

public class InstallSqlLoader {
	private final static Log logger = LogFactory.getLog(InstallSqlLoader.class);
	
	public static void install(){
		try{
			//String countSql = "select count(*) from d_user";
			//JdbcUtils.count(countSql).intValue();
		}catch(Exception e){
			//e.printStackTrace();
			initSql();
		}
	}
	
	private static void initSql(){
		logger.info("开始初始化脚本...");
	}
	
}
