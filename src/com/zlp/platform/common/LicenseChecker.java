package com.zlp.platform.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;

public class LicenseChecker {
	
	private static LicenseType licenseType = LicenseType.unknown;
	
	private static int processCount = 0;
	
	public static void check() throws Exception{
		processCount++;

		//LicenseType licenseType = LicenseChecker.getLicenseType();
		LicenseType licenseType = LicenseType.standard;
		
		switch(licenseType){
			//开发者授权
			case development:{
				if(processCount % 10 == 0){
					throw new Exception("开发版授权提示.");
				}
				break;
			}			
			//一般授权
			case standard:
				break;
			
			//高级授权
			case senior:
				break;
			
			//定制化授权
			case customization:
				break;
			
			//未授权
			case unauthorized: 
				throw new Exception("开发平台未获取授权.");  
			
			//未知的
			case unknown:
				throw new Exception("开发平台授权情况未知.");  
		}
	}
	
	public static LicenseType getLicenseType() throws Exception{
		if(licenseType == LicenseType.unknown){ 
			String licenseSN = LicenseChecker.getLicenseSN(); 
			String licenseTypeStr = LicenseChecker.getLicenseType(licenseSN);
			LicenseChecker.licenseType = LicenseType.valueOf(licenseTypeStr);
		}
		return LicenseChecker.licenseType;
	}

	public static String getCurrentSysInfo() throws Exception{
		Map<String, String> sysInfoDic = LicenseChecker.getSysInfoDic();
		return sysInfoDic.get("projectName")
				+ "#|#" + sysInfoDic.get("companyName")
				+ "#|#" + sysInfoDic.get("cpuId")
				+ "#|#" + sysInfoDic.get("mac")
				+ "#|#" + sysInfoDic.get("mainBoardId");
	}
	public static Map<String, String> getSysInfoDic() throws Exception{
		String projectName = ConfigContext.getConfigMap().get(ZlpState.PLATFORM_PROJECT_NAME);
		String companyName = ConfigContext.getConfigMap().get(ZlpState.PLATFORM_COMPANY_NAME);	
		String cpuId = "";
		String mac = "";
		String mainBoardId = "";
		OSType osType = getOSType();
		switch(osType){
			case linux:{
				cpuId = LicenseChecker.getCPUID_linux();
				mac = LicenseChecker.getMAC_linuxs();
				mainBoardId = LicenseChecker.getMainBoardId_linux();
				break;	
			}
			case windows:
				cpuId = CommonFunction.listToString(LicenseChecker.getCPUID_Windows(), ",");
				mac = CommonFunction.listToString(LicenseChecker.getMAC_windows(), ",");
				mainBoardId = LicenseChecker.getMainBoardId_windows();
				break;
			case macos:
				//Mac OS, 暂时不做处理
				break;
			case unknown:
				//Unknown OS, 暂时不做处理
				break;
		}
		Map<String, String> infoDic = new HashMap<String, String>();
		infoDic.put("projectName", projectName);
		infoDic.put("companyName", companyName);
		infoDic.put("cpuId", cpuId);
		infoDic.put("mac", mac);
		infoDic.put("mainBoardId", mainBoardId);		
		return infoDic;
	} 
	
	public static String getLicenseSN(){
		String licenseSN = ConfigContext.getConfigMap().get(ZlpState.PLATFORM_LICENSE_SN);	
		return licenseSN;
	} 
	
	public static String getLicenseType(String licenseSN) throws Exception{ 
		try{
			String sysInfoInSN = LicenseChecker.decrypt(licenseSN);
			Map<String, String> sysInfoDic = LicenseChecker.getSysInfoDic();
			String projectName = sysInfoDic.get("projectName");
			String companyName = sysInfoDic.get("companyName");
			String cpuId = sysInfoDic.get("cpuId");
			String mac = sysInfoDic.get("mac");
			String mainBoardId = sysInfoDic.get("mainBoardId");
			
			if(sysInfoInSN.startsWith(LicenseType.development.toString())){
				//开发授权，只验证公司名称
				if(sysInfoInSN.indexOf(companyName) >= 0){
					return LicenseType.development.toString();
				}
				else{
					return LicenseType.unauthorized.toString();
				}
			}
			else {
				if(sysInfoInSN.indexOf(projectName) >= 0 
				|| sysInfoInSN.indexOf(companyName) >= 0
				|| sysInfoInSN.indexOf(cpuId) >= 0 
				|| sysInfoInSN.indexOf(mainBoardId) >= 0){
					String[] macParts = mac.split(",");
					boolean macMatched = false;
					for(int i = 0; i < macParts.length; i++){
						if(sysInfoInSN.indexOf(macParts[i]) >= 0){
							macMatched = true;
							break;
						}
					}
					if(macMatched){					
						if(sysInfoInSN.startsWith(LicenseType.customization.toString())){
							return LicenseType.customization.toString();
						}
						else if(sysInfoInSN.startsWith(LicenseType.senior.toString())){
							return LicenseType.senior.toString();
						}
						else if(sysInfoInSN.startsWith(LicenseType.standard.toString())){ 
							return LicenseType.standard.toString();
						}
						else{
							return LicenseType.unauthorized.toString();
						} 
					}
					else{
						return LicenseType.unauthorized.toString();						
					}
				}
				else{
					return LicenseType.unauthorized.toString();
				}
			} 
		}
		catch(Exception ex){
			return "Error License SN. " + ex.getMessage();
		}
	} 
	
