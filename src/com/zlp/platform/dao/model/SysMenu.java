package com.zlp.platform.dao.model;

public class SysMenu {
	
	private String code;
	
	private String name;
	
	private String parentid;
	
	private String description;
	
	private String isdefaultenable;
	
	private String ishidden;
	
	private String isleaf;
	
	private String icon;
	
	private String actionexp;
	
	private String pageurl;
	
	private String isreport;
	
	private String id;
	
	private String reportInstanceId;

	private String reportId;

	private String topmenudId;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsdefaultenable() {
		return isdefaultenable;
	}

	public void setIsdefaultenable(String isdefaultenable) {
		this.isdefaultenable = isdefaultenable;
	}

	public String getIshidden() {
		return ishidden;
	}

	public void setIshidden(String ishidden) {
		this.ishidden = ishidden;
	}

	public String getIsleaf() {
		return isleaf;
	}

	public void setIsleaf(String isleaf) {
		this.isleaf = isleaf;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getActionexp() {
		return actionexp;
	}

	public void setActionexp(String actionexp) {
		this.actionexp = actionexp;
	}

	public String getPageurl() {
		return pageurl;
	}

	public void setPageurl(String pageurl) {
		this.pageurl = pageurl;
	}

	public String getIsreport() {
		return isreport;
	}

	public void setIsreport(String isreport) {
		this.isreport = isreport;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the reportInstanceId
	 */
	public String getReportInstanceId() {
		return reportInstanceId;
	}

	/**
	 * @param reportInstanceId the reportInstanceId to set
	 */
	public void setReportInstanceId(String reportInstanceId) {
		this.reportInstanceId = reportInstanceId;
	}

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTopmenudId() {
        return topmenudId;
    }

    public void setTopmenudId(String topmenudId) {
        this.topmenudId = topmenudId;
    }
}
