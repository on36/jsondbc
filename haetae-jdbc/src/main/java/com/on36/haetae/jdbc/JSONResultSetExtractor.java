package com.on36.haetae.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.on36.haetae.jdbc.utils.DateUtil;
import com.on36.haetae.jdbc.utils.JSONUtils;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class JSONResultSetExtractor implements ResultSetExtractor<String> {

	private boolean unique;

	public JSONResultSetExtractor() {
	}

	public JSONResultSetExtractor(boolean unique) {
		this.unique = unique;
	}

	@Override
	public String extractData(ResultSet rs)
			throws SQLException, DataAccessException {
		ResultSetMetaData md = rs.getMetaData();
		String tableName = md.getTableName(1);
		int columnCount = md.getColumnCount();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		while (rs.next()) {
			Map<String, Object> rowData = new HashMap<String, Object>();
			for (int i = 1; i <= columnCount; i++) {
				String colName = md.getColumnName(i);
				Object value = rs.getObject(colName);
				if (value instanceof Date || value instanceof Timestamp) {
					value = DateUtil.dateToString(
							rs.getTimestamp(md.getColumnName(i)),
							DateUtil.DATETIME_PATTERN);
				}
				rowData.put(colName.toLowerCase(), value);
			}
			data.add(rowData);
		}
		StringBuilder sb = new StringBuilder();
		if (tableName != null && tableName.trim().length() > 0) {
			sb.append("{\"" + tableName + "\":");
			if (this.unique) {
				if (data.size() > 0)
					sb.append(JSONUtils.toJson(data.get(0)));
				else
					sb.append("{}");
			} else
				sb.append(JSONUtils.toJson(data));
			sb.append("}");
		} else {
			if (this.unique) {
				if (data.size() > 0)
					sb.append(JSONUtils.toJson(data.get(0)));
				else
					sb.append("{}");
			} else
				sb.append(JSONUtils.toJson(data));
		}
		return sb.toString();
	}

}
