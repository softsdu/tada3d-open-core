package com.zlp.platform.common.filter;

import java.io.IOException; 
import java.util.HashMap; 

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;

/**
 * <!-- APP跨域访问 added by liyh 20190520 -->
 * @author Lenovo
 *
 */
public class CrossOriginFilter implements Filter {
	//跨域白名单
	private HashMap<String, Boolean> whiteListMap = new HashMap<String, Boolean>();   
	
    @Override  
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {  
        HttpServletRequest request = (HttpServletRequest)arg0;  
        HttpServletResponse response = (HttpServletResponse)arg1; 
        String originHeads = request.getHeader("Origin");
        
        if(this.whiteListMap.containsKey(originHeads) || this.whiteListMap.size() == 0){ 
        	response.setHeader("Access-Control-Allow-Origin", originHeads); 
	        response.setHeader("Access-Control-Allow-Credentials","true");
	        response.setHeader("Access-Control-Allow-Methods","POST, GET, PUT, OPTIONS, DELETE, PATCH");
	        //response.setHeader("Access-Control-Allow-Headers","authorization,Cookie,token,Origin, X-Requested-With, Content-Type, Accept,mid,X-Token,App-Key");
            response.setHeader("Access-Control-Allow-Headers","*");
        }
        else {  
        	throw new IOException("Cross Orgin Error");  
        } 
        if ("OPTIONS".equals(request.getMethod())){
        	response.setStatus(HttpStatus.SC_NO_CONTENT);
        }  
        else{
        	arg2.doFilter(arg0, arg1);
        }
    }  
  
    @Override
    public void destroy() {  
    } 
    
    public void init(FilterConfig fConfig) throws ServletException {  
	    String whiteListStr = fConfig.getInitParameter("whiteList");
	    if(whiteListStr.length() > 0){
		    String[] whiteListArray = whiteListStr.split(";"); 
		    for(int i = 0; i < whiteListArray.length; i++){
		    	whiteListMap.put(whiteListArray[i], true);
		    }
	    }
    } 
}
