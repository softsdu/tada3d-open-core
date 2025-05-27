package com.zlp.platform.service; 

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IAccessoryDao;

public class UploadFile extends HttpServlet{  
	private static final long serialVersionUID = -916959716711563332L;

	protected Session openDBSession() throws SQLException{ 
		HibernateTransactionManager transactionManager = (HibernateTransactionManager)ContextUtil.getBean("transactionManager"); 
		return transactionManager.getSessionFactory().openSession(); 
	}	
	
    //实现多文件的同时上传   
    public void doGet(HttpServletRequest request,  
            HttpServletResponse response) throws ServletException, IOException {  
          
        //设置接收的编码格式  
        request.setCharacterEncoding("UTF-8");        
        Session dbSession = null;
        try {
        	HashMap<String, Object> resultHash = new HashMap<String, Object>();
        	List<String> ids = new ArrayList<String>();
            DiskFileItemFactory fac = new DiskFileItemFactory();  
            ServletFileUpload upload = new ServletFileUpload(fac);  
            upload.setHeaderEncoding("UTF-8");  
            // 获取多个上传文件  
            List<FileItem> fileList = upload.parseRequest(request);  
            // 遍历上传文件写入磁盘  
            Iterator<FileItem> it = fileList.iterator();  
    		IAccessoryDao accessoryDao = (IAccessoryDao)ContextUtil.getBean("accessoryDao"); 
    		dbSession = this.openDBSession();
    		accessoryDao.setDBSession(dbSession);

        	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
    		NcpSession session = new NcpSession(request.getCookies());
    		
            while (it.hasNext()) {  
            	Object obit = it.next();
            	if(obit instanceof DiskFileItem){ 
	                DiskFileItem item = (DiskFileItem) obit;  
	                  
	                // 如果item是文件上传表单域     
	                // 获得文件名及路径     
	                String fileName = item.getName();  
	                if (fileName != null) {  
	                    String filterType = request.getParameter("filterType");  
	                    String filterValue = request.getParameter("filterValue");  
	                    String firstFileName=item.getName().substring(item.getName().lastIndexOf("\\")+1);  
	                    String id = accessoryDao.saveAccessory(session, item.getInputStream(), firstFileName, filterType, filterValue);
	            		ids.add(id);
	                }   
            	}
            }
            resultHash.put("ids", ids);
			String returnStr = ServiceResultProcessor.createJsonResultStr(resultHash);
            response.getWriter().write(returnStr);
        }
        catch (org.apache.commons.fileupload.FileUploadException ex) {
        	ex.printStackTrace();   
			NcpException ncpEx = new NcpException("UploadFile", "上传文件失败", ex);
			response.getWriter().write(ncpEx.toJsonString());   
		} 
        catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
			NcpException ncpEx = new NcpException("UploadFile", "上传文件失败", e);
			response.getWriter().write(ncpEx.toJsonString());   
		}   
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}       
    }  
 
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {   
        doGet(req, resp);    
    }  
}
