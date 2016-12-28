package com.on36.haetae.jdbc;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月27日
 */
public class AggregatePreparedStatementCreator extends ConditionPreparedStatementCreator {

	private String aggregate;
	private String colName;

	public AggregatePreparedStatementCreator(String json) {
		super(json);
	}

	@Override
	protected String dml(String tableName, List<Object> argList) {

		if (this.aggregate == null)
			throw new JDBCException("Aggregate method should not be null");
		if (this.colName == null)
			throw new JDBCException(String.format("Aggregate method %s need one column", this.aggregate));

		StringBuilder sb = new StringBuilder("SELECT ");
		switch (aggregate.toUpperCase()) {
		case "COUNT":
			sb.append("COUNT(").append(colName).append(") COUNT");
			break;
		case "SUM":
			sb.append("SUM(").append(colName).append(") SUM");
			break;
		case "AVG":
			sb.append("AVG(").append(colName).append(") AVG");
			break;
		default:
			sb.append("COUNT(*) COUNT");
			break;
		}
		sb.append(" FROM ").append(tableName);
		return sb.toString();
	}

	@Override
	protected String tail() {
		return "";
	}

	@Override
	protected void fill(JSONObject data) {
		if (data == null)
			return;

		this.aggregate = data.getString("aggregate");
		this.colName = data.getString("aggregate");
	}

}
