package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_Data;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.model.sysmodel.DataFieldMap;
import com.zlp.platform.model.sysmodel.DownList;
import com.zlp.platform.model.sysmodel.DownListCollection;
import com.zlp.platform.model.sysmodel.DownListField;
import com.zlp.platform.model.sysmodel.Tree;
import com.zlp.platform.model.sysmodel.TreeCollection;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.model.sysmodel.ViewDispunit;
import com.zlp.platform.model.sysmodel.dbStructure.DataModelToDBTableCompare;

public class Sys_DataImpl extends DataBaseDao  implements Sys_Data {
	
	private DataCollection dataCollection = null;
	public void setDataCollection(DataCollection dataCollection){
		this.dataCollection = dataCollection;
	} 
	
	//增加自动生成表结构的功能 modify by ls 20190618
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException {
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("generateJs".equals(actionName)){
				return generateJs(session, customParam);
			}
			else if("updateDBStrucure".equals(actionName)){
				return updateDBStrucure(session, customParam);
			}
			return null;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	//更新表结构
	private HashMap<String, Object> updateDBStrucure(INcpSession session, JSONObject customParam) throws Exception{
		List<String> errors = new ArrayList<>();
		String dataId = customParam.getString("dataId");	  
		Data data = dataCollection.reloadDataFromDB(dataId);	
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess"); 

		//更改对应的数据库表结构
		DataModelToDBTableCompare model2TableCompare = new DataModelToDBTableCompare();
		model2TableCompare.setDBSession(this.getDBSession());
		model2TableCompare.setDBParserAccess(dbParserAccess);
		model2TableCompare.init(data.getName()); 
		try{
			if(!model2TableCompare.updateDbTableStructure()){
				errors.add("无需更新表结构");
			}
		}
		catch(Exception ex){
			errors.add(ex.getMessage());
		}
			
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) { 	 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	//根据data的定义，生成前端需要的js
	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String dataId = customParam.getString("dataId");	  
		Data data = dataCollection.reloadDataFromDB(dataId);		
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.DATA_MODEL_PATH_OF_DATA + data.getName() + ".js";
		List<String> errors = this.validateDataModel(data);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsByData(data, jsFilePath);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	private List<String> validateDataModel(Data data){
		List<String> errors = new ArrayList<String>(); 
		
		boolean hasIdField = false;
		HashMap<String, DataField> fields = data.getDataFields();
		for(String fieldName : fields.keySet()){
			if(fieldName.equals(data.getIdFieldName())){
				hasIdField = true;
			}
			DataField field = fields.get(fieldName); 
			if(field.getValueLength()<=0){
				errors.add(fieldName + ": " + "字段长度必须为正整数.");				
			}
			
			if(field.getValueType() == ValueType.Decimal){
				if(field.getDecimalNum()<0){
					errors.add(fieldName + ": " + "小数位数不能为负数.");					
				}
			} 
			if(field.getForeignKeyName() != null){
				DataField foreignKeyField = fields.get(field.getForeignKeyName());
				if(foreignKeyField == null){
					errors.add(fieldName + ": " + "外键字段 " + field.getForeignKeyName() + "不存在于此数据模型中.");
				}
				else{
					if("decimal".equals(foreignKeyField.getValueType())) {
						errors.add(fieldName + ": " + "外键字段 " + field.getForeignKeyName() + "的字段必须为decimal类型.");
					}
				}				
			}
			
			if(field.getIsSum()){
				if(field.getValueType() != ValueType.Decimal){
					errors.add(fieldName + ": " + "合计字段必须为decimal类型.");
				}
			} 
			
			if("list".equals(field.getInputHelpType())){
				List<DataFieldMap> fieldMaps = field.getDataFieldMap();
				if(fieldMaps.size() == 0){
					errors.add(fieldName + ": " + "已定义为下拉类型, 请定义该字段的输入帮助映射字段.");
				}
				
				DownList list =  DownListCollection.getDownList(field.getInputHelpName()); 
				if(list == null){
					errors.add(fieldName + ": " + "不存在名为 " + field.getInputHelpName() + " 的下拉帮助定义.");
				}
				else{
					for(DataFieldMap fieldMap : fieldMaps){
						if(fieldMap.getDestField() == null){
							errors.add(fieldName + ": " + "输入帮助映射目标字段不能为空.");
						}
						if(fieldMap.getSourceField() == null){
							errors.add(fieldName + ": " + "输入帮助映射源字段不能为空.");
						}
						if(fieldMap.getDestField() != null && fieldMap.getSourceField() != null){
							DataField destField = fields.get(fieldMap.getDestField());
							if(destField == null){
								errors.add(fieldName + ": " + "输入帮助映射目标字段 " + fieldMap.getDestField() + " 不存在于此数据模型中.");
							}  
							else{
								DownListField sourceField = list.getDownListField(fieldMap.getSourceField());
								if(sourceField == null){
									errors.add(fieldName + ": " + "输入帮助映射源字段 " + fieldMap.getSourceField() + " 不存在于此下拉模型 " + list.getName() + " 中.");
								}
								else{
									if(destField.getValueType() != sourceField.getValueType()){
										errors.add(fieldName + ": " + "输入帮助映射目标字段 " + fieldMap.getDestField() + "(" + destField.getValueType().toString().toLowerCase() 
												+ ") 和源字段 " + fieldMap.getSourceField() + "(" + sourceField.getValueType().toString().toLowerCase() 
												+ ") 的类型不匹配.");
									}
								}
							}
						}
					}	
				}
			}
			
			if("pop".equals(field.getInputHelpType())){
				List<DataFieldMap> fieldMaps = field.getDataFieldMap();
				if(fieldMaps.size() == 0){
					errors.add(fieldName + ": " + "已定义为弹出类型, 请定义该字段的输入帮助映射字段.");
				}
				
				String popNameStr = field.getInputHelpName();
				String[] popNames = popNameStr.split("\\.");
				if(popNames.length != 2){
					errors.add(fieldName + ": " + "不存在名为 " + field.getInputHelpName() + " 的弹出帮助定义.");
				}
				else{	
					String viewName = "";
					if("tree".equals(popNames[0].toLowerCase())){
						String treeName = popNames[1];
						Tree tree =  TreeCollection.getTree(treeName);
						if(tree == null){
							errors.add(fieldName + ": " + "不存在名为 " + field.getInputHelpName() + " 的树形弹出帮助定义.");
						}
						else{
						   viewName = tree.getViewName();
						}
					}
					else if("view".equals(popNames[0].toLowerCase())){
						viewName = popNames[1];
					}
					if(!viewName.isEmpty()){
						View view =  ViewCollection.getView(viewName);
						if(view == null){
							errors.add(fieldName + ": " + "不存在名为 " + viewName + " 的弹出帮助定义.");
						}
						else{
							Data viewData = DataCollection.getData(view.getDataName());
							if(viewData == null){
								errors.add(fieldName + ": " + "视图 " + view.getName() + " 对应的数据模型 " + view.getDataName() + " 不存在.");
							}
							else{
								for(DataFieldMap fieldMap : fieldMaps){
									if(fieldMap.getDestField() == null){
										errors.add(fieldName + ": " + "输入帮助映射目标字段不能为空.");
									}
									if(fieldMap.getSourceField() == null){
										errors.add(fieldName + ": " + "输入帮助映射源字段不能为空.");
									}
									if(fieldMap.getDestField() != null && fieldMap.getSourceField() != null){
										DataField destField = fields.get(fieldMap.getDestField());
										if(destField == null){
											errors.add(fieldName + ": " + "输入帮助映射目标字段 " + fieldMap.getDestField() + " 不存在于此数据模型中.");
										}  
										else{
											ViewDispunit dispunit = view.getViewDispunit(fieldMap.getSourceField());
											if(dispunit == null){
												errors.add(fieldName + ": " + "输入帮助映射源字段 " + fieldMap.getSourceField() + " 不存在于此弹出模型 " + view.getName() + " 中.");
											}
											else
											{
												DataField dispunitField = viewData.getDataField(dispunit.getName());
												if(dispunitField == null){
													errors.add(fieldName + ": " + "输入帮助映射源字段 " + fieldMap.getSourceField() + " 不存在于对应数据模型里.");
												}
												else {
													if(destField.getValueType() != dispunitField.getValueType()){
														errors.add(fieldName + ": " + "输入帮助映射目标字段 " + fieldMap.getDestField() + "(" + destField.getValueType().toString().toLowerCase() 
																+ ") 和源字段 " + fieldMap.getSourceField() + "(" + dispunitField.getValueType().toString().toLowerCase() 
																+ ") 的类型不匹配.");
													}
												}
											}
										}
									}
								}	
							}
						}			 
					}
				}
			}
		}
		if(!hasIdField){
			//不包含id字段
			errors.add("字段定义中不包含ID字段的定义, idFieldName = " + data.getIdFieldName() + ".");
		} 
		return errors;
	}	
	
	public void generateJsByData(Data data, String jsFilePath) throws Exception{ 
		//ViewCollection viewCollection = (DownListCollection)ContextUtil.getBean("viewCollection");
		StringBuilder jsStr = new StringBuilder("dataModels." + data.getName() + " = {\r\n");
		jsStr.append("  id:\"" + data.getId() + "\",\r\n");
		jsStr.append("  name:\"" + data.getName() + "\",\r\n");
		jsStr.append("  idFieldName:\"" + data.getIdFieldName() + "\",\r\n");
		jsStr.append("  fields:{\r\n");
		HashMap<String, DataField> fields = data.getDataFields();
		int i = 0;
		int fieldCount = fields.size();
		for(String fieldName : fields.keySet()){
			i++;
			DataField field = fields.get(fieldName);
			jsStr.append("    " + field.getName() + ":{\r\n");
			jsStr.append("      id:\"" + field.getId() + "\",\r\n");
			jsStr.append("      name:\"" + field.getName() + "\",\r\n");
			jsStr.append("      displayName:\"" + field.getDisplayName() + "\",\r\n");
			jsStr.append("      valueType:valueType." + field.getValueType().toString().toLowerCase() + ",\r\n");
			jsStr.append("      isSave:" + (field.getIsSave() ? "true" : "false") + ",\r\n");
			jsStr.append("      inputHelpType:\"" + (field.getInputHelpType() == null ? "" : field.getInputHelpType()) + "\",\r\n");
			jsStr.append("      inputHelpName:\"" + (field.getInputHelpName() == null ? "" : field.getInputHelpName())+ "\",\r\n");
			jsStr.append("      foreignKeyName:\"" +(field.getForeignKeyName() == null ? "" : field.getForeignKeyName()) + "\",\r\n");
			jsStr.append("      valueLength:" + field.getValueLength() + ",\r\n");
			jsStr.append("      decimalNum:" + field.getDecimalNum() + ",\r\n");
			
			//增加是否合计 added by ls 20190723
			jsStr.append("      isSum:" + (field.getIsSum() ? "true" : "false") + ",\r\n");

			String inputHelpType = field.getInputHelpType();
			if(!"none".equals(inputHelpType)){
				List<DataFieldMap> maps = field.getDataFieldMap();
				int mapCount = maps.size();
				if(mapCount > 0){
					jsStr.append("      maps:{");	
					for(int j=0; j<mapCount;j++){
						DataFieldMap map = maps.get(j);
						jsStr.append("\"" + map.getDestField() + "\":\"" + map.getSourceField() + "\"" + (j +1 == mapCount ? "" : ","));
					}
					jsStr.append("},\r\n");	
				}
				if("list".equals(inputHelpType)){
					String listName = field.getInputHelpName();
				    DownList list = DownListCollection.getDownList(listName);
				    if(list == null){
				    	throw new Exception("none list named " + listName);
				    }
				    else{
					    jsStr.append("      list:{\r\n");
					    jsStr.append("        name:\"" + list.getName() + "\",\r\n");
					    jsStr.append("        columns:[");
					    List<DownListField> listFields = list.getDownListFields();
					    List<DownListField> sortedListFields = new ArrayList<DownListField>();
					    int columnCount = listFields.size();
					    for(DownListField listField : listFields){
					    	for(int m=0;m<=sortedListFields.size();m++){
					    		if(m == sortedListFields.size()){
					    			sortedListFields.add(listField);
					    			break;
					    		}
					    		else{
						    		if(sortedListFields.get(m).getShowIndex() > listField.getShowIndex()){
						    			sortedListFields.add(m, listField);
						    			break;
						    		}
					    		}
					    	}
					    } 
					    for(int k=0; k<columnCount; k++){
					    	DownListField listField = sortedListFields.get(k);
					    	jsStr.append("{");
					    	jsStr.append("field:\"" + listField.getName() + "\",");
					    	jsStr.append("valueType:valueType." + listField.getValueType().toString().toLowerCase() + ",");
					    	jsStr.append("title:\"" + listField.getDisplayName() + "\",");
					    	jsStr.append("width:" + (listField.getIsShow() ? listField.getShowWidth() : 0) + ",");
					    	jsStr.append("hidden:" + (listField.getIsShow() ? "false" : "true"));
					    	jsStr.append("}" + (k + 1 == columnCount ? "" : ",") + "\r\n");
					    	
					    }
					    jsStr.append("        ]\r\n");
					    jsStr.append("      },\r\n");
				    }
				}
				else if("pop".equals(inputHelpType)){
				    jsStr.append("      view:{\r\n");
				    //jsStr.append("        name:\"" + view.getName() + "\"\r\n");
				    jsStr.append("      },\r\n");
				}
			}
			jsStr.append("      isReadonly:" + field.getIsReadonly() + "\r\n");	
			jsStr.append("    }" + (i == fieldCount ? "\r\n" : ",\r\n"));			
		}
		jsStr.append("  }\r\n"); 
		jsStr.append("}\r\n");
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	}
 
}
