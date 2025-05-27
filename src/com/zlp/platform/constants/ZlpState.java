package com.zlp.platform.constants;

import java.io.File;

/**
 * @Description: platform通用常量类 
 */
public class ZlpState {
	
	//platform banner images
	public static final String PLATFORM_BANNER = "novaBanner";
	public static final String PLATFORM_BANNER_PROPERTIES = "platform.banner.images";
	
	//platform  ui style
	public static final String PLATFORM_UI_STYLE = "novaUIStyle";
	public static final String PLATFORM_UI_STYLE_PROPERTIES = "platform.ui.style";
	
	//platform default password
	public static final String PLATFORM_DEFAULT_PASSWORD = "defaultPassord";
	public static final String PLATFORM_DEFAULT_PASSWORD_PROPERTIES = "default.password";
	
	//platform project name
	public static final String PLATFORM_PROJECT_NAME = "projectName";
	public static final String PLATFORM_PROJECT_NAME_PROPERTIES = "project.title";

	//platform license sn added by ls 20190618
	public static final String PLATFORM_LICENSE_SN = "licenseSN";
	public static final String PLATFORM_LICENSE_SN_PROPERTIES = "license.sn";  
	 
	//platform company name added by ls 20190618
	public static final String PLATFORM_COMPANY_NAME = "companyName";
	public static final String PLATFORM_COMPANY_NAME_PROPERTIES = "company.name";
	
	//用户退出系统的请求地址(支持servlet和struts)
	public static final String PLATFORM_REQUEST_URL = "requestUrl";
	public static final String PLATFORM_REQUEST_URL_PROPERTIES = "logout.request.url";
	
	//用户退出系统请求允许后，返回的页面地址(此处仅应用于servlet)
	public static final String PLATFORM_PAGEJUMP_URL = "pageJumpUrl";
	public static final String PLATFORM_PAGEJUMP_URL_PROPERTIES = "logout.pagejump.url";
	
	//邮箱服务器参数值
	public static final String PLATFORM_EMAIL_SERVER = "emailServer";
	public static final String PLATFORM_EMAIL_SERVER_PROPERTIES = "email.server";
	
	//发送邮箱是否需要认证
	public static final String PLATFORM_EMAIL_NEED_LOGIN = "emailNeedLogin";
	public static final String PLATFORM_EMAIL_NEED_LOGIN_PROPERTIES = "email.needlogin";
	
	//发送邮箱用户名称
	public static final String PLATFORM_EMAIL_USERNAME = "emailUsername";
	public static final String PLATFORM_EMAIL_USERNAME_PROPERTIES = "email.username";
	
	//邮箱帐号密码参数值
	public static final String PLATFORM_EMAIL_ACCOUNT_PWD = "emailAccountPwd";
	public static final String PLATFORM_EMAIL_ACCOUNT_PWD_PROPERTIES = "email.accountpwd";
	
	//邮箱帐号参数值
	public static final String PLATFORM_EMAIL_ACCOUNT = "emailAccount";
	public static final String PLATFORM_EMAIL_ACCOUNT_PROPERTIES = "email.account";
	

	//platform前端组件库目录地址
	public static final String PLATFORM_STYLE_PATH = File.separator + "platform" + File.separator;
	
	//DB相关 added ls 20230601
	public static final String JDBC_DRIVERCLASSNAME = "jdbc.driverClassName";
	public static final String JDBC_DRIVERCLASSNAME_PROPERTIES = "jdbc.driverClassName";
	
	//platform前端js模型目录地址
	public static final String DATA_MODEL_PATH = PLATFORM_STYLE_PATH + "model" + File.separator;
	
	//前端view模型目录
	public static final String DATA_MODEL_PATH_OF_VIEW = DATA_MODEL_PATH + "view" + File.separator;
	//前端data模型目录
	public static final String DATA_MODEL_PATH_OF_DATA = DATA_MODEL_PATH + "data" + File.separator;
	//前端report模型目录
	public static final String DATA_MODEL_PATH_OF_REPORT = DATA_MODEL_PATH + "report" + File.separator;
	//前端sheet模型目录
	public static final String DATA_MODEL_PATH_OF_SHEET = DATA_MODEL_PATH + "sheet" + File.separator;
	//前端tree模型目录
	public static final String DATA_MODEL_PATH_OF_TREE= DATA_MODEL_PATH + "tree" + File.separator;
	//前端paramWin模型目录
	public static final String DATA_MODEL_PATH_OF_PARAMWIN= DATA_MODEL_PATH + "paramWin" + File.separator;
	
