package com.on36.haetae.jdbc;

import java.util.List;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class DeletePreparedStatementCreator extends ConditionPreparedStatementCreator {

	public DeletePreparedStatementCreator(String json) {
		super(json);
	}

	@Override
	protected String dml(String tableName, List<Object> argList) {
		return "DELETE FROM " + tableName;
	}

	@Override
	protected String tail() {
		return "";
	}

	@Override
	protected void fill(String tag, Object data) {

	}
}
