package com.zlp.platform.common;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher; 

public class LicenseGenerator { 
	
	public static String generateLicenseSN(String licenseType, String sysInfo, String publicKey) throws Exception{ 
		String sourceStr = licenseType + ",,," + sysInfo;
		StringBuilder fullSN = new StringBuilder();
		String tempSourceStr = "";
		for(int i = 0; i < sourceStr.length(); i++){
			tempSourceStr += sourceStr.substring(i, i + 1);
			if(i != 0 && i % 20 == 0){
				String sn = LicenseGenerator.encrypt(tempSourceStr, publicKey);
				fullSN.append(sn);
				fullSN.append("#@#");
				tempSourceStr = "";
			}
		}
		if(tempSourceStr.length() != 0){
			String sn = LicenseGenerator.encrypt(tempSourceStr, publicKey);
			fullSN.append(sn);
		}
		
		return fullSN.toString();
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
}