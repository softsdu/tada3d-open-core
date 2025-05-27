package com.zlp.platform.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IAccessoryDao;

public class GetImage extends HttpServlet{     
	private static final long serialVersionUID = 1L;

	protected Session openDBSession() throws SQLException{ 
		HibernateTransactionManager transactionManager = (HibernateTransactionManager)ContextUtil.getBean("transactionManager"); 
		return transactionManager.getSessionFactory().openSession(); 
	}	
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {           
        request.setCharacterEncoding("UTF-8");    
        IAccessoryDao accessoryDao = null;
        Session dbSession = null;
        try {
        	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
    		NcpSession session = new NcpSession(request.getCookies()); 
    		
    		accessoryDao = (IAccessoryDao)ContextUtil.getBean("accessoryDao"); 
    		dbSession = this.openDBSession();
    		accessoryDao.setDBSession(dbSession);

            String accessoryId = this.getParameterValue(request, "id");   
            
            byte[] data = this.getImageData(accessoryDao, accessoryId);
            this.writeResponse(accessoryDao, response, data);
        }
        catch (Exception e) { 
			e.printStackTrace(); 
            try {
            	byte[] data = this.getImageData(accessoryDao, null);
				this.writeResponse(accessoryDao, response, data);
			} 
            catch (Exception e1) {
            	throw new IOException(e.getMessage());
			}
		}     
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}   
    } 
    
    private void writeResponse(IAccessoryDao accessoryDao, HttpServletResponse response, byte[] data) throws Exception{  
        OutputStream out = null;
        try { 
            out = response.getOutputStream();
            response.setContentType("image/png"); 
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
    
    private byte[] getImageData(IAccessoryDao accessoryDao, String accessoryId) throws Exception{
        FileInputStream fis = null;
        try {        	
	        String imageFilePath = accessoryDao.getFilePathById(accessoryId);
	        
	        File file = new File(imageFilePath);
	        fis = new FileInputStream(file);
	
	        long size = file.length();
	        byte[] temp = new byte[(int) size];
	        fis.read(temp, 0, (int) size); 
	        byte[] data = temp;
			return data;  
        }
        catch (Exception e) { 
			throw e;
		}   
		finally{
			if(fis != null){
				fis.close();
			} 
		}     
    }
    
    private String getParameterValue(HttpServletRequest request, String parameterName){
    	switch(request.getMethod()){
			case "POST": 
		    	return request.getParameter(parameterName);  
			case "GET":
			default:
		    	return request.getParameter(parameterName);   
    	}
    }
 
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {   
        doGet(req, resp);    
    }  
}