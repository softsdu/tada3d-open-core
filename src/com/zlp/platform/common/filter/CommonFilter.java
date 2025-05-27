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

public class CommonFilter implements Filter {

    @Override  
    public void init(FilterConfig arg0) throws ServletException {  
    }  
  
    @Override  
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {  
        HttpServletRequest request = (HttpServletRequest)arg0;  
        HttpServletResponse response = (HttpServletResponse)arg1;
        arg2.doFilter(request, response);  
    }  
  
    @Override  
    public void destroy() {  
    } 
}
