package com.zlp.platform.common.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zlp.platform.common.loader.InstallSqlLoader; 

public class PlatformLoaderListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void contextInitialized(ServletContextEvent pServletContextEvent) {
		//final String WEB_HOME = pServletContextEvent.getServletContext().getRealPath("/");
		//ResourcesLoader.load(WEB_HOME); 
		InstallSqlLoader.install();
	}

}
