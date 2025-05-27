package com.zlp.platform.service;
public interface IUserService extends IServiceInterface{
	public String login();
	public String logout();
	public String changePassword();
	public String getMenu();
	public String getSysParam();
}