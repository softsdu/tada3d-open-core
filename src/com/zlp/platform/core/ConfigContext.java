package com.zlp.platform.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap; 
import java.util.Map;
import java.util.Properties; 
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.zlp.platform.constants.ZlpState; 

public class ConfigContext { 
	
	private static final Logger logger = LogManager.getLogger(ConfigContext.class.getName());
	public static final String PROPERTIES_FILE = "system.properties";
	private static Properties reportProperties = new Properties();
	private static Map<String, String> configMap = new HashMap<String, String>();
	private static boolean isInited = false;
	
	public synchronized static boolean initContext() {
		if (isInited) {
			return true;
		}
		logger.info("init ConfigContext...");
		InputStream is = ConfigContext.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		try {
			reportProperties.load(is);
		} catch (IOException e) {
			throw new RuntimeException(PROPERTIES_FILE + " file load error");
		}
		
		//加载可供修改的banner图片地址
		configMap.put(ZlpState.PLATFORM_BANNER, reportProperties.getProperty(ZlpState.PLATFORM_BANNER_PROPERTIES));
		//当前所采用的样式
		configMap.put(ZlpState.PLATFORM_UI_STYLE, reportProperties.getProperty(ZlpState.PLATFORM_UI_STYLE_PROPERTIES));
		//加载系统默认密码
		configMap.put(ZlpState.PLATFORM_DEFAULT_PASSWORD, reportProperties.getProperty(ZlpState.PLATFORM_DEFAULT_PASSWORD_PROPERTIES));
		//加载项目名称
		configMap.put(ZlpState.PLATFORM_PROJECT_NAME, reportProperties.getProperty(ZlpState.PLATFORM_PROJECT_NAME_PROPERTIES));
		//加载公司名称 added by ls 20190618
		configMap.put(ZlpState.PLATFORM_COMPANY_NAME, reportProperties.getProperty(ZlpState.PLATFORM_COMPANY_NAME_PROPERTIES));
		//加载授权序列号 added by ls 20190618
		configMap.put(ZlpState.PLATFORM_LICENSE_SN, reportProperties.getProperty(ZlpState.PLATFORM_LICENSE_SN_PROPERTIES));
		//用户退出系统的请求地址
		configMap.put(ZlpState.PLATFORM_REQUEST_URL, reportProperties.getProperty(ZlpState.PLATFORM_REQUEST_URL_PROPERTIES));
		//用户退出系统请求允许后，返回的页面地址(此处仅应用于servlet)
		configMap.put(ZlpState.PLATFORM_PAGEJUMP_URL, reportProperties.getProperty(ZlpState.PLATFORM_PAGEJUMP_URL_PROPERTIES));
		//邮箱服务器参数值
		configMap.put(ZlpState.PLATFORM_EMAIL_SERVER, reportProperties.getProperty(ZlpState.PLATFORM_EMAIL_SERVER_PROPERTIES));
		//发送邮箱是否需要认证
		configMap.put(ZlpState.PLATFORM_EMAIL_NEED_LOGIN, reportProperties.getProperty(ZlpState.PLATFORM_EMAIL_NEED_LOGIN_PROPERTIES));
		//发送邮箱用户名称
		configMap.put(ZlpState.PLATFORM_EMAIL_USERNAME, reportProperties.getProperty(ZlpState.PLATFORM_EMAIL_USERNAME_PROPERTIES));
		//邮箱帐号密码参数值
		configMap.put(ZlpState.PLATFORM_EMAIL_ACCOUNT_PWD, reportProperties.getProperty(ZlpState.PLATFORM_EMAIL_ACCOUNT_PWD_PROPERTIES));
		//邮箱帐号参数值
		configMap.put(ZlpState.PLATFORM_EMAIL_ACCOUNT, reportProperties.getProperty(ZlpState.PLATFORM_EMAIL_ACCOUNT_PROPERTIES));
		//file storage dir path in data managment function
		configMap.put(ZlpState.PLATFORM_DATAMANAGMENT_STORAGEDIR, reportProperties.getProperty(ZlpState.PLATFORM_DATAMANAGMENT_STORAGEDIR_PROPERTIES));
		//workflow timing drive user id
		configMap.put(ZlpState.PLATFORM_WORKFLOW_TIMINGDRIVEUSERID, reportProperties.getProperty(ZlpState.PLATFORM_WORKFLOW_TIMINGDRIVEUSERID_PROPERTIES));


		//模型页面模板保存地址 added by ls 20190618
		configMap.put(ZlpState.MODEL_PAGES_TEMPLATES_SHEETGRID, reportProperties.getProperty(ZlpState.MODEL_PAGES_TEMPLATES_SHEETGRID_PROPERTIES));
		configMap.put(ZlpState.MODEL_PAGES_TEMPLATES_SHEETSHEET, reportProperties.getProperty(ZlpState.MODEL_PAGES_TEMPLATES_SHEETSHEET_PROPERTIES));
		configMap.put(ZlpState.MODEL_PAGES_TEMPLATES_TREEGRID, reportProperties.getProperty(ZlpState.MODEL_PAGES_TEMPLATES_TREEGRID_PROPERTIES));
		configMap.put(ZlpState.MODEL_PAGES_TEMPLATES_TREECARD, reportProperties.getProperty(ZlpState.MODEL_PAGES_TEMPLATES_TREECARD_PROPERTIES));
		configMap.put(ZlpState.MODEL_PAGES_TEMPLATES_VIEWGRID, reportProperties.getProperty(ZlpState.MODEL_PAGES_TEMPLATES_VIEWGRID_PROPERTIES));
				
		//保存cookie时，指定的Path地址
		configMap.put(ZlpState.COOKIE_WEB_PATH, reportProperties.getProperty(ZlpState.COOKIE_WEB_PATH_PROPERTIES));

		//是否集成 mdm 接口 added by yay 20221109
		configMap.put(ZlpState.IS_INTEGRATE_MDM, reportProperties.getProperty(ZlpState.IS_INTEGRATE_MDM));
		//集成 mdm 接口url added by yay 20221129
		configMap.put(ZlpState.INTEGRATE_MDM_BASE_URL, reportProperties.getProperty(ZlpState.INTEGRATE_MDM_BASE_URL));
		//aes 双向加密解密key  added by yay 20221215
		configMap.put(ZlpState.AES_SECRET_KEY, reportProperties.getProperty(ZlpState.AES_SECRET_KEY));

		//具体系统路径配置 - - - 主要用于不同系统环境下的一些自定义配置(css、js等配置文件) added by liyh 20231031
		configMap.put(ZlpState.SYS_NAME_PATH, reportProperties.getProperty(ZlpState.SYS_NAME_PATH));

		//host和port
		configMap.put(ZlpState.PROJECT_ROOTURL, reportProperties.getProperty(ZlpState.PROJECT_ROOTURL_PROPERTIES));

		//test();
		
		isInited = true;
		return true;
	} 
	
	/**
	 * 
	 * @return
	 */
	public static Map<String, String> getConfigMap() {
		return configMap;
	}
}
