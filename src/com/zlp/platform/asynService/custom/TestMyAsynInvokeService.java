package com.zlp.platform.asynService.custom;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.HashMap;

import com.zlp.platform.asynService.AsynInvokeBase;
import com.zlp.platform.asynService.InvokeResult;
import com.zlp.platform.asynService.InvokeStatusType;

public class TestMyAsynInvokeService extends AsynInvokeBase{

	@Override
	public void setParameter(HashMap<String, Object> parameterValues) { 
		
	}
	@Override 
	public InvokeResult run()  throws Exception {
		return new InvokeResult(InvokeStatusType.running, "执行中。。。");
	}

	@Override
	public InvokeResult check() throws Exception  {
		File f = new File("e:/testinvoke.txt");
		if(f.exists()){
			return new InvokeResult(InvokeStatusType.succeed, "找到文件了");
		}
		else{
			return new InvokeResult(InvokeStatusType.running, "");
		}
	}

	@Override
	public int getCheckWaitingSecond() { 
		return 10;
	}

	//最长执行时间，超过这个时间，则认为超时出错
	@Override
	public int getMaxRuningSecond() { 
		return 600;
	}
}
