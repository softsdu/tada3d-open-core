package com.zlp.platform.schedule;

public enum StatusType {
	//等待执行
	waiting,
	
	//正在执行
	running,
	
	//执行出错
	error,
	
	//执行完成
	succeed,
	
	//已删除
	deleted
}
