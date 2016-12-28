package com.on36.haetae.jdbc.exception;

/**
 * @author zhanghr
 * @date 2016年12月23日
 */
public class JDBCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3117826583268579034L;

	public JDBCException(String message) {
		super(message);
	}

	public JDBCException(String message, Throwable cause) {
		super(message, cause);
	}

}
