package com.zlp.platform.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

//系统配置
public class SysConfig {
	private static String timeFormat = "yyyy-MM-dd HH:mm:ss";
	public static String getTimeFormat() {
		return timeFormat;
	} 
	
	private static String dateFormat = "yyyy-MM-dd";
	public static String getDateFormat() {
		return dateFormat;
	} 
	
	private static String paramPrefix = ":";
	public static String getParamPrefix() {
		return paramPrefix;
	} 

	public static String getPropertyFileValue(String propertyFile,String propertyName) throws Exception{
        try {  
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
            Properties p = new Properties();
            p.load(is);
            String value = p.getProperty(propertyName); 
            is.close();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
	}
    
    // 浏览器类型
    private static final String[] MobileAgents = { "iphone", "android", "phone", "mobile", "wap", "netfront", "java",
        "opera mobi", "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry",
        "dopod", "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola", "foma",
        "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad", "webos",
        "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips", "sagem", "wellcom",
        "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos", "pantech", "gionee",
        "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320", "240x320", "176x220", "w3c ",
        "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac", "blaz", "brew", "cell", "cldc",
        "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs", "kddi", "keji", "leno", "lg-c",
        "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi", "mot-", "moto", "mwbp", "nec-",
        "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port", "prox", "qwap", "sage", "sams",
        "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem", "smal", "smar", "sony", "sph-",
        "symb", "t-mo", "teli", "tim-", "tosh", "tsm-", "upg1", "upsi", "vk-v", "voda", "wap-", "wapa", "wapi",
        "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-", "Googlebot-Mobile" };
	
	public static boolean judgelsMobile(HttpServletRequest request) {
        boolean isMobile = false;
        String[] mobileAgents = MobileAgents;
        if (request.getHeader("User-Agent") != null) {
            for (String mobileAgent : mobileAgents) {
                if (request.getHeader("User-Agent").toLowerCase().indexOf(mobileAgent) > 0) {
                    isMobile = true;
                    break;
                }
            }
        }
        return isMobile;
    }
}
