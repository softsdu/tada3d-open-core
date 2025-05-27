package com.zlp.platform.workflow.definition;

//单据在某个节点中的状态
public enum StepStatusType {
	//已被挂起
	suspended,
	
	//活动
	active,
	
	//已执行通过
	passed,
	
	//不可用，历史记录作废
	disabled,
	
	//正在执行异步操作
	asynProcessing,
	
	//异步操作成功
	asynSucceed,
	
	//异步操作失败
	asynError
}
