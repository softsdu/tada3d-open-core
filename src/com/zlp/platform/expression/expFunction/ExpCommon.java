package com.zlp.platform.expression.expFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

/**
 * 执行四则运算
 * 
 * @author bsh
 * 
 */
// 重新整理了此类，允许抛出异常 modified by ls 20170911
public class ExpCommon {

	//修改为public modified by liyh 20180719
	public Boolean checkIsDecimal(String param) {	//private Boolean checkIsDecimal(String param) {
		
		//修改判断逻辑 modified by liyh 20180720
		
		/*修改前逻辑
		 * if (param == null || param.length() == 0) {
			return true;
		} else {
			param = param.replace("\r", "");
			if (param.length() == 0) {
				return true;
			} else {
				 return ValueConverter.CheckDecimal(param);
			}
		}*/
		
		/*修改后逻辑*/
		param = param == null? "" : param.trim();
		if (param == null || param.length() == 0) {
			return true;
		} else {
			param = param.replace("\r", "");
			param = param.replace(",", "");
			if (param.length() == 0) {
				return true;
			} else {
				 try{
				    BigDecimal  devimalValue = new BigDecimal(param);
				   	return true;
				  }catch(NumberFormatException e)
				  {
					  System.out.println("异常：\"" + param + "\"不是数字...");
					  return false;
				  }
			}
		}
		
	}

	//modified by ls 20180404 更改函数为public
	public BigDecimal toBigDecimal(String param) {

		param = param == null? "" : param.trim();
		if (param == null || param.length() == 0) {
			return new BigDecimal(0);
		} else if (param.trim().equals("-")) {
			return new BigDecimal(0);
		} else {
			param = param.replace("\r", "");
			param = param.replace(",", "");
			if (param.length() == 0) {
				return new BigDecimal(0);
			} else {				
				return new BigDecimal(param.trim());
			}
		}

	}

	/***  屏蔽原有add函数   deleted by liyh 20190213  start  ***/
	
	/**
	 * 加法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 * @throws Exception
	 */
	/*public String add(String param1, String param2) throws Exception {
		String calResult = "";
		try {
			// add的参数为string时，变换成decimal前先trim modified by ls 20170908
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			calResult = add(bigDecimal1, bigDecimal2).toString();
		} catch (Exception e) {
			String errorInfo = "无法将 " + param1 + " 与 " + param2 + " 相加";
			e.printStackTrace();
			throw new Exception(errorInfo);
		}
		return calResult;
	}*/

	/**
	 * 加法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	/*public String add(String param1, BigDecimal param2) {
		String calResult = "";
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			calResult = add(bigDecimal1, param2).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return calResult;
	}*/

	/**
	 * 加法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	/*public BigDecimal add(BigDecimal param1, BigDecimal param2) {
		if (param1 == null) {
			if (param2 == null) {
				return new BigDecimal("0");
			} else {
				return param2;
			}
		} else if (param2 == null) {
			return param1;
		} else {
			return param1.add(param2);
		}
	}*/

	/**
	 * 加法
	 * 
	 * @param param1
	 * @return
	 */
	/*public BigDecimal add(BigDecimal param1) {
		return param1;
	}*/
	
	/***  屏蔽原有add函数   deleted by liyh 20190213  end  ***/
	
	
	/***  修正add函数错误的问题   added by liyh 20190213  start  ***/
	//连接
	public String andString(Object param1, Object param2){
		String p1 = param1 == null ? "" : param1.toString();
		String p2 = param1 == null ? "" : param2.toString(); 
		return p1 + p2;
	}
	//加法
	public BigDecimal add(BigDecimal param1, BigDecimal param2){
		if(param1 == null){
			return param2;
		}
		else if(param2 == null){
			return param1;
		}
		else{
			return param1.add(param2);
		}
	}
	//加法
	public String add(String param1, String param2){
		String p1 = param1 == null ? "" : param1.toString();
		//修改代码错误 modified by ls 20230420
		String p2 = param2 == null ? "" : param2.toString(); 
		return p1 + p2;
	}
	
	//正数
	public BigDecimal add(BigDecimal param1){
		return param1;
	}
	/***    added by liyh 20190213   end    ***/
	
	
	
	

