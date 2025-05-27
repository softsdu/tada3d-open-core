package com.zlp.platform.expression.run;
 
import com.zlp.platform.dao.sys.IDocumentBaseDao;

public interface IDocumentAccess{
	IDocumentBaseDao getDocumentDao(String documentName);
}
