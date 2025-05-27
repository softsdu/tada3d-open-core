package com.zlp.mdl.processor;

import java.util.HashMap;
import org.hibernate.Session;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession; 

public interface IMdlComponentProcessor {

	void setDBSession(Session dbSession);

	JSONObject getComponentFile(INcpSession session, String componentId) throws Exception;

	JSONObject getComponentFileByCode(INcpSession session, String componentCode, String versionNum) throws Exception;

	JSONObject getComponentFileByName(INcpSession session, String componentName, String versionNum) throws Exception;

	//根据国标码+版本号 获取部品信息 added by liyh 20220424
	JSONObject getComponentFileByGbCode(INcpSession session, String gbCode, String versionNum,String note) throws Exception;

	String createComponent(INcpSession session, String componentCode, String componentName, String note,String shareType,String gbCode, String versionNum, String categoryId, String mdlType
			,String m900,String m904,String goodsMid,String sBaseMid,String metadata,String userId,String companyId,JSONObject unitSettingObject,String projectid,String projectunitid) throws Exception;

	void saveComponent(INcpSession session, String componentId, String componentCode, String componentName, String versionNum, String content, String image, String userId) throws Exception;

	void deleteComponent(INcpSession session, String componentId,String userId,String isdeleted) throws Exception;

	void changeComponentProperty(INcpSession session, String componentId, String componentCode, String componentName, String note,String shareType,String gbCode, String versionNum, String categoryId
			,String m900,String m904,String goodsMid,String sBaseMid,String metadata,String userId,String projectid,String projectunitid) throws Exception;

	//发布 added by ls 20230723
	void changePublishStatus(INcpSession session, String componentId, boolean isPublished) throws Exception;

	void getComponentJsonRecursion(INcpSession session, JSONObject componentJson, HashMap<String, JSONObject> refComponentJsonMap) throws Exception;

	String exportLZW(INcpSession session, String componentId, String componentCode, String componentName, JSONObject mainInfoJson, JSONArray allUnitSettingArray, JSONObject allGeoContentsJson, JSONObject allMaterialsJson) throws Exception;

	//根据编码和版本号，获取组件信息 added by ls 20210820
	JSONObject getComponentInfoByCode(INcpSession session, String componentCode, String versionNum) throws Exception;

	//复制组件 added by ls 20210820
	String copyComponent(INcpSession session, String copiedId, String code, String name, String versionNum, String categoryId, String mdlType,String note,String shareType,String gbCode
			,String userId,String companyId) throws Exception;

	//获取类型的属性列表 added by ls 20210824
	JSONArray getCategoryProperties(INcpSession session, String categoryId) throws Exception;

	//获取组件统计指标列表 added by liyh 20211130
	JSONArray  getMdlStatisticIndexList(INcpSession session, JSONObject requestObj) throws Exception;

	JSONArray getCategoryTree(INcpSession session, String mdlType) throws Exception;

	JSONArray getCategoryTree(INcpSession session, String[] mdlTypes) throws Exception;
 
	JSONArray queryComponents(INcpSession session, String componentName, String categoryId, JSONArray propertyJsonArray)
			throws Exception;

	//创建component的内容文本（涉及返回模型文件20220714）  added by ls 20220705
	String createComponentText(INcpSession session, String componentCode, String componentName, String versionNum, String categoryCode) throws Exception;

	//获取组件最后一次修改时间 add by ls 20220913
	String getLastModifyTimeMark(String id) throws Exception;

	//根据mdlType获取当前用户的模型列表  added by ls 20230518
	JSONArray queryUserComponents(INcpSession session, String mdlType, int rowCount) throws Exception;

	//已发布的模型  added by ls 20230723
	JSONArray queryPublishedComponents(INcpSession session, String mdlType, int rowCount) throws Exception;

	void saveImage(INcpSession session, String componentId, String imageBase64, String userId) throws Exception;
}
