package com.zlp.platform.workflow.definition;

//用户可做的操作
public enum OperateType {
	
	//流转，即审批通过
	drive,
	
	//退回
	sendBack,
	
	//取回
	getBack,
	
	//提交，处在开始节点的单据可执行提交
	submit,
	
	//删除
	delete,
	
	//自动流转
	autoDrive
}
