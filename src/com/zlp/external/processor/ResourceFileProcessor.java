package com.zlp.external.processor;
 
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Session;

import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ISequenceGenerator;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.sys.IAccessoryDao;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 

public class ResourceFileProcessor implements IResourceFileProcessor{ 
	private IDBParserAccess dBParserAccess;
	public void setDBParserAccess(IDBParserAccess dBParserAccess){ 
		this.dBParserAccess = dBParserAccess;
	}	
	
	public IDBParserAccess getDBParserAccess(){ 
		return this.dBParserAccess;
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

	private ISequenceGenerator sequenceGenerator;
	public ISequenceGenerator getSequenceGenerator(){
		return this.sequenceGenerator;
	}
	public void setSequenceGenerator(ISequenceGenerator sequenceGenerator){
		this.sequenceGenerator = sequenceGenerator;
	}
	
	private IAccessoryDao accessoryDao;
	public IAccessoryDao getAccessoryDao() {
		return accessoryDao;
	}
	public void setAccessoryDao(IAccessoryDao accessoryDao) {
		this.accessoryDao = accessoryDao;
	}   
	
	private FileOperate fileOperate; 
	public void setFileOperate(FileOperate fileOperate) {
		this.fileOperate = fileOperate;
	}  
	protected FileOperate getFileOperate() {
		return this.fileOperate;
	}

	protected void zipFile(String sourceFilePath, String fileName, ZipOutputStream zos) throws IOException{
		File sourceFile = new File(sourceFilePath);
		if(sourceFile.exists()){
			BufferedInputStream bis = null;
			try{
				bis = new BufferedInputStream(new FileInputStream(sourceFile));
				zos.putNextEntry(new ZipEntry(fileName));
				while(true){
					byte[] b = new byte[100];
					int len = bis.read(b);
					if(len == -1){
						break ;
					}
					zos.write(b, 0, len);
				}
			}
			catch(Exception ex){
				throw ex;
			}
			finally{
				if(bis != null){
					bis.close();
				}
			}
		}
	}

	protected void zipText(String text, String fileName, ZipOutputStream zos) throws IOException {
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		InputStream stream = new ByteArrayInputStream(bytes);
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(stream);
			zos.putNextEntry(new ZipEntry(fileName));
			while (true) {
				byte[] b = new byte[100];
				int len = bis.read(b);
				if (len == -1) {
					break;
				}
				zos.write(b, 0, len);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}
    
    private byte[] getFileData(String zipFilePath) throws Exception{ 
        FileInputStream fis = null;
        try { 
	        
	        File file = new File(zipFilePath);
	        fis = new FileInputStream(file);	
	        long size = file.length();
	        byte[] temp = new byte[(int) size];
	        fis.read(temp, 0, (int) size); 
	        byte[] data = temp;
			return data;  
        }
        catch (Exception e) { 
			throw e;
		}   
		finally{
			if(fis != null){
				fis.close();
			} 
		}
    }	
}
