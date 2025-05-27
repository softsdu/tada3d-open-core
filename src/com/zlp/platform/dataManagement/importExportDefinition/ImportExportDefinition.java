package com.zlp.platform.dataManagement.importExportDefinition;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;

import com.zlp.platform.common.SysConfig;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;

//输入输出定义类
public class ImportExportDefinition {
	
	public static ImportExportDefinition initFromDB(Session dbSession, IDBParserAccess dbParserAccess, String code) throws Exception{

		//获取菌列表的的导入设置
	    String sql = "select def.id as id, def.code as code, def.definitionXml as xml from dm_importexportdefinition def where def.code = " + SysConfig.getParamPrefix() + "code";
    	HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
    	p2vs.put("code", code);
    	DataTable dt = dbParserAccess.getMultiLineValues(dbSession, sql, p2vs, new String[]{ "id", "code", "xml"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.String});
    	List<DataRow> rows = dt.getRows();
    	if(rows.size() == 0){
    		throw new Exception("未找到对应的表dm_ImportExportDefinition记录。 code = " + code);
    	} 
		DataRow row = rows.get(0); 
		String id = row.getStringValue("id"); 
		String xml = row.getStringValue("xml");
		ImportExportDefinition def = new ImportExportDefinition(id, code, xml);
		return def;
	}
	
