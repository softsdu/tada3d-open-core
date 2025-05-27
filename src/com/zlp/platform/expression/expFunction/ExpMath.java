package com.zlp.platform.expression.expFunction;

import java.math.BigDecimal; 
 
public class ExpMath {

	public BigDecimal floor(BigDecimal param) {

//
//		double v = Math.floor(param.doubleValue());
//		return BigDecimal.valueOf(v);
		return BigDecimal.valueOf(Math.floor(param.doubleValue()));
	} 
	public BigDecimal ceil(BigDecimal param) {

//		double v = Math.ceil(param.doubleValue());
//		return BigDecimal.valueOf(v);

		return BigDecimal.valueOf(Math.ceil(param.doubleValue()));
	} 
	public BigDecimal round(BigDecimal param) {

//		double v = Math.round(param.doubleValue());
//		return BigDecimal.valueOf(v);

		return BigDecimal.valueOf(Math.round(param.doubleValue()));
	} 
	public BigDecimal sqrt(BigDecimal param) {
		double v = Math.sqrt(param.doubleValue());
		return BigDecimal.valueOf(v);
	}
	public BigDecimal sin(BigDecimal param){
		double value = Math.sin(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal cos(BigDecimal param){
		double value = Math.cos(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal tan(BigDecimal param){
		double value = Math.tan(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal arcsin(BigDecimal param){
		double value = Math.asin(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal arccos(BigDecimal param){
		double value = Math.acos(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal arctan(BigDecimal param){
		double value = Math.atan(param.doubleValue());
		return BigDecimal.valueOf(value);
	}
	public BigDecimal pi(){ 
		return BigDecimal.valueOf(Math.PI);
	}
	
	/*取绝对值 added by ls 20230307*/
	public BigDecimal abs(BigDecimal param1){
		return BigDecimal.valueOf(Math.abs(param1.doubleValue()));
	} 
}
