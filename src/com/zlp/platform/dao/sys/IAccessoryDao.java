package com.zlp.platform.dao.sys;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.hibernate.Session;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.db.DataRow;

public interface IAccessoryDao { 
	void setDBSession(Session dbSession);
	String getFilePathByNameAndUploadTime(String name, Date uploadTime, String millisecond);
	String getDirPathByNameAndUploadTime(String name, Date uploadTime);
	String getFilePathById(String id) throws Exception; 
	String[] getFilePathByFilter(String filterType, String filterValue) throws Exception;
	int getFileCountByFilter(String filterType, String filterValue) throws Exception;
	String[] getAccessoryIds(String filterType, String filterValue) throws Exception;
	void deleteAccessory(String id) throws Exception;
	String getFilePath(String id, String filterType, String filterValue, String uploadUserId) throws Exception; 
	String saveAccessory(INcpSession session, InputStream inputStream, String fileName, String filterType, String filterValue) throws Exception;
	DataRow getAccessoryDataRow(String id, String filterType, String filterValue, String uploadUserId) throws Exception;
	List<DataRow> getAccessoryDataRows(String filterType, String filterValue) throws Exception;

	//getAccessoryDataRow added by ls 20190628
	DataRow getAccessoryDataRow(String id) throws Exception;
	
	//added by liyh 20190617
	void saveAccessoryIdStrToDB(String billId, String tablename,String columnname,String accessoryIdStr);
	
	//创建附件记录 added by ls 20230828
	String insertAccessoryRow(INcpSession session, String name, Date uploadTime, String millisecond, String filterType, String filterValue) throws Exception;

    //删除没有关联的附件记录（打删除标记）
	void deleteUnrelatedAccessory();

	//备份所有附件
	JSONObject backupAllAccessoryFiles() throws Exception;
}
