package com.zlp.platform.common.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.zlp.mdl.processor.MdlContext;
import com.zlp.platform.core.ConfigContext; 
import com.zlp.platform.core.ExpressionContext;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.SystemContext;

public class ApplicationListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent event) {
       ServletContext context = event.getServletContext();
       try {
    	   initContextUtil(context);
			
    	   // 平台参数配置初始化
    	   ConfigContext.initContext();
           
    	   // 初始化系统配置
    	   SystemContext.initContext();
    	   
    	   // 表达式配置初始化
    	   ExpressionContext.initContext(); 
    	   
    	   // 设计系统初始化
    	   MdlContext.initContext();
          
       } catch (Exception ex) {
           ex.printStackTrace();
       }
    } 
      
    private void initContextUtil(ServletContext context) throws Exception{
           ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
           ContextUtil.setContext(ctx); 
    }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	} 
}