	/**
	 * 减法
	 * 
	 * @param param1
	 * @return
	 */
	public BigDecimal subtract(BigDecimal param1) {
		return BigDecimal.ZERO.subtract(param1);
	}

	/**
	 * 减法
	 * 
	 * @param param1
	 * @return
	 */
	public BigDecimal subtract(String param1) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			return BigDecimal.ZERO.subtract(bigDecimal1);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 减法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public BigDecimal subtract(BigDecimal param1, BigDecimal param2) {
		if (param1 == null) {
			return subtract(param1);
		} else {
			return param1.subtract(param2);
		}
	}

	/**
	 * 减法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public String subtract(String param1, String param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return subtract(bigDecimal1, bigDecimal2).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * 乘法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public BigDecimal multiply(BigDecimal param1, BigDecimal param2) {
		if (null == param1 || null == param2) {
			return new BigDecimal("0");
		}

		//去掉小数点后的空余0：比如 20.04000000 或者  20.0  ==> 20.04 和 20  modified by liyh 20220330
		//return param1.multiply(param2);
		return BigDecimal.valueOf(Double.parseDouble(param1.multiply(param2).stripTrailingZeros().toPlainString()));
	}

	/**
	 * 乘法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public String multiply(String param1, String param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal = toBigDecimal(param2);
			return bigDecimal.multiply(bigDecimal1).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 乘法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public String multiply(String param1, BigDecimal param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			return param2 == null ? "0" : param2.multiply(bigDecimal1).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 除法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public BigDecimal divide(BigDecimal param1, BigDecimal param2) throws Exception {
		if (null == param2 || param2.compareTo(new BigDecimal("0")) == 0) {
			if (param1.compareTo(new BigDecimal("0")) != 0) {
				// 除数为空，那么返回0
				return new BigDecimal("0");
				// throw new Exception("除数不能为0!");
			} else {
				return new BigDecimal("0");
			}
		}
		if (null == param1) {
			return new BigDecimal("0");
		}

		//去掉小数点后的空余0：比如 20.04000000 或者  20.0  ==> 20.04 和 20  modified by liyh 20220330
		//return param1.divide(param2, 10, BigDecimal.ROUND_HALF_UP);
		return BigDecimal.valueOf(Double.parseDouble(param1.divide(param2, 10, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()));
	}

	/**
	 * 除法
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 * @throws Exception
	 */
	public String divide(String param1, String param2) throws Exception {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return divide(bigDecimal1, bigDecimal2).toString();
		} catch (Exception e) {
			throw e;
		}
	}

	//取余  added by ls 20210827
	public BigDecimal remaind(BigDecimal param1, BigDecimal param2) throws Exception {
		if (null == param2 || param2.compareTo(new BigDecimal("0")) == 0) {
			if (param1.compareTo(new BigDecimal("0")) != 0) {
				// 除数为空，那么返回0
				return new BigDecimal("0");
				// throw new Exception("除数不能为0!");
			} else {
				return new BigDecimal("0");
			}
		}
		if (null == param1) {
			return new BigDecimal("0");
		}
		return param1.remainder(param2);
	}

	//判断相当
	//以前的方法存在0!=0.0的情况，修改之 modified by ls 20230525
	public Boolean equal(BigDecimal param1, BigDecimal param2) {
		if (param1 == null) {
			return param2 == null;
		} 
		else if(param2 == null){
			return param1 == null;
		}
		else {
			return param1.doubleValue() == param2.doubleValue();
		}
	}

	/**
	 * 判断相等
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean equal(String param1, String param2) {
		try {
			if (checkIsDecimal(param1) && checkIsDecimal(param1)) {
				BigDecimal bigDecimal1 = toBigDecimal(param1);
				BigDecimal bigDecimal2 = toBigDecimal(param2);
				return equal(bigDecimal1, bigDecimal2);
			} else {
				if (param1 == null) {
					return param2 == null;
				} else {
					return param1.equals(param2);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 大于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean moreThan(BigDecimal param1, BigDecimal param2) {
		if (param1 == null || param2 == null) {
			return false;
		} else {
			param1 = param1.setScale(20, BigDecimal.ROUND_HALF_UP);
			param2 = param2.setScale(20, BigDecimal.ROUND_HALF_UP);
			return param1.compareTo(param2) == 1;
		}
	}

	/**
	 * 大于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean moreThan(String param1, String param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return moreThan(bigDecimal1, bigDecimal2);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 小于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean lessThan(BigDecimal param1, BigDecimal param2) {
		if (param1 == null || param2 == null) {
			return false;
		} else {
			param1 = param1.setScale(20, BigDecimal.ROUND_HALF_UP);
			param2 = param2.setScale(20, BigDecimal.ROUND_HALF_UP);
			return param1.compareTo(param2) == -1;
		}
	}

	/**
	 * 小于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean lessThan(String param1, String param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return lessThan(bigDecimal1, bigDecimal2);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 大于等于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean moreThanOrEqual(BigDecimal param1, BigDecimal param2) {
		if (param1 == null || param2 == null) {
			return false;
		} else {
			param1 = param1.setScale(20, BigDecimal.ROUND_HALF_UP);
			param2 = param2.setScale(20, BigDecimal.ROUND_HALF_UP);
			return param1.compareTo(param2) >= 0;
		}
	}

	/**
	 * 大于等于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean moreThanOrEqual(String param1, String param2) {
		try {
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return moreThanOrEqual(bigDecimal1, bigDecimal2);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 小于等于
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	public Boolean lessThanOrEqual(BigDecimal param1, BigDecimal param2) {
		if (param1 == null || param2 == null) {
			return false;
		} else {
			return param1.compareTo(param2) <= 0;
		}
	}

	public Boolean lessThanOrEqual(String param1, String param2) {
		try {
			// 参数为string时，变换成decimal前先trim modified by ls 20170908
			BigDecimal bigDecimal1 = toBigDecimal(param1);
			BigDecimal bigDecimal2 = toBigDecimal(param2);
			return lessThanOrEqual(bigDecimal1, bigDecimal2);
		} catch (Exception e) {
			throw e;
		}
	}

	// Json转字符串
	public String jsonToString(JSONObject json) {
		String s = JSONProcessor.jsonToStr(json);
		return s;
	}

	// 字符串转Json
	public JSONObject stringToJson(String str) throws Exception {
		JSONObject obj = JSONProcessor.strToJSON(str);
		return obj;
	}

	// IIF判断
	public String iif(Boolean check, String returnValue1, String returnValue2) {
		return check ? returnValue1 : returnValue2;
	}

	// IIF判断
	public Date iif(Boolean check, Date returnValue1, Date returnValue2) {
		return check ? returnValue1 : returnValue2;
	}

	// IIF判断
	public BigDecimal iif(Boolean check, BigDecimal returnValue1, BigDecimal returnValue2) {
		return check ? returnValue1 : returnValue2;
	}

	// IIF判断
	public JSONObject iif(Boolean check, JSONObject returnValue1, JSONObject returnValue2) {
		return check ? returnValue1 : returnValue2;
	}

	// and
	public Boolean and(Boolean param1, Boolean param2) {
		return param1 && param2;
	}

	// or
	public Boolean or(Boolean param1, Boolean param2) {
		return param1 || param2;
	}
	
	public String toString(BigDecimal param1){

		//去掉小数点后的空余0：比如 20.04000000 或者  20.0  ==> 20.04 和 20  modified by liyh 20230425
		//return param1 == null ? "" : param1.toString();
		return param1 == null ? "" : param1.stripTrailingZeros().toPlainString();

	} 
	
	public BigDecimal strToDecimal(String param1){
		return new BigDecimal(Double.parseDouble(param1));
	}
	
	public Boolean strToBoolean(String param1) throws Exception{
		return ValueConverter.convertToBoolean(param1, "Y", "N");
	}
	
	public Date strToDate(String param1) throws Exception{
		return ValueConverter.convertToDate(param1, SysConfig.getDateFormat());
	}
	
	public Date strToTime(String param1) throws Exception{
		return ValueConverter.convertToDate(param1, SysConfig.getTimeFormat());
	}
	
	/*获得是否为空 added by ls 20220524*/
	public boolean isEmpty(String param1){
		return param1 == null || param1.length() == 0;
	} 
	public boolean isEmpty(BigDecimal param1){
		return param1 == null;
	} 
	public boolean isEmpty(Date param1){
		return param1 == null;
	} 
	public boolean isEmpty(Boolean param1){
		return param1 == null;
	} 
	
	/*获得字符串长度 added by ls 20220524*/
	public BigDecimal len(String param1){
		int len = param1 == null ? 0 : param1.length();
		return BigDecimal.valueOf(len);
	} 
	
	/*Trim added by ls 20220524*/
	public String trim(String param1){
		String v = param1 == null ? "" : param1.trim();
		return v;
	}

	/*获取一维数组指定索引位置的值 added by liyh 20230303*/
	public String getArrayData(String arrayStr,BigDecimal index){

		try {

			//added by liyh 20230420
			if(StringUtils.isEmpty(arrayStr)){
				return "";
			}

			String[] arrayList = arrayStr.split(",");

			//修改逻辑：超出索引返回空字符串，不抛出异常；（因 iif(1>2,getArrayData("1,2",3),"A") 执行校验报错，修正 getArrayData 的错误提示） modified by liyh 20230414
			//return arrayList[index.intValue()];
			if(arrayList.length>index.intValue()){
				return arrayList[index.intValue()];
			}else{
				return "";
			}

		}catch (Exception e) {
			throw e;
		}
	}

	/*获取二维数组指定索引位置的值 added by liyh 20230303*/
	public String getArrayData_TwoDim(String arrayStr,BigDecimal index1,BigDecimal index2){
		try {

			//added by liyh 20230420
			if(StringUtils.isEmpty(arrayStr)){
				return "";
			}

			String[] arrayList1 = arrayStr.split(";");

			//修改逻辑：超出索引返回空字符串，不抛出异常；（因 iif(1>2,getArrayData("1,2",3),"A") 执行校验报错，修正 getArrayData 的错误提示） modified by liyh 20230414
//			String[] arrayList = arrayList1[index1.intValue()].split(",");
//			return arrayList[index2.intValue()];
			if(arrayList1.length>index1.intValue()){
				String[] arrayList = arrayList1[index1.intValue()].split(",");
				if(arrayList.length>index2.intValue()){
					return arrayList[index2.intValue()];
				}else{
					return "";
				}

			}else{
				return "";
			}


		}catch (Exception e) {
			throw e;
		}
	}

	/*获取最大值 added by liyh 20230807*/
	public BigDecimal max(String arrayStr){
		BigDecimal result=BigDecimal.ZERO;
		try {
			if(!StringUtils.isEmpty(arrayStr)){
				String[] numberList = arrayStr.split(",");
				if(numberList.length>0) {
					result = BigDecimal.valueOf(Double.parseDouble(numberList[0]));
				}
				for(int i=0;i<numberList.length;i++) {
					BigDecimal number = BigDecimal.valueOf(Double.parseDouble(numberList[i]));
					if (number.compareTo(result)>0) {
						result = number;
					}
				}
			}

			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	/*获取最小值 added by liyh 20230807*/
	public BigDecimal min(String arrayStr) {
		BigDecimal result = BigDecimal.ZERO;
		try {
			if (!StringUtils.isEmpty(arrayStr)) {
				String[] numberList = arrayStr.split(",");
				if (numberList.length > 0) {
					result = BigDecimal.valueOf(Double.parseDouble(numberList[0]));
				}
				for (int i = 0; i < numberList.length; i++) {
					BigDecimal number = BigDecimal.valueOf(Double.parseDouble(numberList[i]));
					if (number.compareTo(result) < 0) {
						result = number;
					}
				}
			}

			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	/*获取二维数组的行数量 added by liyh 20240104*/
	public BigDecimal getMatrixRowsNumber(String arrayStr){
		try {
			if(StringUtils.isEmpty(arrayStr)){
				return BigDecimal.ZERO;
			}

			String[] arrayList = arrayStr.split(";");
			return  BigDecimal.valueOf(arrayList.length).stripTrailingZeros();
		}catch (Exception e) {
			throw e;
		}
	}

}
