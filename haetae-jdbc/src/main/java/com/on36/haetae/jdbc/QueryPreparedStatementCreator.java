package com.on36.haetae.jdbc;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class QueryPreparedStatementCreator
		extends ConditionPreparedStatementCreator {

	private String[] columns;

	public QueryPreparedStatementCreator(String json) {
		super(json);
	}

	public QueryPreparedStatementCreator(String json, String... columns) {
		super(json);
		this.columns = columns;
	}

	@Override
	protected String dml(String tableName,List<Object> argList) {
		if (this.columns == null || this.columns.length == 0)
			return "SELECT * FROM " + tableName;
		else {
			return "SELECT " + String.join(",", this.columns).toUpperCase()
					+ " FROM " + tableName;
		}
	}

	@Override
	protected String tail() {
		return " LIMIT 100";
	}

	@Override
	protected void fill(JSONObject data) {

	}
}
