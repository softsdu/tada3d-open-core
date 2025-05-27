package com.zlp.platform.core;
 
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger; 
import com.zlp.platform.expression.definition.Environment; 

/**
 * @Description: 快速开发工具配置 
 * 项目名称：platform   
 */
public class ExpressionContext { 
	
	private static final Logger logger = LogManager.getLogger(ExpressionContext.class.getName()); 
	private static boolean isInited = false;
	
	public synchronized static boolean initContext() {
		if (isInited) {
			return true;
		}
		logger.info("init ExpressionContext...");
		try {
			Environment.initFromFile();
		} catch (Exception e) {
			throw new RuntimeException("init expression context error",e);
		}
		 
		
		isInited = true;
		return true;
	}  
}
