package com.zlp.platform.model.sysmodel;

//单据类型
public class DocType {
	private String id;
	private String name; 
	private String userIdFieldName;
	private String orgIdFieldName;
	private String isDeletedFieldName;
	private String sheetName;
	private String createPageUrl;
	private String queryPageUrl;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUserIdFieldName() {
		return userIdFieldName;
	}
	public void setUserIdFieldName(String userIdFieldName) {
		this.userIdFieldName = userIdFieldName;
	}
	public String getOrgIdFieldName() {
		return orgIdFieldName;
	}
	public void setOrgIdFieldName(String orgIdFieldName) {
		this.orgIdFieldName = orgIdFieldName;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public String getIsDeletedFieldName() {
		return isDeletedFieldName;
	}
	public void setIsDeletedFieldName(String isDeletedFieldName) {
		this.isDeletedFieldName = isDeletedFieldName;
	}
	
	public Data getMainData() throws Exception{
		Sheet sheet = SheetCollection.getSheet(sheetName);
		if(sheet == null){
			throw new Exception("None sheet named " + sheetName);
		}
		else{
			SheetPart sheetPart = sheet.getMainPart();
			String viewName = sheetPart.getViewName();
			View view = ViewCollection.getView(viewName);
			if(view == null){
				throw new Exception("None view named " + viewName);
			}
			else{
				String dataName = view.getDataName();
				Data data = DataCollection.getData(dataName);
				if(data == null){
					throw new Exception("None data named " + dataName);
				}
				else{
					return data;
				}
			}
		}
	}
	public String getCreatePageUrl() {
		return createPageUrl;
	}
	public void setCreatePageUrl(String createPageUrl) {
		this.createPageUrl = createPageUrl;
	}
	public String getQueryPageUrl() {
		return queryPageUrl;
	}
	public void setQueryPageUrl(String queryPageUrl) {
		this.queryPageUrl = queryPageUrl;
	}
}
