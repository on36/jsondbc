package com.on36.haetae.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.utils.DateUtil;
import com.on36.haetae.jdbc.utils.JSONUtils;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class JSONResultSetExtractor implements ResultSetExtractor<String> {

	private static Map<String, JSONObject> assMap;
	private int type = 0;
	private JSONDB db;

	public JSONResultSetExtractor(JSONDB db) {
		this.db = db;
	}

	public JSONResultSetExtractor(JSONDB db, int type) {
		this.db = db;
		this.type = type;
	}

	@Override
	public String extractData(ResultSet rs) throws SQLException, DataAccessException {
		ResultSetMetaData md = rs.getMetaData();
		String tableName = md.getTableName(1);
		int columnCount = md.getColumnCount();
		Object result = null;
		if (this.type == 3) {
			Map<String, Object> kvData = new HashMap<String, Object>();
			while (rs.next()) {
				String colName1 = md.getColumnName(1);
				String colName2 = md.getColumnName(2);
				String key = rs.getString(colName1);
				Object value = rs.getObject(colName2);
				kvData.put(key, value);
			}
			result = kvData;
		} else {
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> rowData = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					String colName = md.getColumnName(i);
					Object value = rs.getObject(colName);
					if (value instanceof Date || value instanceof Timestamp) {
						value = DateUtil.dateToString(rs.getTimestamp(md.getColumnName(i)), DateUtil.DATETIME_PATTERN);
					} else {
						try {
							if (value != null)
								value = JSON.parseObject(value.toString());
						} catch (Exception e) {
							try {
								if (value != null)
									value = JSON.parseArray(value.toString());
							} catch (Exception e1) {
							}
						}
					}
					rowData.put(colName.toLowerCase(), value);
					if (this.type == 0)
						association(tableName, colName, value, rowData);
				}
				data.add(rowData);

				if (this.type == 1) {
					if (data.size() > 0)
						result = data.get(0);
				} else {
					result = data;
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		if (result != null)
			sb.append(JSONUtils.toJson(result));
		else
			sb.append("{}");
		return sb.toString();
	}

	public void association(String tableName, String columnName, Object value, Map<String, Object> rowData) {

		if (tableName != null && columnName != null) {
			String target = tableName + columnName;
			find(target, columnName, value, rowData);
		}

	}

	private void find(String target, String columnName, Object value, Map<String, Object> rowData) {
		JSONObject jo = get(target);
		if (jo != null && value != null) {
			String type = jo.getString("target_result_type");
			String aliases = jo.getString("aliases");
			String targetTableName = jo.getString("target_table_name");
			String targetColumn = jo.getString("target_column_name");
			String targetResult = jo.getString("target_result");
			StringBuilder sb = new StringBuilder("{\"");
			sb.append(targetTableName).append("\":{\"$columns\":").append(targetResult);
			sb.append(",\"").append(targetColumn).append("\":\"").append(value).append("\"}}");

			Object result = null;
			if ("LIST".equals(type.toUpperCase()))
				result = JSON.parse(db.query(sb.toString()));
			else if ("SINGLE".equals(type.toUpperCase()))
				result = JSON.parse(db.get(sb.toString()));
			else if ("VALUE".equals(type.toUpperCase())) {
				JSONObject valueJO = JSON.parseObject(db.get(sb.toString()));
				Iterator<Entry<String, Object>> iter = valueJO.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
					result = entry.getValue();
				}
			} else if ("KV".equals(type.toUpperCase()))
				result = JSON.parse(db.kv(sb.toString()));
			if (aliases != null) {
				rowData.put(aliases, result);
			} else
				rowData.put(columnName, result);
		}
	}

	private JSONObject get(String target) {
		if (assMap == null)
			init();
		return assMap.get(target);
	}

	private void init() {
		assMap = new HashMap<String, JSONObject>();
		JSONArray ja = JSON.parseArray(db.query("{\"h_association\":{}}"));
		for (Object obj : ja) {
			JSONObject entity = (JSONObject) obj;
			String tableName = entity.getString("tabel_name");
			String colName = entity.getString("column_name");
			assMap.put(tableName + colName, entity);
		}
	}
}
