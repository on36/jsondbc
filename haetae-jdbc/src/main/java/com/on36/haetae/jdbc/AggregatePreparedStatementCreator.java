package com.on36.haetae.jdbc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月27日
 */
public class AggregatePreparedStatementCreator extends QueryPreparedStatementCreator {

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
			throw new JDBCException(String.format("No support aggregate method %s", this.aggregate));
		}
		sb.append(" FROM ").append(tableName);
		return sb.toString();
	}

	@Override
	protected String tail() {
		return "";
	}

	@Override
	protected void fill(String tag, Object object) {
		if (object == null)
			return;

		if (object instanceof JSONObject) {
			switch (tag) {
			case "$AGGRE":
				JSONObject data = (JSONObject) object;
				Set<Entry<String, Object>> dataSet = data.entrySet();
				Iterator<Entry<String, Object>> dataIter = dataSet.iterator();
				while (dataIter.hasNext()) {
					Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) dataIter.next();
					this.colName = (String) dataEntry.getKey().toUpperCase();
					this.aggregate = dataEntry.getValue().toString();
				}
				return;
			default:
				break;
			}
		}

		super.fill(tag, object);
	}

}
