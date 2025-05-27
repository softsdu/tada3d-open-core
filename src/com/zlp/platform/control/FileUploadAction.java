package com.zlp.platform.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IAccessoryDao; 

public class FileUploadAction extends NcpActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FileUploadServiceAbstract fileUploadService;

	// 文件上传
	public void uploadFile() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		// 设置接收的编码格式
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
			
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpRequest().getCookies());
			
			while (it.hasNext()) {
				Object obit = it.next();
				if (obit instanceof DiskFileItem) {
					DiskFileItem item = (DiskFileItem) obit;

					// 如果item是文件上传表单域
					// 获得文件名及路径
					String fileName = item.getName();
					if (fileName != null) {
						String filterType = request.getParameter("filterType");
						String filterValue = request.getParameter("filterValue");
						String firstFileName = item.getName().substring(item.getName().lastIndexOf("\\") + 1);
						String id = fileUploadService.saveAccessory(session, item.getInputStream(), firstFileName, filterType, filterValue);
						ids.add(id);
					}
				}
			}
			resultHash.put("ids", ids);
			String returnStr = ServiceResultProcessor.createJsonResultStr(resultHash);
			response.getWriter().write(returnStr);
		} catch (org.apache.commons.fileupload.FileUploadException ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("UploadFile", "上传文件失败", ex);
			response.getWriter().write(ncpEx.toJsonString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			NcpException ncpEx = new NcpException("UploadFile", "上传文件失败", e);
			response.getWriter().write(ncpEx.toJsonString());
		} finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}

	}

	public FileUploadServiceAbstract getFileUploadService() {
		return fileUploadService;
	}

	public void setFileUploadService(FileUploadServiceAbstract fileUploadService) {
		this.fileUploadService = fileUploadService;
	}

}
