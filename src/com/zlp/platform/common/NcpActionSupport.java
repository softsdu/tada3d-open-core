package com.zlp.platform.common;
 
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class NcpActionSupport extends ActionSupport {

	protected String requestParam = "";
	public void setRequestParam(String requestParam){
		this.requestParam = requestParam;
	}
	protected String result = "";
	public String getResult(){
		return this.result;
	}

	protected ActionContext getContext(){
		return  ActionContext.getContext();
	}

	protected HttpServletRequest getHttpRequest(){
		return (HttpServletRequest)this.getContext().get(ServletActionContext.HTTP_REQUEST);
	}
	protected HttpServletResponse getHttpResponse(){
		return (HttpServletResponse)this.getContext().get(ServletActionContext.HTTP_RESPONSE);
	}

	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
	protected Cookie[] getHttpCookies(){
		return this.getHttpRequest().getCookies();
	} 	 
	
	protected void addResponse(String info) {
		try {
			HttpServletResponse response = this.getHttpResponse();
			response.setContentType("text/xml;charset=utf-8"); //更改字符编码  
			response.getWriter().println(info);            //这句话是把结果返回（这个是关键）
		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	     
}
