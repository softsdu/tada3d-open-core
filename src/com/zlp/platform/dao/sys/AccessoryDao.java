package com.zlp.platform.dao.sys;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.model.sysmodel.DataField;
import org.apache.commons.fileupload.util.Streams;
import org.hibernate.Session;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import org.hibernate.Transaction;

public class AccessoryDao implements IAccessoryDao{

	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}
	
	private FileOperate fileOperate; 
	public void setFileOperate(FileOperate fileOperate) {
		this.fileOperate = fileOperate;
	}  
	private FileOperate getFileOperate() {
		return this.fileOperate;
	}
	
	private Session dbSession = null;
	protected Session getDBSession(){ 
		if(this.dbSession == null){
			throw new RuntimeException("none db session.");
		}
		return this.dbSession;
	} 
	public void setDBSession(Session dbSession){
		this.dbSession = dbSession;
	}

	private String uploadFileDir = "";
	public String getUploadFileDir() {
		return uploadFileDir;
	}
	public void setUploadFileDir(String uploadFileDir) {
		this.uploadFileDir = uploadFileDir;
	}

	private String backupFileDir = "";
	public String getBackupFileDir() {
		return backupFileDir;
	}
	public void setBackupFileDir(String backupFileDir) {
		this.backupFileDir = backupFileDir;
	}

	//新增附件扩展名白名单，如果未定义则不限制；如果需定义，直接 jpg/dxf/s3d/fbx/obj  这种格式定义
	private Map<String, String> fileExtensionMap = null;
	private Map<String, String> getFileExtensionMap(){
		return this.fileExtensionMap;
	}
	public void setFileExtensionMap(Map<String, String> fileExtensionMap){
		this.fileExtensionMap = fileExtensionMap;
	}

	private Map<String, String> dataName2AccessoryIdFieldNames = null;
	private Map<String, String> getDataName2AccessoryIdFieldNames(){
		return this.dataName2AccessoryIdFieldNames;
	}
	public void setDataName2AccessoryIdFieldNames(Map<String, String> dataName2AccessoryIdFieldNames){
		this.dataName2AccessoryIdFieldNames = dataName2AccessoryIdFieldNames;
	}

	private Map<String, String> dataName2AccessoryFilterTypes = null;
	private Map<String, String> getDataName2AccessoryFilterTypes(){
		return this.dataName2AccessoryFilterTypes;
	}
	public void setDataName2AccessoryFilterTypes(Map<String, String> dataName2AccessoryFilterTypes){
		this.dataName2AccessoryFilterTypes = dataName2AccessoryFilterTypes;
	}
	
	public String getFilePathByNameAndUploadTime(String name, Date uploadTime, String millisecond){
        SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMddHHmmss");   
        String  uploadTimeStr = sdfFileName.format(uploadTime);
        String filePath = this.getUploadFileDir() + uploadTimeStr.substring(0, 8) + "/" + uploadTimeStr.substring(8) + millisecond + name;
        return filePath;
	}

	public String getDirPathByNameAndUploadTime(String name, Date uploadTime){
        SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd");   
        String  uploadDateStr = sdfFileName.format(uploadTime);
        String dirPath = this.getUploadFileDir() + uploadDateStr;
        return dirPath;
	}
	
	public String getFilePathById(String id) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond from d_accessory a where a.id = " + SysConfig.getParamPrefix()+"id";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("id", id);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		if(rows.size() == 0){
			throw new Exception("none accessory。 id="+id);
		}
		else{
			DataRow row = rows.get(0);
			String name = row.getStringValue("name");
			String millisecond = row.getStringValue("millisecond");
			Date uploadTime = row.getDateTimeValue("uploadtime");
			return this.getFilePathByNameAndUploadTime(name, uploadTime, millisecond);
		}		
	}
	
	public String getFilePath(String id, String filterType, String filterValue, String uploadUserId) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond from d_accessory a where a.id = " + SysConfig.getParamPrefix()+"id"
				+ " and a.filtertype = " + SysConfig.getParamPrefix()+"filtertype"
				+ " and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue"
				+ " and a.uploaduserid = " + SysConfig.getParamPrefix()+"uploaduserid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("id", id);
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		p2vs.put("uploaduserid", uploadUserId);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		if(rows.size() == 0){
			throw new Exception("none accessory。 id="+id);
		}
		else{
			DataRow row = rows.get(0);
			String name = row.getStringValue("name");
			String millisecond = row.getStringValue("millisecond");
			Date uploadTime = row.getDateTimeValue("uploadtime");
			return this.getFilePathByNameAndUploadTime(name, uploadTime, millisecond);
		}		
	}

	public DataRow getAccessoryDataRow(String id, String filterType, String filterValue, String uploadUserId) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond, a.filetype as filetype from d_accessory a where a.id = " + SysConfig.getParamPrefix()+"id"
				+ " and a.filtertype = " + SysConfig.getParamPrefix()+"filtertype"
				+ " and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue"
				+ " and a.uploaduserid = " + SysConfig.getParamPrefix()+"uploaduserid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("id", id);
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		p2vs.put("uploaduserid", uploadUserId);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond", "filetype"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		if(rows.size() == 0){
			throw new Exception("none accessory。 id="+id);
		}
		else{
			return rows.get(0);
		}
	}

	public List<DataRow> getAccessoryDataRows(String filterType, String filterValue) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond, a.filetype as filetype from d_accessory a "
				+ " where a.filtertype = " + SysConfig.getParamPrefix()+"filtertype"
				+ " and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond", "filetype"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		if(rows.isEmpty()){
			throw new Exception("none accessory. FilterType=" + filterType + ", FilterValue=" + filterValue);
		}
		else{
			return rows;
		}
	}
	
	//getAccessoryDataRow added by ls 20190628
	public DataRow getAccessoryDataRow(String id) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond, a.filetype as filetype from d_accessory a where a.id = " + SysConfig.getParamPrefix()+"id";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("id", id);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond", "filetype"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		if(rows.isEmpty()){
			throw new Exception("none accessory. id="+id);
		}
		else{
			return rows.get(0);
		}		
	}

	public String[] getFilePathByFilter(String filterType, String filterValue) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime, a.millisecond as millisecond from d_accessory a where a.filtertype = " + SysConfig.getParamPrefix()+"filtertype and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime", "millisecond"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time, ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		List<String> filePaths = new ArrayList<String>(); 
		for(DataRow row : rows){ 
			String name = row.getStringValue("name");
			String millisecond = row.getStringValue("millisecond");
			Date uploadTime = row.getDateTimeValue("uploadtime");
			filePaths.add(this.getFilePathByNameAndUploadTime(name, uploadTime, millisecond));
		}
		return (String[])filePaths.toArray();
	}

	public String[] getAccessoryIds(String filterType, String filterValue) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id from d_accessory a where a.filtertype = " + SysConfig.getParamPrefix() + "filtertype and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id"}, new ValueType[]{ValueType.String});
		List<DataRow> rows = accessoryDt.getRows();
		List<String> ids = new ArrayList<String>(); 
		for(DataRow row : rows){ 
			String id = row.getStringValue("id"); 
			ids.add(id);
		}
		return (String[])ids.toArray();
	}
	
	public int getFileCountByFilter(String filterType, String filterValue) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select a.id as id, a.name as name, a.uploadtime as uploadtime from d_accessory a where a.filtertype = " + SysConfig.getParamPrefix()+"filtertype and a.filtervalue = " + SysConfig.getParamPrefix()+"filtervalue";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("filtertype", filterType);
		p2vs.put("filtervalue", filterValue);
		DataTable accessoryDt = dbAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "name", "uploadtime"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.Time});
		return accessoryDt.getRows().size();
	}

	//创建附件记录 added by ls 20230828
	@Override
	public String insertAccessoryRow(INcpSession session, String name, Date uploadTime, String millisecond, String filterType, String filterValue) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess(); 
		Data data = DataCollection.getData("d_Accessory");
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		String fileType = name.substring (name.lastIndexOf (".") + 1).toLowerCase();
		fieldValues.put("name", name);
		fieldValues.put("filetype", fileType);
		fieldValues.put("uploadtime", uploadTime); 
		fieldValues.put("millisecond", millisecond);
		fieldValues.put("filtertype", filterType);
		fieldValues.put("filtervalue", filterValue);
		fieldValues.put("isdeleted", "N");
		fieldValues.put("uploaduserid", session.getUserId());
		return dbAccess.insertByData(this.getDBSession(), data, fieldValues);
	} 
	
	public String saveAccessory(INcpSession session, InputStream inputStream, String fileName, String filterType, String filterValue) throws Exception{

		//增加上传附件对扩展名白名单的定义 added by liyh 20240204
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		if(fileName.contains(".") && fileExtensionMap!=null && !fileExtensionMap.containsKey(fileExtension)) {
			throw new Exception("附件上传失败:不支持的文件类型!");
		}

		Date uploadTime = new Date();
		String dirPath = this.getDirPathByNameAndUploadTime(fileName, uploadTime);
		this.getFileOperate().createFolder(dirPath);
        SimpleDateFormat sdfFileName = new SimpleDateFormat("SSS");
        String millisecond = sdfFileName.format(uploadTime);
		String filePath = this.getFilePathByNameAndUploadTime(fileName, uploadTime, millisecond);
		BufferedInputStream in = new BufferedInputStream(inputStream);// 获得文件输入流
        BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(filePath));// 获得文件输出流
        Streams.copy(in, outStream, true);// 开始把文件写到你指定的上传文件夹
        return this.insertAccessoryRow(session, fileName, uploadTime, millisecond, filterType, filterValue);
	}
	
	public void deleteAccessory(String id) throws Exception{
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "update d_accessory set isdeleted = 'Y' where id = " + SysConfig.getParamPrefix() + "id";
		HashMap<String, Object> ps = new HashMap<String, Object>();
		ps.put("id", id);
		dbAccess.update(this.getDBSession(), sql, ps);
	}

	//保存附件记录ID值到业务表 modified by ls 202203	
	public void saveAccessoryIdStrToDB(String idValue, String tableName,String fieldName,String idsStr){
		IDBParserAccess dbAccess = this.getDBParserAccess();
		Data data = DataCollection.getData(tableName);
		String idFieldName = data.getIdFieldName();
		String sql = "update " + tableName + " set  " + fieldName + " = :accessoryIdStr "
				+ " where " + idFieldName + " = " + SysConfig.getParamPrefix() +"id";
		HashMap<String, Object> ps = new HashMap<String, Object>();
		ps.put("id", idValue);
		ps.put("accessoryIdStr", idsStr); 
    	dbAccess.update(getDBSession(), sql, ps);
	}

	//删除没有关联的附件记录（打删除标记）
	@Override
	public void deleteUnrelatedAccessory(){
		this.deleteUnrelatedAccessory(this.getDataName2AccessoryIdFieldNames(), this.getDataName2AccessoryFilterTypes());
	}

	//删除没有关联的附件记录（打删除标记）
	private void deleteUnrelatedAccessory(Map<String, String> dataName2AccessoryIdFieldNames, Map<String, String> dataName2AccessoryFilterTypes){
		Transaction tx = null;
		try {
			Session dbSession = this.getDBSession();
			tx = dbSession.beginTransaction();
			IDBParserAccess dbAccess = this.getDBParserAccess();
			String disableAccessorySql = "update d_accessory set isdeleted = 'Y'";
			dbAccess.update(dbSession, disableAccessorySql, null);

			for (String dataName : dataName2AccessoryIdFieldNames.keySet()) {
				Data data = DataCollection.getData(dataName);
				String accessoryIdFieldNameStr = dataName2AccessoryIdFieldNames.get(dataName);
				DataTable dt = null;
				DataField isDeletedField = data.getDataField("isdeleted");
				if(isDeletedField == null) {
					dt = dbAccess.getDt(dbSession, data);
				}
				else {
					dt = dbAccess.getDtByFieldValue(dbSession, data, "isdeleted", "=", "N");
				}
				List<DataRow> rows = dt.getRows();
				String[] accessoryIdFieldNames = accessoryIdFieldNameStr.split(",");
				for(DataRow row : rows) {
					for(String accessoryIdFieldName : accessoryIdFieldNames) {
						String accessoryId = row.getStringValue(accessoryIdFieldName);
						String enableAccessorySql = "update d_accessory set isdeleted = 'N' where id = " + SysConfig.getParamPrefix() + "id";
						HashMap<String, Object> p2vs = new HashMap<>();
						p2vs.put("id", accessoryId);
						dbAccess.update(dbSession, enableAccessorySql, p2vs);
					}
				}
			}

			for (String dataName : dataName2AccessoryFilterTypes.keySet()) {
				Data data = DataCollection.getData(dataName);
				String filterTypeStr = dataName2AccessoryFilterTypes.get(dataName);
				DataTable dt = null;
				DataField isDeletedField = data.getDataField("isdeleted");
				if(isDeletedField == null) {
					dt = dbAccess.getDt(dbSession, data);
				}
				else {
					dt = dbAccess.getDtByFieldValue(dbSession, data, "isdeleted", "=", "N");
				}
				List<DataRow> rows = dt.getRows();
				String[] filterTypes = filterTypeStr.split(",");
				for(DataRow row : rows) {
					for(String filterType : filterTypes) {
						//使用业务表id作为附件表filtervalue的值的情况
						String dataId = row.getStringValue(data.getIdFieldName());
						String enableAccessorySql = "update d_accessory set isdeleted = 'N' where filterType = " + SysConfig.getParamPrefix() + "filtertype and filterValue = " + SysConfig.getParamPrefix() + "filtervalue";
						HashMap<String, Object> p2vs = new HashMap<>();
						p2vs.put("filtertype", filterType);
						p2vs.put("filtervalue", dataId);
						dbAccess.update(dbSession, enableAccessorySql, p2vs);
					}
				}
			}
			tx.commit();
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		}
	}

	//备份所有附件
	@Override
	public JSONObject backupAllAccessoryFiles() throws Exception {
		String backupDestDir = this.getBackupFileDir() + ValueConverter.dateTimeToString(new Date(), "yyyyMMddHHmmss") + "/";
		int copiedCount = this.backupAllAccessoryFiles(backupDestDir);
		JSONObject resultJson = new JSONObject();
		resultJson.put("dir", backupDestDir);
		resultJson.put("count", copiedCount);
		return resultJson;
	}

	public int backupAllAccessoryFiles(String backupDestDir) throws Exception {
		Data data = DataCollection.getData("d_Accessory");
		IDBParserAccess dbAccess = this.getDBParserAccess();
		FileOperate fileOperate = this.getFileOperate();
		DataTable dt = dbAccess.getDtByFieldValue(dbSession, data, "isdeleted", "=", "N");
		List<DataRow> rows = dt.getRows();
		for(DataRow row : rows){
			String accessoryId = row.getStringValue("id");
			String sourceFilePath = this.getFilePathById(accessoryId);
			Path sourcePath = Paths.get(sourceFilePath);
			Path sourceParent = sourcePath.getParent();
			String folderName = sourceParent.getFileName().toString();
			String destDirPath = backupDestDir + folderName;
			File destDir = new File(destDirPath);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			String destFilePath = destDirPath + "/" + sourcePath.getFileName();
			fileOperate.copyFile(sourceFilePath, destFilePath);
		}
		return rows.size();
	}
}
