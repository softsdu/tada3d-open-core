package com.zlp.platform.dao.system;

import com.zlp.platform.model.sysmodel.Data;


public interface Sys_Data {  
	void generateJsByData(Data data, String jsFilePath) throws Exception ;
}
