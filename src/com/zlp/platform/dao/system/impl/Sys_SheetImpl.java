package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_Sheet;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField; 
import com.zlp.platform.model.sysmodel.Sheet;
import com.zlp.platform.model.sysmodel.SheetCollection;
import com.zlp.platform.model.sysmodel.SheetPart;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.model.sysmodel.ViewDispunit;

public class Sys_SheetImpl extends DataBaseDao implements Sys_Sheet {

	private SheetCollection sheetCollection = null;
	public void setSheetCollection(SheetCollection sheetCollection){
		this.sheetCollection = sheetCollection;
	} 
	
	//生成js、jsp页面 modified by ls 20190618
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
			return null;
		}
		catch(Exception ex){
	    	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	//生成页面
	private HashMap<String, Object> generatePage(INcpSession session, JSONObject customParam) throws Exception{ 
		
		String sheetId = customParam.getString("sheetId");	 
		Sheet sheet = sheetCollection.reloadSheetFromDB(sheetId);		 
		List<String> errors = this.validateSheetModel(sheet);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generatePageBySheet(sheet);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}

	private void generatePageBySheet(Sheet sheet) throws Exception{
		String templateSheetGridFilePath = ConfigContext.getConfigMap().get(ZlpState.MODEL_PAGES_TEMPLATES_SHEETGRID); 
		String templateSheetSheetFilePath = ConfigContext.getConfigMap().get(ZlpState.MODEL_PAGES_TEMPLATES_SHEETSHEET); 
		String gridPageFilePath = ContextUtil.getAbsolutePath() + ZlpState.GENERATED_MODEL_PAGES_DIR_OF_SHEET + sheet.getName() + ".jsp";
		String sheetPageFilePath = ContextUtil.getAbsolutePath() + ZlpState.GENERATED_MODEL_PAGES_DIR_OF_SHEET + sheet.getName() + "_Sheet.jsp";
		generateSheetSheetPageBySheet(sheet, templateSheetSheetFilePath, sheetPageFilePath);
		generateSheetGridPageBySheet(sheet, templateSheetGridFilePath, gridPageFilePath);
	}
	
	private void generateSheetSheetPageBySheet(Sheet sheet, String templateSheetSheetFilePath, String pageFilePath) throws Exception{
		FileOperate fo = new FileOperate();
		String pageText = fo.readTxt(templateSheetSheetFilePath, "utf-8");
		pageText = pageText.replace("<%sheetTitle%>", sheet.getTitle());
		pageText = pageText.replace("<%sheetName%>", sheet.getName());
	    SheetPart mainPart = sheet.getMainPart();
	    pageText = pageText.replace("<%parentViewModel%>", mainPart.getViewName()); 
		View mainPartView = ViewCollection.getView(mainPart.getViewName());
		Data mainPartData = DataCollection.getData(mainPartView.getDataName());
		pageText = pageText.replace("<%parentDataModel%>", mainPartView.getDataName());
		List<SheetPart> childParts = mainPart.getChildParts();
		pageText = pageText.replace("<%mainRegion%>", childParts.size() == 0 ? "center" : "north"); 
				
		//处理表头字段录入控件
		StringBuilder allRowText = new StringBuilder();
		
		String beginInputRow = "<%begin-inputRow%>";
		String endInputRow = "<%end-inputRow%>";
		int inputRowBeginIndex = pageText.indexOf(beginInputRow);
		int inputRowEndIndex = pageText.indexOf(endInputRow);
		String inputRowTextTemplate = pageText.substring(inputRowBeginIndex + beginInputRow.length(), inputRowEndIndex);

		String beginInputCell = "<%begin-inputCell%>";
		String endInputCell = "<%end-inputCell%>";
		int inputCellBeginIndex = inputRowTextTemplate.indexOf(beginInputCell);
		int inputCellEndIndex = inputRowTextTemplate.indexOf(endInputCell);
		String inputCellTextTemplate = inputRowTextTemplate.substring(inputCellBeginIndex + beginInputCell.length(), inputCellEndIndex);
		
		String beginEmptyCell= "<%begin-emptyCell%>";
		String endEmptyCell = "<%end-emptyCell%>";
		int emptyCellBeginIndex = inputRowTextTemplate.indexOf(beginEmptyCell);
		int emptyCellEndIndex = inputRowTextTemplate.indexOf(endEmptyCell);
		String emptyCellTextTemplate = inputRowTextTemplate.substring(emptyCellBeginIndex + beginEmptyCell.length(), emptyCellEndIndex);

		String inputRowTextPrefix = inputRowTextTemplate.substring(0, inputCellBeginIndex);
		String inputRowTextPostfix = inputRowTextTemplate.substring(emptyCellEndIndex + endEmptyCell.length());

		String beginTextareaRow = "<%begin-textareaRow%>";
		String endTextareaRow = "<%end-textareaRow%>";
		int textareaRowBeginIndex = pageText.indexOf(beginTextareaRow);
		int textareaRowEndIndex = pageText.indexOf(endTextareaRow);
		String textareaRowTextTemplate = pageText.substring(textareaRowBeginIndex + beginTextareaRow.length(), textareaRowEndIndex);

		List<ViewDispunit> dispUnitList = new ArrayList<ViewDispunit>();
		for(ViewDispunit dispunit : mainPartView.getDispunits().values()){ 
			for(int j = 0;j <= dispUnitList.size();j++){
				if(j == dispUnitList.size()){
					dispUnitList.add(dispunit);
					break;
				}
				else if(dispUnitList.get(j).getColIndex() > dispunit.getColIndex()){
					dispUnitList.add(j, dispunit);
					break;
				}
			}
		}  
		String inputRowAllCellText = "";
		for(ViewDispunit dispUnit : dispUnitList){ 
			if(dispUnit.getColVisible()){
				DataField field = mainPartData.getDataField(dispUnit.getName());
				if(field.getValueLength() > 255){
					//textarea
					
					if(inputRowAllCellText.length() != 0){
						inputRowAllCellText += emptyCellTextTemplate;
						allRowText.append(inputRowTextPrefix + inputRowAllCellText + inputRowTextPostfix);
						inputRowAllCellText = "";
					}
					
					String rowText = textareaRowTextTemplate;
					rowText = rowText.replace("<%dispUnitTitle%>", dispUnit.getLabel());
					rowText = rowText.replace("<%dispUnitFieldName%>", dispUnit.getName());
					allRowText.append(rowText);
				}
				else{
					//input
					String cellText = inputCellTextTemplate;
					cellText = cellText.replace("<%dispUnitTitle%>", dispUnit.getLabel());
					cellText = cellText.replace("<%dispUnitFieldName%>", dispUnit.getName());
					cellText = cellText.replace("<%dispUnitType%>", field.getValueType() == ValueType.Boolean ? "checkbox" : "input");
					if(inputRowAllCellText.length() == 0){					
						inputRowAllCellText = cellText;
					}
					else{				
						inputRowAllCellText += cellText;
						allRowText.append(inputRowTextPrefix + inputRowAllCellText + inputRowTextPostfix);
						inputRowAllCellText = "";
					}
				}
			}
		}		
		if(inputRowAllCellText.length() != 0){
			inputRowAllCellText += emptyCellTextTemplate;
			allRowText.append(inputRowAllCellText); 
		}		
		pageText = pageText.substring(0, inputRowBeginIndex) + allRowText.toString() + pageText.substring(textareaRowEndIndex + endTextareaRow.length());

		//处理子表录入控件
		HashMap<String, SheetPart> allParts = sheet.getSheetParts();
		String allChildPartsText = "";
		String beginChildParts = "<%begin-childParts%>";
		String endChildParts = "<%end-childParts%>";
		int childPartsBeginIndex = pageText.indexOf(beginChildParts);
		int childPartsEndIndex = pageText.indexOf(endChildParts);

		if(allParts.size() != 0){
			String childPartsTextTemplate = pageText.substring(childPartsBeginIndex + beginChildParts.length(), childPartsEndIndex);
			StringBuilder partsText = new StringBuilder();
			
			String beginChildPart = "<%begin-childPart%>";
			String endChildPart = "<%end-childPart%>";
			int childPartBeginIndex = childPartsTextTemplate.indexOf(beginChildPart);
			int childPartEndIndex = childPartsTextTemplate.indexOf(endChildPart);
			String childPartTextTemplate = childPartsTextTemplate.substring(childPartBeginIndex + beginChildPart.length(), childPartEndIndex);
			
			String childPartsTextPrefix = childPartsTextTemplate.substring(0, childPartBeginIndex);
			String childPartsTextPostfix = childPartsTextTemplate.substring(childPartEndIndex + endChildPart.length());
			for(String key : allParts.keySet()){
				SheetPart part = allParts.get(key);
				if(part != mainPart){
					String partText = childPartTextTemplate;
					partText = partText.replace("<%partTitle%>", part.getTitle());
					partText = partText.replace("<%partName%>", part.getName());
					partsText.append(partText + "\r\n");
				}
			}
			allChildPartsText = childPartsTextPrefix + partsText.toString() + childPartsTextPostfix;
		} 
		pageText = pageText.substring(0, childPartsBeginIndex) + allChildPartsText + pageText.substring(childPartsEndIndex + endChildParts.length()); 
		
		//处理模型		
		StringBuilder dataModelText = new StringBuilder();
		String beginDataModel = "<%begin-dataModel%>";
		String endDataModel = "<%end-dataModel%>";
		int dataModelBeginIndex = pageText.indexOf(beginDataModel);
		int dataModelEndIndex = pageText.indexOf(endDataModel);
		String dataModelTextTemplate = pageText.substring(dataModelBeginIndex + beginDataModel.length(), dataModelEndIndex);
		
		StringBuilder viewModelText = new StringBuilder();
		String beginViewModel = "<%begin-viewModel%>";
		String endViewModel = "<%end-viewModel%>";
		int viewModelBeginIndex = pageText.indexOf(beginViewModel);
		int viewModelEndIndex = pageText.indexOf(endViewModel);
		String viewModelTextTemplate = pageText.substring(viewModelBeginIndex + beginViewModel.length(), viewModelEndIndex);
				
		for(String key : allParts.keySet()){
			SheetPart part = allParts.get(key);
			View partView = ViewCollection.getView(part.getViewName());
			String viewName = partView.getName();
			String dataName = partView.getDataName();
			dataModelText.append(dataModelTextTemplate.replace("<%dataName%>", dataName) + "\r\n");
			viewModelText.append(viewModelTextTemplate.replace("<%viewName%>", viewName) + "\r\n");
		}
		pageText = pageText.substring(0, dataModelBeginIndex) + dataModelText.toString() + viewModelText.toString() + pageText.substring(viewModelEndIndex + endViewModel.length());
		
		fo.createFile(pageFilePath, pageText, "utf-8");	 
	}
	
	private void generateSheetGridPageBySheet(Sheet sheet, String templateSheetGridFilePath, String pageFilePath) throws Exception{
		FileOperate fo = new FileOperate();
		String pageText = fo.readTxt(templateSheetGridFilePath, "utf-8");
		pageText = pageText.replace("<%sheetTitle%>", sheet.getTitle());
		pageText = pageText.replace("<%sheetName%>", sheet.getName());
	    SheetPart mainPart = sheet.getMainPart();
	    pageText = pageText.replace("<%parentViewModel%>", mainPart.getViewName()); 
		View mainPartView = ViewCollection.getView(mainPart.getViewName());
		pageText = pageText.replace("<%parentDataModel%>", mainPartView.getDataName());
		HashMap<String, SheetPart> allParts = sheet.getSheetParts();
		
		StringBuilder dataModelText = new StringBuilder();
		String beginDataModel = "<%begin-dataModel%>";
		String endDataModel = "<%end-dataModel%>";
		int dataModelBeginIndex = pageText.indexOf(beginDataModel);
		int dataModelEndIndex = pageText.indexOf(endDataModel);
		String dataModelTextTemplate = pageText.substring(dataModelBeginIndex + beginDataModel.length(), dataModelEndIndex);
		
		StringBuilder viewModelText = new StringBuilder();
		String beginViewModel = "<%begin-viewModel%>";
		String endViewModel = "<%end-viewModel%>";
		int viewModelBeginIndex = pageText.indexOf(beginViewModel);
		int viewModelEndIndex = pageText.indexOf(endViewModel);
		String viewModelTextTemplate = pageText.substring(viewModelBeginIndex + beginViewModel.length(), viewModelEndIndex);
		
		for(String key : allParts.keySet()){
			SheetPart part = allParts.get(key);
			View partView = ViewCollection.getView(part.getViewName());
			String viewName = partView.getName();
			String dataName = partView.getDataName();
			dataModelText.append(dataModelTextTemplate.replace("<%dataName%>", dataName) + "\r\n");
			viewModelText.append(viewModelTextTemplate.replace("<%viewName%>", viewName) + "\r\n");
			
		}

		pageText = pageText.substring(0, dataModelBeginIndex) + dataModelText.toString() + viewModelText.toString() + pageText.substring(viewModelEndIndex + endViewModel.length());
		fo.createFile(pageFilePath, pageText, "utf-8");	 
	} 

	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String sheetId = customParam.getString("sheetId");	 
		Sheet sheet = sheetCollection.reloadSheetFromDB(sheetId);		
		String jsFilePath = ContextUtil.getAbsolutePath() +  ZlpState.DATA_MODEL_PATH_OF_SHEET  + sheet.getName() + ".js";
		List<String> errors = this.validateSheetModel(sheet);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsBySheet(sheet, jsFilePath);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	private List<String> validateSheetModel(Sheet sheet){
		List<String> errors = new ArrayList<String>(); 
		 		 
		HashMap<String, SheetPart> sheetParts = sheet.getSheetParts();
		SheetPart mainSheetPart = null;
		for(String name : sheetParts.keySet()){
			SheetPart sheetPart = sheetParts.get(name);
			if(sheetPart.getParentPartName() != null && !sheetPart.getParentPartName().isEmpty()){
				if(mainSheetPart != null){
					errors.add("只准指定一个顶级组成部分, 即只准有一个组成部分的上级组成部分为空.");
				}
			}
			List<String> parentNames = new ArrayList<String>();
			SheetPart tempPart = sheetPart;
			String parentName = sheetPart.getParentPartName();
			while(parentName!= null && !parentName.isEmpty()){
				if(parentNames.contains(parentName)){
					errors.add("查找上级组成部分时，发现构成了环形.");
					break;
				}
				else{
					parentNames.add(parentName);
					tempPart = sheet.getSheetPart(parentName);
					if(tempPart == null){
						errors.add("不存在名为 " + parentName + " 的组成部分, 不能指定 " + parentName + " 为上级组成部分.");
						break;
					}
					else{
						parentName = tempPart.getParentPartName();
					}
				}
			}
		}
		for(String name : sheetParts.keySet()){
			SheetPart sheetPart = sheetParts.get(name);
			View view = ViewCollection.getView(sheetPart.getViewName());
			if(view == null){
				errors.add("视图模型 " + sheetPart.getViewName() + " 不存在.");
			}
			else{
				Data data = DataCollection.getData(view.getDataName());
				if(data == null){
					errors.add("视图模型 " + sheetPart.getViewName() + " 对应的数据模型 " + view.getDataName() + " 不存在.");
				}
				else{
					DataField labelField = data.getDataField(sheetPart.getLabelField());
					if(labelField == null){
						errors.add("数据模型 " + data.getName() + " 中不存在名为 " + sheetPart.getLabelField() + " 的字段");
					}
					if(sheetPart.getParentPartName() != null && !sheetPart.getParentPartName().isEmpty()){ 
						DataField parentPointerField = data.getDataField(sheetPart.getParentPointerField());
						if(parentPointerField == null){
							errors.add("数据模型 " + data.getName() + " 中不存在名为 " + sheetPart.getParentPointerField() + " 的字段");
						} 
						else{
							if(parentPointerField.getValueType() !=ValueType.String){
								errors.add("上级外键字段 " + parentPointerField.getName() + " 必须为字符串类型.");
							}
						}
					}
				}
			}
		}
		return errors;
	}	
	
	private void generateJsBySheet(Sheet sheet, String jsFilePath) throws Exception{  
		StringBuilder jsStr = new StringBuilder("sheetModels." + sheet.getName() + " = {\r\n");
		jsStr.append("  id:\"" + sheet.getId() + "\",\r\n");
		jsStr.append("  name:\"" + sheet.getName() + "\",\r\n"); 
		jsStr.append("  parts:{\r\n");
		int partCount = sheet.getSheetParts().size();
		int i = 0;
		for(String partName : sheet.getSheetParts().keySet()){
			i++;
			SheetPart part = sheet.getSheetPart(partName);  
			jsStr.append("    " + partName + ":{"); 
			jsStr.append("name:\"" + part.getName() + "\",");
			jsStr.append("view:\"" + part.getViewName() + "\",");
			jsStr.append("labelField:\"" + part.getLabelField() + "\","); 
			jsStr.append("parentPartName:\"" + (part.getParentPartName()==null|| part.getParentPartName().isEmpty() ? "" : part.getParentPartName()) + "\","); 
			jsStr.append("parentPointerField:\"" + part.getParentPointerField() + "\""); 
			jsStr.append("}" + (i == partCount ? "\r\n" : ",\r\n"));	 
		} 
		
		jsStr.append("  }\r\n");  
		jsStr.append("}\r\n");

		/*
		for(String partName : sheet.getSheetParts().keySet()){ 
			SheetPart part = sheet.getSheetPart(partName);  
			View view = viewCollection.getView(part.getView());
			jsStr.append("document.writeln(\"<script type=\\\"text/javascript\\\" src=\\\"../../js/data/" + view.getDataName() + ".js\\\" />\");\r\n");
			jsStr.append("document.writeln(\"<script type=\\\"text/javascript\\\" src=\\\"../../js/view/" + view.getName() + ".js\\\" />\");\r\n");
		}
		*/
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	} 
}
