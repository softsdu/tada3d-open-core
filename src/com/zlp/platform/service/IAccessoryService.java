package com.zlp.platform.service; 

public interface IAccessoryService {   
	String getFileCountByFilter();

	String getAccessoryIds();

	String deleteAccessory();

	//保存附件记录ID值到业务表 modified by ls 202203	
	String saveAccessoryIdStrToDB();

	//删除没有关联的附件记录（打删除标记）
	String deleteUnrelatedAccessory();

	//备份所有附件
    String backupAllAccessoryFiles();
}