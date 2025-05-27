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
import com.zlp.platform.dao.system.Sys_Tree;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField; 
import com.zlp.platform.model.sysmodel.Tree;
import com.zlp.platform.model.sysmodel.TreeCollection;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.model.sysmodel.ViewDispunit;

public class Sys_TreeImpl extends DataBaseDao implements Sys_Tree {

	private TreeCollection treeCollection = null;
	public void setTreeCollection(TreeCollection treeCollection){
		this.treeCollection = treeCollection;
	} 
	
	//生成js和jsp modified by ls 20190618
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
		String treeId = customParam.getString("treeId");	 
		Tree tree = treeCollection.reloadTreeFromDB(treeId);		 
		List<String> errors = this.validateTreeModel(tree);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generatePageByTree(tree);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	
	private void generatePageByTree(Tree tree) throws Exception{
		String templateTreeGridFilePath = ConfigContext.getConfigMap().get(ZlpState.MODEL_PAGES_TEMPLATES_TREEGRID); 
		String templateTreeCardFilePath = ConfigContext.getConfigMap().get(ZlpState.MODEL_PAGES_TEMPLATES_TREECARD); 
		String gridPageFilePath = ContextUtil.getAbsolutePath() + ZlpState.GENERATED_MODEL_PAGES_DIR_OF_TREE + tree.getName() + ".jsp";
		String cardPageFilePath = ContextUtil.getAbsolutePath() + ZlpState.GENERATED_MODEL_PAGES_DIR_OF_TREE + tree.getName() + "_Card.jsp";
		generateTreeCardPageBySheet(tree, templateTreeCardFilePath, cardPageFilePath);
		generateTreeGridPageBySheet(tree, templateTreeGridFilePath, gridPageFilePath);
	}
	
	private void generateTreeCardPageBySheet(Tree tree, String templateTreeCardFilePath, String pageFilePath) throws Exception{
		FileOperate fo = new FileOperate();
		String pageText = fo.readTxt(templateTreeCardFilePath, "utf-8");
		pageText = pageText.replace("<%treeTitle%>", tree.getTitle());
		pageText = pageText.replace("<%treeModelName%>", tree.getName()); 
	    pageText = pageText.replace("<%viewModelName%>", tree.getViewName()); 
		View view = ViewCollection.getView(tree.getViewName());
		pageText = pageText.replace("<%dataModelName%>", view.getDataName()); 
		Data data = DataCollection.getData(view.getDataName());
				
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
		for(ViewDispunit dispunit : view.getDispunits().values()){ 
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
				DataField field = data.getDataField(dispUnit.getName());
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
 
		fo.createFile(pageFilePath, pageText, "utf-8");	 
	}

	private void generateTreeGridPageBySheet(Tree tree, String templateTreeGridFilePath, String pageFilePath) throws Exception{
		FileOperate fo = new FileOperate();
		String pageText = fo.readTxt(templateTreeGridFilePath, "utf-8");
		pageText = pageText.replace("<%treeTitle%>", tree.getTitle());
		pageText = pageText.replace("<%treeModelName%>", tree.getName()); 
	    pageText = pageText.replace("<%viewModelName%>", tree.getViewName()); 
		View view = ViewCollection.getView(tree.getViewName());
		pageText = pageText.replace("<%dataModelName%>", view.getDataName());
		fo.createFile(pageFilePath, pageText, "utf-8");	 
	} 
	
	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String treeId = customParam.getString("treeId");	 
		Tree tree = treeCollection.reloadTreeFromDB(treeId);		
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.DATA_MODEL_PATH_OF_TREE + tree.getName() + ".js";
		List<String> errors = this.validateTreeModel(tree);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsByTree(tree, jsFilePath);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	private List<String> validateTreeModel(Tree tree){
		List<String> errors = new ArrayList<String>(); 
		 		    
		View view = ViewCollection.getView(tree.getViewName());
		if(view == null){
			errors.add("视图模型 " + tree.getViewName() + " 不存在.");
		}
		else{
			Data data = DataCollection.getData(view.getDataName());
			if(data == null){
				errors.add("视图模型 " + tree.getViewName() + " 对应的数据模型 " + view.getDataName() + " 不存在.");
			}
			else{
				DataField labelField = data.getDataField(tree.getLabelField());
				if(labelField == null){
					errors.add("数据模型 " + data.getName() + " 中不存在名为 " + tree.getLabelField() + " 的字段");
				}
				DataField parentPointerField = data.getDataField(tree.getParentPointerField());
				if(parentPointerField == null){
					errors.add("数据模型 " + data.getName() + " 中不存在名为 " + tree.getParentPointerField() + " 的字段");
				} 
				else{
					if(parentPointerField.getValueType() !=ValueType.String){
						errors.add("上级外键字段 " + parentPointerField.getName() + " 必须为字符串类型.");
					}
				}
				DataField isLeafField = data.getDataField(tree.getIsLeafField());
				if(isLeafField == null){
					errors.add("数据模型 " + data.getName() + " 中不存在名为 " + tree.getIsLeafField() + " 的字段");
				} 
				else{
					if(isLeafField.getValueType() !=ValueType.Boolean){
						errors.add("叶节点标示字段 " + isLeafField.getName() + " 必须为布尔类型.");
					}
				}
				String sortFieldName = tree.getSortField();
				if(sortFieldName == null || sortFieldName.isEmpty()){
					DataField sortField = data.getDataField(tree.getSortField());
					if(sortField == null){
						errors.add("数据模型 " + data.getName() + " 中不存在名为 " + tree.getSortField() + " 的字段");
					}
				}
			}
		}
		return errors;
	}	
	
	private void generateJsByTree(Tree tree, String jsFilePath) throws Exception{  

		StringBuilder jsStr = new StringBuilder("treeModels." + tree.getName() + " = {\r\n");
		jsStr.append("  id:\"" + tree.getId() + "\",\r\n");
		jsStr.append("  name:\"" + tree.getName() + "\",\r\n"); 
		jsStr.append("  view:\"" + tree.getViewName() + "\",\r\n");  
		jsStr.append("  labelField:\"" + tree.getLabelField() + "\",\r\n");   
		jsStr.append("  parentPointerField:\"" + tree.getParentPointerField() + "\",\r\n");   
		jsStr.append("  isLeafField:\"" + tree.getIsLeafField() + "\",\r\n");   
		jsStr.append("  sortField:\"" + tree.getSortField() + "\"\r\n");   
		jsStr.append("}\r\n");
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	} 
}
