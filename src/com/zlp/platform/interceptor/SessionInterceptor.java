package com.zlp.platform.interceptor;

import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.redis.RedisException;
import com.zlp.platform.common.redis.RedisSessionCacheClient;
import com.zlp.platform.dao.sys.ContextUtil;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import org.apache.struts2.ServletActionContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SessionInterceptor extends MethodFilterInterceptor {

	public RedisSessionCacheClient getRedisSessionCacheClient(){
		return (RedisSessionCacheClient)ContextUtil.getBean("redisSessionCacheClient");
	}  

	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		
		//如果现在是登录状态，那么刷新一下session的过期时间  modified by ls 20190730     
        try{
            Cookie[] cookies = request.getCookies();
        	String sessionId = NcpSession.getCookieSessionId(cookies); 
            if(sessionId.length() != 0){            	
            	RedisSessionCacheClient redisClient = this.getRedisSessionCacheClient(); 
            	redisClient.refreshExpire(sessionId);
            }            
        }
        catch(RedisException ex){
        	 throw new Exception(ex.getMessage(), ex);
        } 
        catch(NcpException ex){
       	 	throw new Exception(ex.getMessage(), ex);
        } 
		return invocation.invoke();
	}
}
