package com.zlp.platform.model.sysmodel; 
 
public class Report {
	private String id;
	private String name;  
	private String paramWinName;
	private String reportName;
	private Boolean isAuto;
	
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
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public Boolean getIsAuto() {
		return isAuto;
	}
	public void setIsAuto(Boolean isAuto) {
		this.isAuto = isAuto;
	}
	public String getParamWinName() {
		return paramWinName;
	}
	public void setParamWinName(String paramWinName) {
		this.paramWinName = paramWinName;
	}  
}
