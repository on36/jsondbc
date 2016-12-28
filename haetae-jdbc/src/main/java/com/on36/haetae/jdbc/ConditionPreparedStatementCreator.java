package com.on36.haetae.jdbc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public abstract class ConditionPreparedStatementCreator extends JSONPreparedStatementCreator {

	public ConditionPreparedStatementCreator(String json) {
		super(json);
	}

	protected abstract String dml(String tableName, List<Object> argList);

	protected abstract String tail();

	protected abstract void fill(JSONObject data);

	@Override
	protected void parser(StringBuilder sql, List<Object> argList) {
		JSONObject root = null;
		try {
			root = JSON.parseObject(json);
		} catch (Exception e) {
			throw new JDBCException("Not support format", e);
		}
		Set<Entry<String, Object>> rootSet = root.entrySet();
		if (rootSet.size() != 1) {
			throw new JDBCException("Not support format :" + json);
		}
		String tableName = null;
		Iterator<Entry<String, Object>> iter = rootSet.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
			tableName = (String) entry.getKey();
			Object value = entry.getValue();
			StringBuffer values = new StringBuffer();
			if (value instanceof JSONObject) {
				JSONObject data = (JSONObject) value;
				Set<Entry<String, Object>> dataSet = data.entrySet();
				Iterator<Entry<String, Object>> dataIter = dataSet.iterator();
				while (dataIter.hasNext()) {
					Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) dataIter.next();
					String colName = (String) dataEntry.getKey();
					Object colValue = dataEntry.getValue();
					if (colValue instanceof JSONObject || colValue instanceof JSONArray) {
						String result = condition(colName.toUpperCase(), colValue, argList);
						values.append(result);
						if (result != null && result.trim().length() > 0)
							values.append(" AND ");
					} else {
						argList.add(colValue);
						values.append(colName.toUpperCase() + " = ? AND ");
					}
				}
				values.delete((values.length() - 5), values.length());
			} else if (value instanceof JSONArray) {
				throw new JDBCException("Not support format :" + json);
			} else {
				values.append("1=1");
			}
			sql.append(" WHERE ");
			sql.append(values);
			sql.append(tail());
			sql.insert(0, dml(tableName.toUpperCase(), argList));
		}
	}

	private String condition(String field, Object object, List<Object> args) {

		StringBuilder sb = new StringBuilder();
		if (object instanceof JSONObject) {
			JSONObject data = (JSONObject) object;
			boolean aggregate = false;
			switch (field) {
			case "$PAGE":
			case "$AGGRE":
				fill(data);
				aggregate = true;
				break;
			default:
				break;
			}
			if (aggregate)
				return "";

			Set<Entry<String, Object>> dataSet = data.entrySet();
			Iterator<Entry<String, Object>> dataIter = dataSet.iterator();
			while (dataIter.hasNext()) {
				Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) dataIter.next();
				String tag = (String) dataEntry.getKey();
				Object colValue = dataEntry.getValue();
				sb.append(field.toUpperCase());
				switch (tag.toUpperCase()) {
				case "$GE":
					sb.append(" >= ?");
					break;
				case "$LE":
					sb.append(" <= ?");
					break;
				case "$GT":
					sb.append(" > ?");
					break;
				case "$LT":
					sb.append(" < ?");
					break;
				case "$EQ":
					sb.append(" = ?");
					break;
				case "$NE":
					sb.append(" <> ?");
					break;
				case "$LIKE":
					sb.append(" LIKE ");
					break;
				case "$IN":
					sb.append(" IN (");
					break;
				case "$NIN":
					sb.append(" NOT IN (");
					break;
				case "$BETWEEN":
					sb.append(" BETWEEN ? AND ?");
					break;
				default:
					sb.append(" = ?");
					break;
				}
				if ("$BETWEEN".equals(tag.toUpperCase()))
					transformBetweenValue(colValue, args);
				else if ("$IN".equals(tag.toUpperCase()) || "$NIN".equals(tag.toUpperCase())) {
					checkIsArray(tag, colValue);
					sb.append(transformValue(colValue)).append(")");
				} else if ("$LIKE".equals(tag.toUpperCase()))
					sb.append("'%").append(transformValue(colValue)).append("%'");
				else
					args.add(transformValue(colValue));
				sb.append(" AND ");
			}
		} else if (object instanceof JSONArray) {
			JSONArray data = (JSONArray) object;
			sb.append("(");
			for (Object v : data) {
				if (v instanceof JSONObject) {
					JSONObject vdata = (JSONObject) v;
					Set<Entry<String, Object>> vdataSet = vdata.entrySet();
					Iterator<Entry<String, Object>> vdataIter = vdataSet.iterator();
					while (vdataIter.hasNext()) {
						Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) vdataIter.next();
						String tag = (String) dataEntry.getKey();
						Object jsonValue = dataEntry.getValue();
						sb.append("(");
						sb.append(condition(tag, jsonValue, args));
						sb.append(")");
					}
				}
				switch (field.toUpperCase()) {
				case "$AND":
					sb.append(" AND ");
					break;
				case "$OR":
					sb.append(" OR  ");
					break;
				default:
					throw new JDBCException(String.format("Not support format  %s: %s", field, data.toJSONString()));
				}
			}
			sb.delete((sb.length() - 5), sb.length());
			sb.append(") AND ");
		} else {
			sb.append(field.toUpperCase()).append(" = ? AND ");
			args.add(object);
		}
		sb.delete((sb.length() - 5), sb.length());
		return sb.toString();
	}

	private Object transformValue(Object value) {
		if (value instanceof JSONArray) {
			StringBuilder sb = new StringBuilder();
			JSONArray arrayValue = (JSONArray) value;
			for (Object v : arrayValue) {
				if (v instanceof String)
					sb.append("'").append(v).append("',");
				else
					sb.append(v).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
		return value;
	}

	private void checkIsArray(String tag, Object value) {
		if (value instanceof JSONArray) {
		} else
			throw new JDBCException(
					String.format("Not support format %s : %s, value should be array type", tag, value.toString()));
	}

	private void transformBetweenValue(Object value, List<Object> args) {
		if (value instanceof JSONArray) {
			JSONArray arrayValue = (JSONArray) value;
			if (arrayValue.size() == 2) {
				for (Object v : arrayValue)
					args.add(v);
			} else
				throw new JDBCException(
						String.format("Not support format $between : %s, the size of value array shoud be 2",
								arrayValue.toJSONString()));
		} else
			throw new JDBCException(String.format("Not support format $between : %s, value should be array type", value.toString()));
	}
}