	public static String getLicenseSysInfo(String licenseSN) throws Exception{ 
		try{
			String sysInfoInSN = LicenseChecker.decrypt(licenseSN);
			String[] snParts = sysInfoInSN.split(",,,");
			if(snParts.length == 2){
				String licenseSysInfo = snParts[1];
				return licenseSysInfo;
			}
			else{
				return "Error License SN. " + sysInfoInSN;
			}
		}
		catch(Exception ex){
			return "Error License SN. " + ex.getMessage();
		}
	} 
	
	public static Map<String, String> generateKeyPair() throws NoSuchAlgorithmException{
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		
		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
		
		Map<String, String> keyMap = new HashMap<String, String>();
		keyMap.put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		keyMap.put("privateKey", Base64.getEncoder().encodeToString(privateKey.getEncoded()));
		return keyMap;
	}
	
	private static String encrypt( String str, String publicKey ) throws Exception{
		//base64编码的公钥
		byte[] decoded = Base64.getDecoder().decode(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
		//RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	} 
	
	private static String decrypt(String str) throws Exception{
		String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAI369olR2Oyia0KP1fNKFS8tY83uDU41fcnlBlqfFj0XMeXHRmTGLQa53Zen46y0nZIBMGvYwqhRRRiYciWoGH/cz3+4g5CYviPfXz+oHnJmEJER8EGYkxK+3NsvbKO6AVSFPgJ4BAf1AbTcUT9gRYWJ32xXIoiJdEjevnFfVx+BAgMBAAECgYBa7cM+psB6rHptIpkvOt7eW/9zP2YeFHv+9UJgf0pKzbQTKNCYcaMcj6SvH8LcNMBFZaWRLD1eds39toREQaeOJ1zWPlM1aV1MSmY5oZHqh/6ScZ+Nc8GHvPINAr/5sFiEOOHBX/tkw6aCdqaOE5CBkyJf8d9NsxEZtKFX2UoJwQJBAMkTgL4fcj/fdiV3Xf+i/onoD5Ir3cuasAqrVoZf6hJmTszlio8yXL5VdoKvoa+dnryfUaiuSpgXZDEojmIDmo0CQQC0wxw2CI6xXc1rhs4FBg10BjBBUo7dsEU9yGcvAkGR1GzvqpmdpYFakMimn2pxZwv8PWCaNX6z9l8XvlvWWjXFAkB0SD8PtAfSQFyG1j0Z9RzWi8lVcuDVOiPMR8HF+/nVoiy3+ZbjsPBJcgsTEJakhLajxk7nCcRBqGVcuGN+8y6RAkAozepuYBwl+bDHVxTmmWksCcGW3VYWwRXATp7MQ8wdaUB2EaK/rX63vPsuccoLc2GHPSuzY+QIJ9NrfrMofhCpAkBjWzNUwtQud8dDInodoWlJRhybkSE0G1RpESjoH7K05Yhrm8BbyZ4Z7rTM0LJo7Z9V1j/VxSQWyzVQTHY24k8N";
		
		String[] snParts = str.split("#@#");
		
		StringBuilder fullStr = new StringBuilder();
		for(int i = 0; i < snParts.length; i++){
			String snPart = snParts[i];
			if(snPart.length() > 0){				
				//64位解码加密后的字符串
				byte[] inputByte = Base64.getDecoder().decode(snPart); 
				//base64编码的私钥
				byte[] decoded = Base64.getDecoder().decode(privateKey);  
		        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
				//RSA解密
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, priKey); 
				String outStr = new String(cipher.doFinal(inputByte), "utf-8");
				fullStr.append(outStr);				
			}
		}
		return fullStr.toString();
	} 
	
	private static OSType getOSType(){
		String osName = getOSName();
		if(osName.indexOf("linux") >= 0){
			return OSType.linux;
		}
		else if(osName.indexOf("windows") >= 0){
			return OSType.windows;
		}
		else if(osName.indexOf("mac os") >= 0){
			return OSType.macos;
		}
		else{
			return OSType.unknown;
		}
	}
 
	/**
	 * 获取当前操作系统名称
	 */
	private static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}
 
