package com.on36.haetae.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class JSONPreparedStatementCreator implements PreparedStatementCreator {

	protected String json;

	public JSONPreparedStatementCreator(String json) {
		this.json = json;
	}

	@Override
	public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
		StringBuilder sql = new StringBuilder();
		List<Object> argList = new ArrayList<Object>();
		parser(sql, argList);// 解析JSON为SQL语句
		Object[] args = new Object[argList.size()];
		argList.toArray(args);
		System.out.println(sql.toString());
		PreparedStatement ps = conn.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
		ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
		pss.setValues(ps);
		return ps;
	}

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
		Iterator<Entry<String, Object>> iter = rootSet.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
			String tableName = (String) entry.getKey();
			sql.append("INSERT INTO " + tableName.toUpperCase());
			Object value = entry.getValue();
			StringBuffer cols = new StringBuffer();
			StringBuffer values = new StringBuffer();
			if (value instanceof JSONObject) {
				JSONObject data = (JSONObject) value;
				Set<Entry<String, Object>> dataSet = data.entrySet();
				Iterator<Entry<String, Object>> dataIter = dataSet.iterator();
				while (dataIter.hasNext()) {
					Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) dataIter.next();
					String colName = (String) dataEntry.getKey();
					Object colValue = dataEntry.getValue();
					argList.add(colValue);
					cols.append(colName.toUpperCase() + ",");
					values.append("?,");
				}
			} else if (value instanceof JSONArray) {
				throw new JDBCException("Not support format :" + json);
			}

			cols.deleteCharAt(cols.length() - 1);
			values.deleteCharAt(values.length() - 1);

			sql.append("(");
			sql.append(cols);
			sql.append(") VALUES (");
			sql.append(values);
			sql.append(")");
		}
	}

}
