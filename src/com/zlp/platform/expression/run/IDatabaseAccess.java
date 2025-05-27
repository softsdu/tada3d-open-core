package com.zlp.platform.expression.run;

import org.hibernate.Session;

import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ISequenceGenerator;

public interface IDatabaseAccess {
	Session getSession(); 
	
	void openSession(); 
	
	Session newSession(); 
	
	void setSession(Session session); 
	
	IDBParserAccess getDBParserAccess(); 
	
	ISequenceGenerator getSequenceGenerator(); 
}