	// 主板序列号 windows
	private static String getMainBoardId_windows() throws Exception {
		String result = "";
		try {
			File file = File.createTempFile("realhowto", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);
 
			String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
					+ "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_BaseBoard\") \n"
					+ "For Each objItem in colItems \n" + "    Wscript.Echo objItem.SerialNumber \n"
					+ "    exit for  ' do the first cpu only! \n" + "Next \n";
 
			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo \"" + file.getPath() + "\"");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;
			}
			input.close();
		} catch (Exception e) {
			throw new Exception("获取主板信息错误", e);
		}
		return result.trim();
	}
 
	// 主板序列号 linux
	private static String getMainBoardId_linux() throws Exception {
 
		String result = "";
		String maniBord_cmd = "dmidecode | grep 'Serial Number' | awk '{print $3}' | tail -1";
		Process p;
		try {
			p = Runtime.getRuntime().exec(new String[] { "sh", "-c", maniBord_cmd });// 管道
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				result += line;
				break;
			}
			br.close();
		} catch (IOException e) {
			throw new Exception("获取主板信息错误", e);
		}
		return result;
	}
  
    /*
     * 获取Linux的mac
     */
	private static String getMAC_linuxs() throws Exception {
	
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try {
			// linux下的命令，一般取eth0作为本地主网卡
			process = Runtime.getRuntime().exec("ifconfig");
			// 显示信息中包含有mac地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				Pattern pat = Pattern.compile("\\b\\w+:\\w+:\\w+:\\w+:\\w+:\\w+\\b");
				Matcher mat= pat.matcher(line);
				if(mat.find()) {
					mac = mat.group(0);
				}
			} 
		} 
		catch (IOException e) {
			throw new Exception("获取mac信息错误", e);
		} 
		finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				throw new Exception("获取mac信息错误", e1);
			}
			bufferedReader = null;
			process = null;
		}
		return mac;
	}
 
 
	/**
	 * 获取widnows网卡的mac地址.
	 * @throws Exception 
	 */
	private static List<String> getMAC_windows() throws Exception {
		InetAddress ip = null;
		NetworkInterface ni = null;
		List<String> macList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
					.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				ni = (NetworkInterface) netInterfaces.nextElement();
				// ----------特定情况，可以考虑用ni.getName判断
				// 遍历所有ip
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ip = (InetAddress) ips.nextElement();
					if (!ip.isLoopbackAddress() // 非127.0.0.1
							&& ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
						macList.add(getMacFromBytes(ni.getHardwareAddress()));
					}
				}
			}
		} catch (Exception e) {
			throw new Exception("获取mac错误", e);
		}
		return macList; 
	}
 
	private static String getMacFromBytes(byte[] bytes) {
		StringBuffer mac = new StringBuffer();
		byte currentByte;
		boolean first = false;
		for (byte b : bytes) {
			if (first) {
				mac.append("-");
			}
			currentByte = (byte) ((b & 240) >> 4);
			mac.append(Integer.toHexString(currentByte));
			currentByte = (byte) (b & 15);
			mac.append(Integer.toHexString(currentByte));
			first = true;
		}
		return mac.toString().toUpperCase(); 
	}
 
	/**
	 * 获取CPU序列号 Windows
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static List<String> getCPUID_Windows() throws Exception {
		List<String> cpus = new ArrayList<String>();
		try {
			File file = File.createTempFile("tmp", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);
			String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
					+ "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_Processor\") \n"
					+ "For Each objItem in colItems \n" + "    Wscript.Echo objItem.ProcessorId \n"
					+ "    exit for  ' do the first cpu only! \n" + "Next \n";
 
			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo \"" + file.getPath() + "\"");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				cpus.add(line.trim());
			}
			input.close();
			file.delete();
		} catch (Exception e) {
			throw new Exception("获取cpu信息错误", e);
		}
		return cpus;
	}
 
	/**
	 * 获取CPU序列号 linux
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static String getCPUID_linux() throws Exception {
		String result = "";
		String CPU_ID_CMD = "dmidecode";
		BufferedReader bufferedReader = null;
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(new String[] { "sh", "-c", CPU_ID_CMD });// 管道
			bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				// 寻找标示字符串[hwaddr]
				index = line.toLowerCase().indexOf("uuid");
				if (index >= 0) {// 找到了
					// 取出mac地址并去除2边空格
					result = line.substring(index + "uuid".length() + 1).trim();
					break;
				}
			}
 
		} catch (IOException e) {
			throw new Exception("获取cpu信息错误", e);
		}
		return result.trim();
	}	
}