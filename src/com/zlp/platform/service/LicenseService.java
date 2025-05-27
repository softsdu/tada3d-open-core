package com.zlp.platform.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.LicenseChecker;
import com.zlp.platform.common.LicenseGenerator;
import com.zlp.platform.common.LicenseType;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.util.CommonFunction;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport; 

//@WebService
//@SOAPBinding(style = Style.RPC)
public class LicenseService extends NcpActionSupport implements ILicenseService { 
	private static Logger logger = Logger.getLogger(LicenseService.class); 

	@Override
	public String getLicenseInfo() {

		Session dbSession = null;
		try {   
		    String currentSysInfo = LicenseChecker.getCurrentSysInfo();
			String licenseSN = LicenseChecker.getLicenseSN(); 
			String licenseType = LicenseChecker.getLicenseType(licenseSN);
			String licenseSysInfo = LicenseChecker.getLicenseSysInfo(licenseSN); 		    

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("currentSysInfo", CommonFunction.encode(currentSysInfo));
			resultHash.put("licenseSN", CommonFunction.encode(licenseSN)); 
			resultHash.put("licenseType", CommonFunction.encode(licenseType)); 
			resultHash.put("licenseSysInfo", CommonFunction.encode(licenseSysInfo));  
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		    
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("getLicenseInfo", "获取授权信息失败", ex);
			this.addResponse(ncpEx.toJsonString());
		} 
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	} 
	@Override
	public String generateSN() {

		Session dbSession = null;
		try {   
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			String publicKey = CommonFunction.decode(requestObj.getString("publicKey"));
			String licenseType = CommonFunction.decode(requestObj.getString("licenseType"));
			String sysInfo = CommonFunction.decode(requestObj.getString("sysInfo")); 
			
			String licenseSN = LicenseGenerator.generateLicenseSN(licenseType, sysInfo, publicKey);
		    

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("licenseSN", CommonFunction.encode(licenseSN)); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		    
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("generateSN", "生成授权码失败", ex);
			this.addResponse(ncpEx.toJsonString());
		} 
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	} 
}