	//file storage dir path in data managment function 
	public static final String PLATFORM_DATAMANAGMENT_STORAGEDIR = "dataManagmentStorageDir";
	public static final String PLATFORM_DATAMANAGMENT_STORAGEDIR_PROPERTIES = "platform.datamanagment.storageDir";
	
	//workflow timing drive user id
	public static final String PLATFORM_WORKFLOW_TIMINGDRIVEUSERID = "workflowTimingDriveUserId";
	public static final String PLATFORM_WORKFLOW_TIMINGDRIVEUSERID_PROPERTIES = "platform.workflow.timingDriveUserId";

	//模型页面模板保存地址 added by ls 20190618	
	public static final String MODEL_PAGES_TEMPLATES_SHEETGRID = "modelPages.templates.sheetGrid";
	public static final String MODEL_PAGES_TEMPLATES_SHEETGRID_PROPERTIES = "modelPages.templates.sheetGrid";
	public static final String MODEL_PAGES_TEMPLATES_SHEETSHEET = "modelPages.templates.sheetSheet";
	public static final String MODEL_PAGES_TEMPLATES_SHEETSHEET_PROPERTIES = "modelPages.templates.sheetSheet";
	public static final String MODEL_PAGES_TEMPLATES_TREECARD = "modelPages.templates.treeCard";
	public static final String MODEL_PAGES_TEMPLATES_TREECARD_PROPERTIES = "modelPages.templates.treeCard";
	public static final String MODEL_PAGES_TEMPLATES_TREEGRID = "modelPages.templates.treeGrid";
	public static final String MODEL_PAGES_TEMPLATES_TREEGRID_PROPERTIES = "modelPages.templates.treeGrid";
	public static final String MODEL_PAGES_TEMPLATES_VIEWGRID = "modelPages.templates.viewGrid";
	public static final String MODEL_PAGES_TEMPLATES_VIEWGRID_PROPERTIES = "modelPages.templates.viewGrid";

	//系统page文件夹相对路径 added by ls 20190618	
	public static final String PAGE_PATH = File.separator + "web";


	//project rootUrl
	public static final String PROJECT_ROOTURL = "project.rootUrl";
	public static final String PROJECT_ROOTURL_PROPERTIES = "project.rootUrl";
	
	//导入导出查询页面所在文件夹相对路径 added by ls 20190618	
	public static final String IMPORTEXPORT_QUERYPAGES_DIR = PAGE_PATH + File.separator + "ie" + File.separator + "queryPages" + File.separator;

	//自动生成单表模型的jsp页面的地址 added by ls 20190618	
	public static final String GENERATED_MODEL_PAGES_DIR_OF_VIEW = PAGE_PATH + File.separator + "ps" + File.separator + "view" + File.separator;
	
	//自动生成父子表模型的jsp页面的地址 added by ls 20190618	
	public static final String GENERATED_MODEL_PAGES_DIR_OF_SHEET = PAGE_PATH + File.separator + "ps" + File.separator + "sheet" + File.separator;
	
	//自动生成树形模型的jsp页面的地址 added by ls 20190618	
	public static final String GENERATED_MODEL_PAGES_DIR_OF_TREE = PAGE_PATH + File.separator + "ps" + File.separator + "tree" + File.separator;
		
	//保存cookie时，指定的path地址
	public static final String COOKIE_WEB_PATH = "cookie.path";
	public static final String COOKIE_WEB_PATH_PROPERTIES = "cookie.path";

	//是否集成 mdm 接口 added by yay 20221118
	public static final String IS_INTEGRATE_MDM = "is_integrate_mdm";
	//集成 mdm 接口url added by yay 20221129
	public static final String INTEGRATE_MDM_BASE_URL = "integrate_mdm_base_url";
	//aes 双向加密解密key  added by yay 20221215
	public static final String AES_SECRET_KEY = "aes_secret_key";

	//具体系统路径配置 - - - 主要用于不同系统环境下的一些自定义配置(css、js等配置文件) added by liyh 20231031
	public static final String SYS_NAME_PATH = "sysName.path";
}
