package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_View;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField; 
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.model.sysmodel.ViewDispunit; 

public class Sys_ViewImpl extends DataBaseDao implements Sys_View {

	private ViewCollection viewCollection = null;
	public void setViewCollection(ViewCollection viewCollection){
		this.viewCollection = viewCollection;
	} 

	//生成js和jsp、popPage modified by ls 20190618
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("generateJs".equals(actionName)){
				return generateJs(session, customParam);
			}
			else if("generatePage".equals(actionName)){
				return generatePage(session, customParam);
			}
			else if("generatePopPage".equals(actionName)){
				return generatePopPage(session, customParam);
			}
			return null;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	//生成页面
	private HashMap<String, Object> generatePage(INcpSession session, JSONObject customParam) throws Exception{
		String viewId =  customParam.getString("viewId");	 
		View view = viewCollection.reloadViewFromDB(viewId);		
		HashMap<String, List<String>> msgs = this.validateViewModel(view);
		List<String> errors = msgs.get("errors");
		List<String> warnings = msgs.get("warnings");
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			generatePageByView(view);
			resultMap.put("succeed", "true");
			resultMap.put("warnings", warnings);
		}
		else { 		
			resultMap.put("succeed", "false");
			resultMap.put("errors", errors);
			resultMap.put("warnings", warnings);
		}
		return resultMap;
	}
	
	public void generatePageByView(View view) throws Exception{
		String templateFilePath = ConfigContext.getConfigMap().get(ZlpState.MODEL_PAGES_TEMPLATES_VIEWGRID); 
		String pageFilePath = ContextUtil.getAbsolutePath() + ZlpState.GENERATED_MODEL_PAGES_DIR_OF_VIEW + view.getName() + ".jsp";
		generatePageByView(view, templateFilePath, pageFilePath);
	}
	
	private void generatePageByView(View view, String templateFilePath, String pageFilePath) throws Exception{
		Data data = DataCollection.getData(view.getDataName());
		
		FileOperate fo = new FileOperate();
		String pageText = fo.readTxt(templateFilePath, "utf-8");

		pageText = pageText.replace("<%viewTitle%>", view.getTitle());
		pageText = pageText.replace("<%viewName%>", view.getName());
		pageText = pageText.replace("<%dataName%>", data.getName()); 
		
		fo.createFile(pageFilePath, pageText, "utf-8");
	} 
	
	//生成弹出所用的view的页面
	//TODO: 现在还未自成生成，现在是手工操作
	private HashMap<String, Object> generatePopPage(INcpSession session, JSONObject customParam) throws Exception{
		String viewId = customParam.getString("viewId");	 
		View view = viewCollection.reloadViewFromDB(viewId);		
		String modelPageFilePath = ContextUtil.getAbsolutePath() + "\\page\\pop\\viewPageModel.jsp";
		String pageFilePath = ContextUtil.getAbsolutePath() + "\\page\\pop\\" + view.getName() + "_View.js";
		HashMap<String, List<String>> msgs = this.validateViewModel(view);
		List<String> errors = msgs.get("errors");
		List<String> warnings = msgs.get("warnings");
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generatePopPageByView(view, modelPageFilePath, pageFilePath);		 
			resultMap.put("succeed", "true");
			resultMap.put("warnings", warnings);
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
			resultMap.put("warnings", warnings);
		}
		return resultMap;		
	}
	
	private void generatePopPageByView(View view, String modelPageFilePath, String pageFilePath) throws Exception{
		StringBuilder pageStr = new StringBuilder();
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(pageFilePath, pageStr.toString());
	} 
	//根据view的定义，生成前端需要的js
	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String viewId =  customParam.getString("viewId");	 
		View view = viewCollection.reloadViewFromDB(viewId);		
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.DATA_MODEL_PATH_OF_VIEW + view.getName() + ".js";
		HashMap<String, List<String>> msgs = this.validateViewModel(view);
		List<String> errors = msgs.get("errors");
		List<String> warnings = msgs.get("warnings");
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsByView(view, jsFilePath);
			resultMap.put("succeed", "true");
			resultMap.put("warnings", warnings);
		}
		else { 		
			resultMap.put("succeed", "false");
			resultMap.put("errors", errors);
			resultMap.put("warnings", warnings);
		}
		return resultMap;
	}
	
	private HashMap<String, List<String>> validateViewModel(View view){
		List<String> errors = new ArrayList<String>(); 
		List<String> warnings = new ArrayList<String>(); 
		
		Data data = DataCollection.getData(view.getDataName());
		if(data == null){
			errors.add("不存在名为 " + view.getDataName() + " 的数据模型.");		
		}
		else {
			HashMap<String, ViewDispunit> dispunits = view.getDispunits();
			for(String fieldName : dispunits.keySet()){
				DataField df = data.getDataField(fieldName);
				if(df == null){
					warnings.add("数据模型 " + data.getName() + " 中不存在名为 " + fieldName + " 的字段");
				}
			} 
		}
		HashMap<String, List<String>> msgHash = new HashMap<String, List<String>>();
		msgHash.put("errors", errors);
		msgHash.put("warnings", warnings);
		return msgHash;
	}	
	
	private void generateJsByView(View view, String jsFilePath) throws Exception{ 
		Data data = DataCollection.getData(view.getDataName());
		//ViewCollection viewCollection = (DownListCollection)ContextUtil.getBean("viewCollection"); 
		int dispunitCount = view.getDispunits().size();
		List<ViewDispunit> dispunitList = new ArrayList<ViewDispunit>();

		for(ViewDispunit dispunit : view.getDispunits().values()){ 
			for(int j=0;j<=dispunitList.size();j++){
				if(j==dispunitList.size()){
					dispunitList.add(dispunit);
					break;
				}
				else if(dispunitList.get(j).getColIndex() > dispunit.getColIndex()){
					dispunitList.add(j, dispunit);
					break;
				}
			}
		} 
		StringBuilder colModelStr = new StringBuilder();
		StringBuilder dispUnitModelStr = new StringBuilder();
		
		//增加多选列  
		colModelStr.append("    {"); 
		colModelStr.append("name:\"ncpRowSelect\", ");
		colModelStr.append("label:\" \", ");
		colModelStr.append("width:30, ");
		colModelStr.append("hidden:false, ");	
		colModelStr.append("sortable:false, ");	
		colModelStr.append("search:false, ");	
		colModelStr.append("resizable:false, ");	
		colModelStr.append("editable:false, ");	
		colModelStr.append("canEdit:false, ");	
		colModelStr.append("nullable:true, ");	
		colModelStr.append("align:'center', ");	
		colModelStr.append("edittype:\"checkbox\", ");
		colModelStr.append("dispunitType:\"checkbox\"");
		colModelStr.append("},\r\n"); 
		
		//循环所有显示域
		for(int i = 0;i<dispunitCount;i++){
			ViewDispunit dispunit = dispunitList.get(i);
			DataField field = data.getDataField(dispunit.getName());
			String editType = "";
			String dispunitType = "";
			String align = "";
			if(field != null){
				switch(field.getValueType()){
					case String:
						editType = "text";
						dispunitType = "text";
						align = "left";
						break;
					case Decimal:
						editType = "text";
						dispunitType = "decimal";
						align = "right";
						break;
					case Date:
						editType = "text";
						dispunitType = "date";
						align = "left";
						break;
					case Time:
						editType = "text";
						dispunitType = "time";
						align = "left";
						break;
					case Boolean:
						editType = "checkbox";
						dispunitType = "checkbox";
						align = "center";
						break;
					default:
						break;
				}
				if("list".equals(field.getInputHelpType())){
					dispunitType = "list";
				}
				if("pop".equals(field.getInputHelpType())){
					dispunitType = "pop";
				}
			}

			colModelStr.append("    {"); 
			colModelStr.append("name:\"" + dispunit.getName() + "\", ");
			colModelStr.append("label:\"" + dispunit.getLabel() + "\", ");
			colModelStr.append("width:" + dispunit.getColWidth() + ", ");
			colModelStr.append("hidden:" + !dispunit.getColVisible() + ", ");	
			colModelStr.append("sortable:" + dispunit.getColSortable() + ", ");	
			colModelStr.append("search:" + dispunit.getColSearch() + ", ");	
			colModelStr.append("resizable:" + dispunit.getColResizable() + ", ");	
			colModelStr.append("editable:true, ");	
			colModelStr.append("align:'" + align + "', ");	
			colModelStr.append("canEdit:" + dispunit.getEditable() + ", ");	
			colModelStr.append("nullable:" + dispunit.getNullable()+ ", ");	
			colModelStr.append("edittype:\"" + editType + "\", ");

			if(field != null){
				switch(field.getValueType()){
				
					case Date:
						colModelStr.append("formatter:dateFormater, ");
						break;
					case Time:
						colModelStr.append("formatter:timeFormater, ");
						break;
					default:
						break;			
				}
			}
			
			colModelStr.append("dispunitType:\"" + dispunitType + "\"");
			colModelStr.append("},\r\n");	

			dispUnitModelStr.append("    {"); 
			dispUnitModelStr.append("name:\"" + dispunit.getName() + "\", ");
			dispUnitModelStr.append("label:\"" + dispunit.getLabel() + "\", "); 	
			dispUnitModelStr.append("editable:" + dispunit.getEditable() + ","); 
			dispUnitModelStr.append("nullable:" + dispunit.getNullable()+ ", ");	
			dispUnitModelStr.append("hidden:" + !dispunit.getColVisible() + ", ");	
			dispUnitModelStr.append("dispunitType:\"" + dispunitType + "\", ");
			dispUnitModelStr.append("}" + (i == dispunitCount-1 ? "\r\n" : ",\r\n"));	
		}
		
		//增加多选列  
		colModelStr.append("    {"); 
		colModelStr.append("name:\"ncpRowOperate\", ");
		colModelStr.append("label:\"操作\", ");
		colModelStr.append("width:150, ");
		colModelStr.append("hidden:false, ");	
		colModelStr.append("sortable:false, ");	
		colModelStr.append("search:false, ");	
		colModelStr.append("resizable:true, ");	
		colModelStr.append("editable:false, ");	
		colModelStr.append("canEdit:false, ");	
		colModelStr.append("nullable:true, ");	
		colModelStr.append("align:'center'");	 
		colModelStr.append("}\r\n"); 
		
		StringBuilder jsStr = new StringBuilder("viewModels." + view.getName() + " = {\r\n");
		jsStr.append("  id:\"" + view.getId() + "\",\r\n");
		jsStr.append("  name:\"" + view.getName() + "\",\r\n"); 
		jsStr.append("  dataName:\"" + view.getDataName() + "\",\r\n"); 
		jsStr.append("  title:\"" + view.getTitle() + "\",\r\n"); 
		jsStr.append("  colModel:[\r\n");
		jsStr.append(colModelStr.toString()); 
		jsStr.append("  ],\r\n"); 
		jsStr.append("  dispUnitModel:[\r\n");
		jsStr.append(dispUnitModelStr.toString()); 
		jsStr.append("  ]\r\n"); 
		jsStr.append("}\r\n");
		
		//jsStr.append("document.writeln(\"<script type=\\\"text/javascript\\\" src=\\\"../../js/data/" + view.getDataName() + ".js\\\" />\");");
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	} 
}
