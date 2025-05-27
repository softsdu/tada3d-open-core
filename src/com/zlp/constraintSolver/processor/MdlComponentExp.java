package com.zlp.constraintSolver.processor;

public class MdlComponentExp {
	
	public MdlComponentExp(String pim, String js){
		this.pim = pim;
		this.js = js;
	}
	
	private String pim = null;
	public String getPim() {
		return pim;
	}
	public void setPim(String pim) {
		this.pim = pim;
	}
	
	private String js = null;
	public String getJs() {
		return js;
	}
	public void setJs(String js) {
		this.js = js;
	}
	
	private String ps = null;
	public String getPs() {
		return ps;
	}
	public void setPs(String ps) {
		this.ps = ps;
	}
	
}
