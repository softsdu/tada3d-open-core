package com.zlp.platform.dao.sys;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public final class SystemContext {
	private static final Logger logger = LogManager
			.getLogger(SystemContext.class.getName());
	private static boolean isInited = false;
	public static final String PROPERTIES_FILE = "system.properties";

	public static final String PROP_DRIVER_CLASS_NAME = "driverClassName";
	public static final String PROP_SPIDER_SCHEDULED_CRON_EXPR = "SPIDER_SCHEDULED_CRON_EXPR";
	private static Properties sysProperties = new Properties();

	public synchronized static boolean initContext() {
		if (isInited) {
			return true;
		}
		logger.info("init SystemContext...");
		InputStream is = SystemContext.class.getClassLoader()
				.getResourceAsStream(PROPERTIES_FILE);
		try {
			sysProperties.load(is);
		} catch (IOException e) {
			throw new RuntimeException(PROPERTIES_FILE + " file load error");

		}
		isInited = true;
		return true; 
	} 

	public static Properties getSysProperties() {
		return sysProperties;
	}

	public static void setSysProperties(Properties sysProperties) {
		SystemContext.sysProperties = sysProperties;
		
	}

	public synchronized static String getDriverClassName() {
		return sysProperties.getProperty(PROP_DRIVER_CLASS_NAME);
	}

}