	//构造函数
	public ImportExportDefinition(String id, String code, String xml) throws Exception{

		this.setId(id);
		this.setCode(code);
		
		Document document = null;
		try{
			SAXReader reader = new SAXReader(); 
			document = reader.read(new StringReader(xml));
			//根节点
			Element rootNode = document.getRootElement();
			
			//UpdateType
			String updateTypeStr = rootNode.attribute("UpdateType").getValue();
			UpdateType updateType = Enum.valueOf(UpdateType.class, updateTypeStr);
			this.setUpdateType(updateType);

			//FileType
			String fileTypeStr = rootNode.attribute("FileType").getValue();
			FileType fileType = Enum.valueOf(FileType.class, fileTypeStr);
			this.setFileType(fileType);
			
			//FileParser
			switch(fileType){
				case EXCEL:{
					ExcelParser ep = new ExcelParser();					
					this.setFileParser(ep);

					Element excelParserNode = rootNode.element("ExcelParser");
					String hasHeaderRowStr = excelParserNode.attribute("HasHeaderRow").getValue();
					boolean hasHeaderRow = Boolean.parseBoolean(hasHeaderRowStr);
					ep.setHasHeaderRow(hasHeaderRow);

					List<ExcelColumn> columns =new ArrayList<ExcelColumn>();
					ep.setColumns(columns);
					Element columnsNode = excelParserNode.element("Columns");
					List<Element> columnNodeList = columnsNode.elements("Column");
					for(int i=0;i<columnNodeList.size();i++){
						Element columnNode = columnNodeList.get(i);
						ExcelColumn column = new ExcelColumn();

					    Attribute excelColumnAttri = columnNode.attribute("ExcelColumnName");
					    String excelColumnName = excelColumnAttri.getValue();
					    column.setExcelColumnName(excelColumnName); 
					    
					    Attribute itemNameAttri = columnNode.attribute("ItemName");
					    String itemName = itemNameAttri.getValue();
					    column.setItemName(itemName);
					    
					    Attribute dataTypeAttri = columnNode.attribute("DataType");
					    String dataTypeStr = dataTypeAttri.getValue();
					    DataType dataType = Enum.valueOf(DataType.class, dataTypeStr);
					    column.setDataType(dataType);
					    
					    Attribute formatPatternAttri = columnNode.attribute("FormatPattern");
					    if(formatPatternAttri != null){
						    String formatPattern= formatPatternAttri.getValue(); 
						    column.setFormatPattern(formatPattern);
					    }
					    
					    columns.add(column);
					}
					break;
				}
				case CSV:{
					break;
				}
				case XML:{
					break;
				}
				default:{
					break;
				}
			}
			
			//FieldList
			Element fieldListNode = rootNode.element("FieldList");
			List<Field> fieldList = new ArrayList<Field>();					
			this.setFieldList(fieldList); 
			List<Element> fieldNodeList = fieldListNode.elements("Field");
			for(int i=0;i<fieldNodeList.size();i++){
				Element fieldNode = fieldNodeList.get(i);
				Field field = new Field();

			    Attribute itemNameAttri = fieldNode.attribute("ItemName");
			    String itemName = itemNameAttri.getValue();
			    field.setItemName(itemName);

			    Attribute dbFieldNameAttri = fieldNode.attribute("DBFieldName");
			    String dbFieldName = dbFieldNameAttri.getValue();
			    field.setDbFieldName(dbFieldName);

			    Attribute showNameAttri = fieldNode.attribute("ShowName");
			    String showName =showNameAttri == null ? itemNameAttri.getValue() : showNameAttri.getValue();
			    field.setShowName(showName);

			    Attribute isUniqueAttri = fieldNode.attribute("IsUnique");
			    String isUniqueStr = isUniqueAttri.getValue();
			    boolean isUnique = Boolean.parseBoolean(isUniqueStr);
			    field.setIsUnique(isUnique);

			    Attribute widthAttri = fieldNode.attribute("Width");
			    String widthStr = widthAttri.getValue();
			    int width = Integer.parseInt(widthStr);
			    field.setWidth(width);

			    Attribute displayWidthAttri = fieldNode.attribute("DisplayWidth");
			    if(displayWidthAttri != null){
				    String displayWidthStr =  displayWidthAttri.getValue();
				    int displayWidth = Integer.parseInt(displayWidthStr);
				    field.setDisplayWidth(displayWidth);
			    }

			    Attribute fieldTypeAttri = fieldNode.attribute("FieldType");
			    String fieldTypeStr = fieldTypeAttri.getValue();
			    DataType fieldType = Enum.valueOf(DataType.class, fieldTypeStr);
			    field.setFieldType(fieldType);

			    Attribute canQueryAttri = fieldNode.attribute("CanQuery");
			    if(canQueryAttri != null){
				    String canQueryStr = canQueryAttri.getValue();
				    boolean canQuery = Boolean.parseBoolean(canQueryStr);
				    field.setCanQuery(canQuery);
			    }

			    Attribute formatPatternAttri = fieldNode.attribute("FormatPattern");
			    if(formatPatternAttri != null){
				    String formatPattern = formatPatternAttri.getValue();
				    field.setFormatPattern(formatPattern);
			    }
			    
			    Element listOptionsNode = fieldNode.element("ListOptions");
			    if(listOptionsNode != null){
			    	ListOptions listOptions = new ListOptions(); 
			    	field.setListOptions(listOptions);

				    Attribute listTypeAttri = listOptionsNode.attribute("ListType");
				    String listTypeStr = listTypeAttri.getValue();
				    ListType listType = Enum.valueOf(ListType.class, listTypeStr);
				    listOptions.setListType(listType);

				    Attribute isMultiValueAttri = listOptionsNode.attribute("IsMultiValue");
				    if(isMultiValueAttri != null){
					    String isMultiValueStr = isMultiValueAttri.getValue();
					    boolean isMultiValue = Boolean.parseBoolean(isMultiValueStr);
					    listOptions.setIsMultiValue(isMultiValue);
				    }

				    Attribute listSplitterAttri = listOptionsNode.attribute("ListSplitter");
				    if(listSplitterAttri != null){
					    String listSplitter = listSplitterAttri.getValue(); 
					    listOptions.setListSplitter(listSplitter);
				    }
				    
				    switch(listType){
				    	case Static:{
				    		List<Element> optionNodeList = listOptionsNode.elements("Option");
				    		List<Option> options = new ArrayList<Option>();
				    		listOptions.setOptions(options);
				    		
				    		for(int j=0;j<optionNodeList.size();j++){
				    			Element optionNode = optionNodeList.get(j);
				    			Option option = new Option();

							    Attribute keyAttri = optionNode.attribute("Key");
							    String key = keyAttri.getValue();
							    option.setKey(key);

							    Attribute valueAttri = optionNode.attribute("Value");
							    String value = valueAttri.getValue();
							    option.setValue(value);		

							    options.add(option);
				    		}
				    		break;
				    	}
				    }
			    }
			    
			    
			    fieldList.add(field);
			}
			
			
			
		}
		catch(Exception ex){
			throw ex;
		}
	}
	
	//id
	private String id = null;
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	
	//编码，对应数据库表名称
	private String code = null;
	public String getCode(){
		return this.code;
	}
	public void setCode(String code){
		this.code = code;
	}
	
	//数据更新方式
	private UpdateType updateType = null;
	public UpdateType getUpdateType(){
		return this.updateType;
	}
	public void setUpdateType(UpdateType updateType){
		this.updateType = updateType;
	}
	
	//文件类型
	private FileType fileType = null;
	public FileType getFileType(){
		return this.fileType;
	}
	public void setFileType(FileType fileType){
		this.fileType = fileType;
	}
	
	//导入导出文件解析
	private FileParser fileParser = null;
	public FileParser getFileParser(){
		return this.fileParser;
	}
	public void setFileParser(FileParser fileParser){
		this.fileParser = fileParser;
	}	
	
	//字段
	private List<Field> fieldList = null;
	public List<Field> getFieldList(){
		return this.fieldList;
	}
	public void setFieldList(List<Field> fieldList){
		this.fieldList = fieldList;
	}	
	
	
}
