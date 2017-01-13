package com.on36.haetae.jdbc;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
public class QueryPreparedStatementCreator extends ConditionPreparedStatementCreator {

	private String[] columns;
	private String[] joinColumns;
	protected String joinCondition;
	protected String orderBy;

	public QueryPreparedStatementCreator(String json) {
		super(json);
		this.columns = null;
		this.joinColumns = null;
		this.joinCondition = null;
		this.orderBy = null;
	}

	@Override
	protected String dml(String tableName, List<Object> argList) {
		if (this.columns == null || this.columns.length == 0) {
			if ((this.joinColumns != null && this.joinColumns.length > 0) && joinCondition != null)
				return "SELECT A.*," + String.join(",", this.joinColumns).toUpperCase() + " FROM " + tableName + " A "
						+ joinCondition;
			else if (joinCondition != null)
				return "SELECT A.* FROM " + tableName + " A " + joinCondition;
			else
				return "SELECT * FROM " + tableName;
		} else {
			if ((this.joinColumns != null && this.joinColumns.length > 0) && joinCondition != null)
				return "SELECT " + String.join(",", this.columns).toUpperCase() + ","
						+ String.join(",", this.joinColumns).toUpperCase() + " FROM " + tableName + " A "
						+ joinCondition;
			else if (joinCondition != null)
				return "SELECT " + String.join(",", this.columns).toUpperCase() + " FROM " + tableName + " A "
						+ joinCondition;
			else
				return "SELECT " + String.join(",", this.columns).toUpperCase() + " FROM " + tableName;
		}
	}

	@Override
	protected String tail() {
		if (this.orderBy == null)
			return " LIMIT 100";
		else
			return String.format(" %s LIMIT 100", this.orderBy);
	}

	@Override
	protected void fill(String tag, Object object) {
		if (object == null)
			return;
		if (object instanceof JSONArray) {
			switch (tag) {
			case "$COLUMNS":
				JSONArray data = (JSONArray) object;
				this.columns = new String[data.size()];
				data.toArray(this.columns);
				break;
			case "$PAGE":
				throw new JDBCException(String.format("Please change to page method for %s", tag));
			case "$AGGRE":
				throw new JDBCException(String.format("Please change to aggregate method for %s", tag));
			default:
				throw new JDBCException(String.format("Not support format %s : %s", tag, object));
			}

		} else if (object instanceof JSONObject) {
			switch (tag) {
			case "$JOIN":
				join(object);
				break;
			case "$SORT":
				orderBy(object);
				break;
			case "$AGGRE":
				throw new JDBCException(String.format("Please change to aggregate method for %s", tag));
			default:
				break;
			}
		}
	}

	private void orderBy(Object object) {
		StringBuilder sb = new StringBuilder("ORDER BY ");
		LinkedHashMap<String, String> result = JSON.parseObject(object.toString(),
				new TypeReference<LinkedHashMap<String, String>>() {
				});
		for (Map.Entry<String, String> entry : result.entrySet()) {
			String colName = entry.getKey().toUpperCase();
			String sort = entry.getValue().toUpperCase();
			sb.append(colName).append(" ").append(sort).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		this.orderBy = sb.toString();
	}

	private void join(Object object) {
		JSONObject data = (JSONObject) object;
		Set<Entry<String, Object>> rootSet = data.entrySet();
		if (rootSet.size() == 1) {
			Iterator<Entry<String, Object>> iter = rootSet.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter.next();
				String joinTableName = (String) entry.getKey();
				Object value = entry.getValue();
				StringBuilder sb = new StringBuilder("INNER JOIN " + joinTableName.toUpperCase() + " B ON ");
				if (value instanceof JSONObject) {
					JSONObject vdata = (JSONObject) value;
					Set<Entry<String, Object>> vdataSet = vdata.entrySet();
					Iterator<Entry<String, Object>> vdataIter = vdataSet.iterator();
					while (vdataIter.hasNext()) {
						Map.Entry<String, Object> dataEntry = (Map.Entry<String, Object>) vdataIter.next();
						String ctag = (String) dataEntry.getKey().toUpperCase();
						Object jsonValue = dataEntry.getValue();
						switch (ctag) {
						case "$COLUMNS":
							if (jsonValue instanceof JSONArray) {
								JSONArray jcolumns = (JSONArray) jsonValue;
								joinColumns = new String[jcolumns.size()];
								jcolumns.toArray(joinColumns);
							} else
								throw new JDBCException(String.format("Not support format $COLUMNS : %s", jsonValue));
							break;
						case "$REFER":
							if (jsonValue instanceof JSONObject) {
								JSONObject refData = (JSONObject) jsonValue;
								Set<Entry<String, Object>> refdataSet = refData.entrySet();
								Iterator<Entry<String, Object>> refdataIter = refdataSet.iterator();
								while (refdataIter.hasNext()) {
									Map.Entry<String, Object> refdataEntry = (Map.Entry<String, Object>) refdataIter
											.next();
									String refColumn = (String) refdataEntry.getKey().toUpperCase();
									Object mainColumn = refdataEntry.getValue();
									sb.append("B." + refColumn);
									sb.append(" = A.");
									sb.append(mainColumn.toString().toUpperCase());
									sb.append(" AND ");
								}
							} else
								throw new JDBCException(String.format("Not support format $REF : %s", jsonValue));
							break;
						default:
							sb.append("B." + ctag);
							sb.append(" = ");
							sb.append((jsonValue instanceof String) ? String.format("'%s'", jsonValue.toString().toUpperCase())
									: jsonValue.toString());
							sb.append(" AND ");
							break;
						}
					}
					sb.delete((sb.length() - 5), sb.length());
					joinCondition = sb.toString();
				}
			}
		}
	}
}
