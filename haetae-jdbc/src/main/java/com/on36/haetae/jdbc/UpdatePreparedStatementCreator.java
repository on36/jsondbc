package com.on36.haetae.jdbc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
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
				sb.insert(0, "," + colName.toUpperCase() + " = ?");
				argList.add(0, value);
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
	protected void fill(JSONObject data) {

	}
}
