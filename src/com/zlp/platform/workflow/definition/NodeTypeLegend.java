package com.zlp.platform.workflow.definition;

//节点类型
public enum NodeTypeLegend {
	//开始节点
	start,
	
	//结束节点
	end,
	
	//活动节点
	active,
	
	//判断节点
	judge,
	
	//并行节点（暂未实现）
	parallel,
	
	//开始并行节点（暂未实现）
	startParallel,
	
	//完成并行（暂未实现）
	endParallel
}
