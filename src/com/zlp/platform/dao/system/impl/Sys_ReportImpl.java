package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_Report;
import com.zlp.platform.model.sysmodel.ParamWin;
import com.zlp.platform.model.sysmodel.ParamWinCollection;
import com.zlp.platform.model.sysmodel.Report;
import com.zlp.platform.model.sysmodel.ReportCollection;

public class Sys_ReportImpl extends DataBaseDao implements Sys_Report{

	private ReportCollection reportCollection = null;
	public void setReportCollection(ReportCollection reportCollection){
		this.reportCollection = reportCollection;
	} 
	
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("generateJs".equals(actionName)){
				return generateJs(session, customParam);
			}
			return null;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String reportId = customParam.getString("reportId");	 
		Report report = reportCollection.reloadSheetFromDB(reportId);		
		String jsFilePath = ContextUtil.getAbsolutePath() + "\\js\\report\\" + report.getName() + ".js";
		List<String> errors = this.validateReportModel(report);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsByReport(report, jsFilePath);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	private List<String> validateReportModel(Report report){
		List<String> errors = new ArrayList<String>();
		  		    
		ParamWin paramWin = ParamWinCollection.getParamWin(report.getParamWinName());
		if(paramWin == null){
			errors.add("参数窗口 " + report.getParamWinName() + " 不存在.");
		} 
		return errors;
	}	
	
	private void generateJsByReport(Report report, String jsFilePath) throws Exception{  

		StringBuilder jsStr = new StringBuilder("reportModels." + report.getName() + " = {\r\n");
		jsStr.append("  id:\"" + report.getId() + "\",\r\n");
		jsStr.append("  name:\"" + report.getName() + "\",\r\n"); 
		jsStr.append("  reportName:\"" + report.getReportName() + "\",\r\n"); 
		jsStr.append("  paramWinName:\"" + report.getParamWinName() + "\",\r\n");   
		jsStr.append("  isAuto:" + report.getIsAuto() + "\r\n");    
		jsStr.append("}\r\n");
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	} 
}
