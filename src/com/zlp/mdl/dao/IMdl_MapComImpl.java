package com.zlp.mdl.dao;

import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.IDataBaseDao;

//构件关系 add by ls 20230731
public interface IMdl_MapComImpl extends IDataBaseDao {

	void generateMapComFile(INcpSession session) throws Exception;

}
