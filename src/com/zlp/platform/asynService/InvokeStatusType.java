package com.zlp.platform.asynService;

//异步调用状态
public enum InvokeStatusType {
	//等待执行
	waiting,
	
	//正在执行
	running,

	//执行超时
	timeout,
	
	//执行成功
	succeed,
	
	//执行出错
	error,
	
	//已删除
	deleted
}
