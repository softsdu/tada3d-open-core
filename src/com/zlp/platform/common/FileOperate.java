package com.zlp.platform.common; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter; 
import java.io.UnsupportedEncodingException; 

public class FileOperate {
	public static final String DefaultEncoding = "utf-8";
	/**
	 * 遍历文件夹中文件
	 * 
	 * @param filepath
	 * @return 返回file［］数组
	 * @throws Exception 
	 */
	public File[] getFileList(String filepath) throws Exception {
		File d = null;
		File list[] = null;
		// 建立当前目录中文件的File对象
		try {
			d = new File(filepath);
			if (d.exists()) {
				list = d.listFiles();
			}
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
		// 取得代表目录中所有文件的File对象数组

		return list;
	}

	public String readTxt(String filePathAndName) throws Exception {
		return this.readTxt(filePathAndName, "UTF-8");
	}

	public boolean exists(String filePathAndName) throws Exception {
		File f = new File(filePathAndName);
		return f.exists();
	}
	
	/**
	 * 读取文本文件内容
	 * 
	 * @param filePathAndName
	 *            带有完整绝对路径的文件名
	 * @param encoding
	 *            文本文件打开的编码方式
	 * @return 返回文本文件的内容
	 * @throws Exception 
	 */
	public String readTxt(String filePathAndName, String encoding) throws Exception {
		encoding = encoding.trim(); 
		StringBuffer str = new StringBuffer("");
		String st = "";
		FileInputStream fs = null;
		InputStreamReader isr = null;
		try {
			fs = new FileInputStream(filePathAndName);  
			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + "\r\n");
				}
			} 
			catch (Exception ex) {
            	ex.printStackTrace();
				throw ex;
			}
			st = str.toString();
			if (st != null && st.length() > 1){
				st = st.substring(0, st.length() - 1);
			}
		} catch (IOException ex) {
        	ex.printStackTrace();
			throw ex;
		}
		finally{
			if(isr != null){
				isr.close();
			}
			if(fs != null){
				fs.close();
			}
		}
		return st;
	}

	/**
	 * 新建目录
	 * 
	 * @param folderPath
	 *            目录
	 * @return 返回目录创建后的路径
	 * @throws Exception 
	 */
	public String createFolder(String folderPath) throws Exception {
		String txt = folderPath;
		try {
			File myFilePath = new File(txt);
			txt = folderPath;
			if (!myFilePath.exists()) {
				myFilePath.mkdir();
			}
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
		return txt;
	} 

	/**
	 * 递归新建目录
	 * added by ls 20180404 新增函数
	 * @param file
	 *            目录  
	 */
    public void createFolderRecursion(File file) {
        if (file.getParentFile().exists()) {  
            file.mkdir();  
        } else {  
        	createFolderRecursion(file.getParentFile());  
            file.mkdir();    
        }  
    }  

	/**
	 * 新建文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 * @param fileContent
	 *            文本文件内容
	 * @return
	 * @throws Exception 
	 */
	public void createFile(String filePathAndName, String fileContent) throws Exception {
		 this.createFile(filePathAndName, fileContent, "UTF-8");
	}

	/**
	 * 有编码方式的文件创建
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 * @param fileContent
	 *            文本文件内容
	 * @param encoding
	 *            编码方式 例如 GBK 或者 UTF-8
	 * @return
	 * @throws Exception 
	 */
	public void createFile(String filePathAndName, String fileContent, String encoding) throws Exception {
		OutputStream out = null;
		OutputStreamWriter osr = null;
        BufferedWriter bw = null;
		try {
			out = new FileOutputStream(filePathAndName);
			osr = new OutputStreamWriter(out, "UTF-8");
			bw = new BufferedWriter(osr);
			bw.write(fileContent);  
			bw.flush();
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
		finally{
			if(bw != null){
				bw.close();
			}
			if(osr != null){
				osr.close();
			}
	        if(out != null){
	        	out.close();
	        }
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 * @return Boolean 成功删除返回true遭遇异常返回false
	 * @throws Exception 
	 */
	public boolean delFile(String filePathAndName) throws Exception {
		boolean bea = false;
		try {
			String filePath = filePathAndName;
			File myDelFile = new File(filePath);
			if (myDelFile.exists()) {
				myDelFile.delete();
				bea = true;
			} 
			else{
				bea = true;
			}
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
		return bea;
	}

	/**
	 * 删除文件
	 * 
	 * @param folderPath
	 *            文件夹完整绝对路径
	 * @return
	 * @throws Exception 
	 */
	public void delFolder(String folderPath) throws Exception {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 删除指定文件夹下所有文件
	 * 
	 * @param path
	 *            文件夹完整绝对路径
	 * @return
	 * @return
	 * @throws Exception 
	 */
	public boolean delAllFile(String path) throws Exception {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件
				bea = true;
			}
		}
		return bea;
	}

	public void copyDir(String fromDirPath, String toDirPath) throws Exception {
		File fromDir = new File(fromDirPath);
		if (!fromDir.exists() || !fromDir.isDirectory()) {
			throw new Exception("No directory. Path=" + fromDirPath);
		}
		File toDir = new File(toDirPath);
		if (!toDir.exists() || !toDir.isDirectory()) {
			throw new Exception("No directory. Path=" + toDirPath);
		}

		String[] fromList = fromDir.list();
		if (fromList != null) {
			for (String s : fromList) {
				String tempFromPath = null;
				if (fromDirPath.endsWith(File.separator) || fromDirPath.endsWith("/")) {
					tempFromPath = fromDirPath + s;
				} else {
					tempFromPath = fromDirPath + File.separator + s;
				}

				String tempToPath = null;
				if (toDirPath.endsWith(File.separator) || toDirPath.endsWith("/")) {
					tempToPath = toDirPath + s;
				}
				else {
					tempToPath = toDirPath + File.separator + s;
				}

				File tempFromFile = new File(tempFromPath);
				if (tempFromFile.isFile()) {
					this.copyFile(tempFromPath, tempToPath);
				} else {
					this.createFolder(tempToPath);
					this.copyDir(tempFromPath, tempToPath);
				}
			}
		}
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPathFile
	 *            准备复制的文件源
	 * @param newPathFile
	 *            拷贝到新绝对路径带文件名
	 * @return
	 * @throws Exception 
	 */
	public void copyFile(String oldPathFile, String newPathFile) throws Exception {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			if (oldfile.exists()) { // 文件存在
				inStream = new FileInputStream(oldPathFile); // 读入源文件
				fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节 文件大小
//					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
			else{
				throw new Exception("不存在的文件. Path=" + oldPathFile);
			}
		} 
		catch (Exception ex) {
        	ex.printStackTrace();
			throw ex;
		}
		finally{
			if(inStream != null){
				inStream.close();
			}
			if(fs != null){
				fs.close();
			}
		}
	} 
	/**
	 * 移动文件
	 * 
	 * @param oldPath
	 * @param newPath
	 * @return
	 * @throws Exception 
	 */
	public void moveFile(String oldPath, String newPath) throws Exception {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}
 
	/**
	 * 建立一个可以追加的bufferedwriter
	 * 
	 * @param fileDir
	 * @param fileName
	 * @return
	 * @throws Exception 
	 */
	public BufferedWriter getWriter(String fileDir, String fileName) throws Exception {
		BufferedWriter bw = null;
		try {
			File f1 = new File(fileDir);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			f1 = new File(fileDir, fileName);
			if (!f1.exists()) {
				f1.createNewFile();
			}
			bw = new BufferedWriter(new FileWriter(f1.getPath(), true));
			return bw;
		} 
		catch (Exception ex) {
			if(bw != null){
				bw.close();
			}
        	ex.printStackTrace();
			throw ex;
		} 
	}

	/**
	 * 得到一个bufferedreader
	 * 
	 * @param fileDir
	 * @param fileName
	 * @param encoding
	 * @return
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * @throws IOException 
	 */
	public BufferedReader getReader(String fileDir, String fileName,
			String encoding) throws Exception {
		InputStreamReader read = null;
		BufferedReader br= null;
		try {
			File file = new File(fileDir, fileName);
			read = new InputStreamReader(new FileInputStream(file), encoding);
			br = new BufferedReader(read);
			return br; 
		} 
		catch (Exception ex) { 
			if(br != null) {
				br.close();
			}
			if(read != null){
				read.close();
			}
        	ex.printStackTrace();
			throw ex;
		} 
	} 
	
	public boolean fileExist(String filePath){
		File f = new File(filePath);
		return f.exists();
	}
}
