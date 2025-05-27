package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_ParamWin; 
import com.zlp.platform.model.sysmodel.DownList;
import com.zlp.platform.model.sysmodel.DownListCollection;
import com.zlp.platform.model.sysmodel.DownListField;
import com.zlp.platform.model.sysmodel.ParamWin;
import com.zlp.platform.model.sysmodel.ParamWinCollection;
import com.zlp.platform.model.sysmodel.ParamWinUnit;
import com.zlp.platform.model.sysmodel.ParamWinUnitMap; 

public class Sys_ParamWinImpl extends DataBaseDao implements Sys_ParamWin{

	private ParamWinCollection paramWinCollection = null;
	public void setParamWinCollection(ParamWinCollection paramWinCollection){
		this.paramWinCollection = paramWinCollection;
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
	
	//根据view的定义，生成前端需要的js
	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String paramWinId = customParam.getString("paramWinId");	 
		ParamWin paramWin = paramWinCollection.reloadParamWinFromDB(paramWinId);		
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.DATA_MODEL_PATH_OF_PARAMWIN + paramWin.getName() + ".js";
		List<String> errors = this.validateParamWinModel(paramWin);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {
			this.generateJsByParamWin(paramWin, jsFilePath);		 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap;
	}
	
	private List<String> validateParamWinModel(ParamWin paramWin){

		List<String> errors = new ArrayList<String>();  
		 
		HashMap<String, ParamWinUnit> units = paramWin.getUnits();
		for(String unitName : units.keySet()){ 
			ParamWinUnit u = units.get(unitName); 
			if(u.getValueLength()<=0){
				errors.add(unitName + ": " + "长度必须为正整数.");				
			}
			
			if(u.getValueType() == ValueType.Decimal){
				if(u.getDecimalNum()<0){
					errors.add(unitName + ": " + "小数位数不能为负数.");					
				}
			}  
			
			if("list".equals(u.getInputHelpType())){
				List<ParamWinUnitMap> uMaps = u.getParamWinUnitMap();
				if(uMaps.size() == 0){
					errors.add(unitName + ": " + "已定义为下拉类型, 请定义该它的输入帮助映射字段.");
				}
				
				DownList list =  DownListCollection.getDownList(u.getInputHelpName()); 
				if(list == null){
					errors.add(unitName + ": " + "不存在名为 " + u.getInputHelpName() + " 的下拉帮助定义.");
				}
				else{
					for(ParamWinUnitMap uMap : uMaps){
						if(uMap.getDestField() == null){
							errors.add(unitName + ": " + "输入帮助映射目标参数名不能为空.");
						}
						if(uMap.getSourceField() == null){
							errors.add(unitName + ": " + "输入帮助映射源字段不能为空.");
						}
						if(uMap.getDestField() != null && uMap.getSourceField() != null){
							ParamWinUnit destUnit = units.get(uMap.getDestField());
							if(destUnit == null){
								errors.add(unitName + ": " + "输入帮助映射目标参数 " + uMap.getDestField() + " 不存在于此参数录入窗口模型中.");
							}  
							else{
								DownListField sourceField = list.getDownListField(uMap.getSourceField());
								if(sourceField == null){
									errors.add(unitName + ": " + "输入帮助映射源字段 " + uMap.getSourceField() + " 不存在于此下拉模型 " + list.getName() + " 中.");
								}
								else{
									if(destUnit.getValueType() != sourceField.getValueType()){
										errors.add(unitName + ": " + "输入帮助映射目标参数 " + uMap.getDestField() + "(" + destUnit.getValueType().toString().toLowerCase() 
												+ ") 和源字段 " + uMap.getSourceField() + "(" + sourceField.getValueType().toString().toLowerCase() 
												+ ") 的类型不匹配.");
									}
								}
							}
						}
					}	
				}
			}
			
			if("pop".equals(u.getInputHelpType())){
				List<ParamWinUnitMap> uMaps = u.getParamWinUnitMap();
				if(uMaps.size() == 0){
					errors.add(unitName + ": " + "已定义为弹出类型, 请定义该字段的输入帮助映射字段.");
				}

				/*不验证弹出是否存在
				String popNameStr = u.getInputHelpName();
				String[] popNames = popNameStr.split("\\.");
				if(popNames.length != 2){
					errors.add(unitName + ": " + "不存在名为 " + u.getInputHelpName() + " 的弹出帮助定义.");
				}
				else{	
					String viewName = "";
					if("tree".equals(popNames[0].toLowerCase())){
						String treeName = popNames[1];
						Tree tree =  TreeCollection.getTree(treeName);
						if(tree == null){
							errors.add(unitName + ": " + "不存在名为 " + u.getInputHelpName() + " 的树形弹出帮助定义.");
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
							errors.add(unitName + ": " + "不存在名为 " + viewName + " 的弹出帮助定义.");
						}
						else{
							Data viewData = DataCollection.getData(view.getDataName());
							if(viewData == null){
								errors.add(unitName + ": " + "视图 " + view.getName() + " 对应的数据模型 " + view.getDataName() + " 不存在.");
							}
							else{
								for(ParamWinUnitMap uMap : uMaps){
									if(uMap.getDestField() == null){
										errors.add(unitName + ": " + "输入帮助映射目标参数不能为空.");
									}
									if(uMap.getSourceField() == null){
										errors.add(unitName + ": " + "输入帮助映射源字段不能为空.");
									}
									if(uMap.getDestField() != null && uMap.getSourceField() != null){
										ParamWinUnit destUnit = units.get(uMap.getDestField());
										if(destUnit == null){
											errors.add(unitName + ": " + "输入帮助映射目标参数 " + uMap.getDestField() + " 不存在于此参数录入窗口模型中.");
										}  
										else{
											ViewDispunit dispunit = view.getViewDispunit(uMap.getSourceField());
											if(dispunit == null){
												errors.add(unitName + ": " + "输入帮助映射源字段 " + uMap.getSourceField() + " 不存在于此弹出模型 " + view.getName() + " 中.");
											}
											else
											{
												DataField dispunitField = viewData.getDataField(dispunit.getName());
												if(dispunitField == null){
													errors.add(unitName + ": " + "输入帮助映射源字段 " + uMap.getSourceField() + " 不存在于对应数据模型里.");
												}
												else {
													if(destUnit.getValueType() != dispunitField.getValueType()){
														errors.add(unitName + ": " + "输入帮助映射目标参数 " + uMap.getDestField() + "(" + destUnit.getValueType().toString().toLowerCase() 
																+ ") 和源字段 " + uMap.getSourceField() + "(" + dispunitField.getValueType().toString().toLowerCase() 
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
				*/
			}
		} 
		return errors;
	}	
	
	private void generateJsByParamWin(ParamWin paramWin, String jsFilePath) throws Exception{   
 
		StringBuilder jsStr = new StringBuilder("paramWinModels." + paramWin.getName() + " = {\r\n");
		jsStr.append("  id:\"" + paramWin.getId() + "\",\r\n");
		jsStr.append("  name:\"" + paramWin.getName() + "\",\r\n");
 		jsStr.append("  units:{\r\n");
		HashMap<String, ParamWinUnit> units = paramWin.getUnits();
		int i = 0;
		int unitCount = units.size();
		for(String unitName : units.keySet()){
			i++;
			ParamWinUnit unit = units.get(unitName);
			jsStr.append("    " + unit.getName() + ":{\r\n");
			jsStr.append("      id:\"" + unit.getId() + "\",\r\n");
			jsStr.append("      name:\"" + unit.getName() + "\",\r\n");
			jsStr.append("      label:\"" + unit.getLabel() + "\",\r\n");
			jsStr.append("      valueType:valueType." + unit.getValueType().toString().toLowerCase() + ",\r\n"); 
			jsStr.append("      inputHelpType:\"" + (unit.getInputHelpType() == null ? "" : unit.getInputHelpType()) + "\",\r\n");
			jsStr.append("      inputHelpName:\"" + (unit.getInputHelpName() == null ? "" : unit.getInputHelpName()) + "\",\r\n");
			jsStr.append("      decimalNum:\"" + unit.getDecimalNum() + "\",\r\n");
			jsStr.append("      valueLength:" + unit.getValueLength() + ",\r\n"); 
			jsStr.append("      isMultiValue:" + unit.getIsMultiValue() + ",\r\n");
			jsStr.append("      isNullable:" + unit.getIsNullable() + ",\r\n");
			jsStr.append("      isEditable:" + unit.getIsEditable() + ",\r\n");
 
			String unitType = "";
			switch(unit.getValueType()){
				case String: 
					unitType = "text";
					break;
				case Decimal: 
					unitType = "decimal";
					break;
				case Date: 
					unitType = "date";
					break;
				case Time: 
					unitType = "time";
					break;
				case Boolean: 
					unitType = "checkbox";
					break;
				default:
					break;
			}
			if("list".equals(unit.getInputHelpType())){
				unitType = "list";
			}
			if("pop".equals(unit.getInputHelpType())){
				if(unit.getIsMultiValue()){
					unitType = "popMulti";
				}
				else{
					unitType = "pop";
				}
			}
			jsStr.append("      unitType:\"" + unitType + "\",\r\n");

			String inputHelpType = unit.getInputHelpType();
			if(!"none".equals(inputHelpType)){
				List<ParamWinUnitMap> maps = unit.getParamWinUnitMap();
				int mapCount = maps.size();
				if(mapCount > 0){
					jsStr.append("      maps:{");	
					for(int j=0; j<mapCount;j++){
						ParamWinUnitMap map = maps.get(j);
						jsStr.append("\"" + map.getDestField() + "\":\"" + map.getSourceField() + "\"" + (j +1 == mapCount ? "" : ","));
					}
					jsStr.append("},\r\n");	
				}
				if("list".equals(inputHelpType)){
					String listName = unit.getInputHelpName();
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
			jsStr.append("      defaultValue:\"" + (unit.getDefaultValue() == null ? "" : unit.getDefaultValue()) + "\",\r\n");
			jsStr.append("    }" + (i == unitCount ? "\r\n" : ",\r\n"));			
		}
		jsStr.append("  }\r\n"); 
		jsStr.append("}\r\n");
		
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsStr.toString());
	} 
}
