package com.on36.haetae.jdbc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class UpdatePreparedStatementCreator extends ConditionPreparedStatementCreator {

	private String newJson;

	public UpdatePreparedStatementCreator(String json, String newJson) {
		super(json);
		this.newJson = newJson;
	}

	@Override
	protected String dml(String tableName, List<Object> argList) {
		StringBuilder sb = new StringBuilder();
		try {
			JSONObject root = JSON.parseObject(newJson);
			Set<Entry<String, Object>> rootSet = root.entrySet();
			Iterator<Entry<String, Object>> iter = rootSet.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
				String colName = (String) entry.getKey();
				Object value = entry.getValue();
				if (value instanceof JSONObject) {
					JSONObject data = (JSONObject) value;
					Set<Entry<String, Object>> dataSet = data.entrySet();
					Iterator<Entry<String, Object>> dataIter = dataSet.iterator();
					while (dataIter.hasNext()) {
						Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) dataIter.next();
						String tag = (String) dataEntry.getKey();
						Object colValue = dataEntry.getValue();
						if (colValue instanceof JSONObject || colValue instanceof JSONArray)
							throw new IllegalArgumentException();

						switch (tag.toUpperCase()) {
						case "$INC":
							sb.insert(0, "," + colName.toUpperCase() + " = " + colName.toUpperCase() + " + ?");
							argList.add(0, colValue);
							break;
						case "$MULTI":
							sb.insert(0, "," + colName.toUpperCase() + " = " + colName.toUpperCase() + " * ?");
							argList.add(0, colValue);
							break;
						default:
							break;
						}
					}
				} else {
					sb.insert(0, "," + colName.toUpperCase() + " = ?");
					argList.add(0, value);
				}
			}
			sb.replace(0, 1, "SET ");
		} catch (Exception e) {
			throw new JDBCException("Not support format :" + newJson);
		}
		return "UPDATE " + tableName + " " + sb.toString();
	}

	@Override
	protected String tail() {
		return "";
	}

	@Override
	protected void fill(String tag,Object data) {

	}
}
