package com.zlp.platform.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zlp.platform.common.NcpNonePurviewException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.model.sysmodel.PagePurviewHash;

public class CheckPagePurviewFilter implements Filter {
    @Override  
    public void init(FilterConfig arg0) throws ServletException {  
    }   

	private PagePurviewHash pagePurviewHash = null; 
	
    @Override  
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException { 
    	if(this.pagePurviewHash == null){
    		this.pagePurviewHash = (PagePurviewHash)ContextUtil.getBean("pagePurviewHash"); 
    	}
    	
    	HttpServletRequest request = (HttpServletRequest)arg0;  
        HttpServletResponse response = (HttpServletResponse)arg1;  
        String url = "../" + request.getRequestURI().substring(request.getContextPath().length() + 6); 


		NcpSession session;
		try {
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			session = new NcpSession(request.getCookies());
		} catch (Exception e) { 
			e.printStackTrace();
        	throw new NcpNonePurviewException("NonePurview", url, null);
		}
        
        if(PagePurviewHash.isEnable(url, session.getRoleList())){
            arg2.doFilter(request, response); 
        }
        else{
        	throw new NcpNonePurviewException("NonePurview", url, null);
        }
        
        
        
    }  
  
    @Override  
    public void destroy() {  
    } 
}
