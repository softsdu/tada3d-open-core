package com.zlp.mdl.dao;

import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.IDataBaseDao;

//构件参数分类 add by ls 20230731
public interface IMdl_PropertyCategoryImpl extends IDataBaseDao {

	void generateCategoryFile(INcpSession session) throws Exception;

}
