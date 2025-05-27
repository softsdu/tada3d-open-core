package com.zlp.platform.dao.sys; 


import java.io.IOException;

import org.springframework.context.ApplicationContext;

public class ContextUtil {
    private static ApplicationContext context;
    public static ApplicationContext getContext() {
       return context;
    }
    public static Object getBean(String beanId){
        Object bean = context.getBean(beanId); 
        return bean;
    } 
    public static boolean containsBean(String beanId){
        return context.containsBean(beanId);
    } 
    public static void setContext(ApplicationContext ctx) throws IOException {
       context = ctx;
       ContextUtil.absolutePath = ContextUtil.getContext().getResource("").getFile().getAbsolutePath();
    }
    
    private static String absolutePath = null;
    public static String getAbsolutePath(){
    	return ContextUtil.absolutePath;
    } 
}