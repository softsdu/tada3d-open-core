package com.zlp.platform.asynService;

import java.util.HashMap;

//异步调用基类，所有用户自定义的异步任务，都要继承此类
public class AsynInvokeBase implements IAsynInvokeBase{

	//设置参数值，用于将系统自动生成的参数值传递给异步调用类
	@Override
	public void setParameter(HashMap<String, Object> parameterValues) { 
		
	}
	
	//对应的表记录ais_invoke.id字段值
	private String invokeId;
	public final String getInvokeId(){
		return this.invokeId;
	}
	
	@Override
	public final void setInvokeId(String invokeId){
		this.invokeId = invokeId;
	}
	

	//执行异步调用
	@Override
	public InvokeResult run() throws Exception {
		return new InvokeResult(InvokeStatusType.error, "Unimplemented method, method name = run");		
	}

	//检验异步调用是否完成
	@Override
	public InvokeResult check() throws Exception {
		return new InvokeResult(InvokeStatusType.error, "Unimplemented method, method name = check");
	}

	//获取异步调用轮询间隔时间
	@Override
	public int getCheckWaitingSecond() {
		return 30;
	}

	//最长执行时间，超过这个时间，则认为超时出错
	@Override
	public int getMaxRuningSecond() {
		return 120;
	}

}
