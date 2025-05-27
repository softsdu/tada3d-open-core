package com.zlp.platform.common.filter;

import java.io.IOException; 

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 

import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.redis.RedisException;
import com.zlp.platform.common.redis.RedisSessionCacheClient;
import com.zlp.platform.dao.sys.ContextUtil;

public class CheckLoginFilter implements Filter {

	public RedisSessionCacheClient getRedisSessionCacheClient(){
		return (RedisSessionCacheClient)ContextUtil.getBean("redisSessionCacheClient");
	}  

    @Override  
    public void init(FilterConfig arg0) throws ServletException {  
    }  
  
    @Override  
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)arg0;  
        HttpServletResponse response = (HttpServletResponse)arg1;  

        try{
            Cookie[] cookies = request.getCookies();
        	String sessionId = NcpSession.getCookieSessionId(cookies); 
            if(sessionId.length() != 0){            	
            	RedisSessionCacheClient redisClient = this.getRedisSessionCacheClient(); 
            	String userId = redisClient.getAttribute(sessionId, "userId");
	        	
	            if(userId == null || userId.length() == 0){  
	            	response.sendRedirect("login.html");
	            }
	            else { 
	            	redisClient.refreshExpire(sessionId);
	                arg2.doFilter(request, response);    
	            }  
            }
            else{
            	response.sendRedirect("login.html");
            }
        }
        catch(RedisException ex){
        	 throw new ServletException(ex.getMessage(), ex);
        } 
        catch(NcpException ex){
       	 	throw new ServletException(ex.getMessage(), ex);
        }
    }
  
    @Override  
    public void destroy() {} 
}
