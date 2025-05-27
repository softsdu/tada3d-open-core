package com.zlp.platform.common.exception;

/**
 * 数据库异常.
 * 
 * @version Revision: 1.0
 * 
 */
public class InstallSqlException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InstallSqlException() {
	}

	public InstallSqlException(String message) {
		super(message);
	}

	public InstallSqlException(Throwable cause) {
		super(cause);
	}

	public InstallSqlException(String message, Throwable cause) {
		super(message, cause);
	}

}
