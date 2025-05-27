package com.zlp.platform.control;

import java.io.InputStream;
import java.sql.SQLException;

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.common.NcpSession;
import com.zlp.platform.dao.sys.IAccessoryDao;

public abstract class FileUploadServiceAbstract {

	// 事务管理器
	private HibernateTransactionManager transactionManager;

	private IAccessoryDao asccessoryDao;

	public IAccessoryDao getAsccessoryDao() {
		return asccessoryDao;
	}

	public void setAsccessoryDao(IAccessoryDao asccessoryDao) {
		this.asccessoryDao = asccessoryDao;
	}

	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// 数据库Hibernate Session
	protected Session openDBSession() throws SQLException {
		return this.transactionManager.getSessionFactory().openSession();
	}

	public abstract String saveAccessory(NcpSession session, InputStream inputStream, String firstFileName, String filterType, String filterValue) throws Exception ;

}
