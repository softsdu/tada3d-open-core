package com.zlp.external.service; 

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager; 
import com.zlp.external.processor.IResourceFileProcessor;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.dao.sys.ContextUtil; 

//获取resource file的基类  added by ls 20231101
public class GetResourceFile extends HttpServlet{
	
	private static final long serialVersionUID = -7259986437749890452L;

	protected Session openDBSession() throws SQLException{ 
		HibernateTransactionManager transactionManager = (HibernateTransactionManager)ContextUtil.getBean("transactionManager"); 
		return transactionManager.getSessionFactory().openSession(); 
	}	
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
           
        request.setCharacterEncoding("UTF-8");    
        IResourceFileProcessor resourceFileProcessor = null;
        Session dbSession = null;
        try { 
    		NcpSession session = new NcpSession(request.getCookies(), false); 
    		resourceFileProcessor = (IResourceFileProcessor)ContextUtil.getBean("resourceFileProcessor");    
    		dbSession = this.openDBSession();
    		resourceFileProcessor.setDBSession(dbSession);

            String code = this.getParameterValue(request, "code");
            if(code == null){
                String id = this.getParameterValue(request, "id");
                byte[] data = this.getResDataById(resourceFileProcessor, id);
                this.writeResponse(resourceFileProcessor, response, data);
            }
            else{
                byte[] data = this.getResData(resourceFileProcessor, code);
                this.writeResponse(resourceFileProcessor, response, data);
            }      
        }
        catch (Exception e) { 
			e.printStackTrace();  
        	throw new IOException(e.getMessage()); 
		}     
		finally{
			if(dbSession != null){ 
				dbSession.close();  
			}
		}   
    } 
    
    protected void writeResponse(IResourceFileProcessor resourceFileProcessor, HttpServletResponse response, byte[] data) throws Exception{ 
    	response.setContentType("text/html;charset=UTF-8");
        OutputStream out = null;
        try { 
            out = response.getOutputStream();           
            out.write(data);
            out.flush();
        }
        catch (Exception e) { 
			throw e; 
		}   
		finally{ 
			if(out != null){
	            out.close();
			}
		}       
    }
    
    private byte[] getResDataById(IResourceFileProcessor resourceFileProcessor, String id) throws Exception{
    	return null;
    }
    
    private byte[] getResData(IResourceFileProcessor resourceFileProcessor, String code) throws Exception{
    	return null;
    }
    
    protected String getParameterValue(HttpServletRequest request, String parameterName) throws UnsupportedEncodingException{
    	String paramValue = "";
    	switch(request.getMethod()){
			case "POST": {
				paramValue = request.getParameter(parameterName);  
				break;
			}
			case "GET":
			default:{
				paramValue = request.getParameter(parameterName);   
				break;
			}
    	} 
    	return paramValue;
    }
 
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {   
        doGet(req, resp);    
    }  
}
