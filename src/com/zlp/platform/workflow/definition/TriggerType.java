package com.zlp.platform.workflow.definition;

//流程节点触发执行的方式
public enum TriggerType {
	//客户端手工调用
	clientManual,
	
	//第三方系统调用
	externalInvoke,
	
	//系统自定执行
	serverAuto
}
