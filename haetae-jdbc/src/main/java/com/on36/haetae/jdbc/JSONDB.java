package com.on36.haetae.jdbc;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */

@Service
public class JSONDB extends AbstractDB {
	/**
	 * 分页查询,无分页信息时只取前100条数据.
	 * 
	 * @param json
	 *            查询的JSON数据
	 * @param columns
	 *            只查询指定字段
	 * @return
	 */
	public String page(String json) {
		return getJdbcTemplate().query(new PageQueryPreparedStatementCreator(json), new JSONResultSetExtractor());
	}

	/**
	 * 查询操作 ，无法分页，默认最大只能取100条数据.
	 * 
	 * @param json
	 *            查询的JSON数据
	 * @param columns
	 *            只查询指定字段
	 * @return
	 */
	public String query(String json) {
		return getJdbcTemplate().query(new QueryPreparedStatementCreator(json), new JSONResultSetExtractor());
	}

	/**
	 * 单一查询操作 ，只返回第一行数据.
	 * 
	 * @param json
	 *            查询的JSON数据
	 * @param columns
	 *            只查询指定字段
	 * @return
	 */
	public String get(String json) {
		return getJdbcTemplate().query(new QueryPreparedStatementCreator(json), new JSONResultSetExtractor(true));
	}

	/**
	 * 聚合查询操作.
	 * 
	 * @param json
	 *            查询的JSON数据
	 * @return
	 */
	public String aggregate(String json) {
		return getJdbcTemplate().query(new AggregatePreparedStatementCreator(json), new JSONResultSetExtractor(true));
	}

	/**
	 * 添加操作.
	 * 
	 * @param json
	 *            添加的JSON数据
	 * @return 返回当前影响的行数
	 */
	public int insert(String json) {
		return insert(json, false);
	}

	/**
	 * 添加操作.
	 * 
	 * @param json
	 *            添加的JSON数据
	 * @param autoIncrement
	 *            主键是否自增长
	 * @return 当autoIncrement为false，返回当前影响的行数;当autoIncrement为true，返回主键值
	 */
	public int insert(String json, boolean autoIncrement) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int update = getJdbcTemplate().update(new JSONPreparedStatementCreator(json), keyHolder);
		if (autoIncrement)
			return keyHolder.getKey().intValue();
		else
			return update;
	}

	/**
	 * 删除操作.
	 * 
	 * @param json
	 *            删除的JSON数据
	 * @return 返回当前影响的行数
	 */
	public int remove(String json) {
		return getJdbcTemplate().update(new DeletePreparedStatementCreator(json));
	}

	/**
	 * 修改操作.
	 * 
	 * @param json
	 *            条件的JSON数据
	 * @param newJson
	 *            修改的JSON数据
	 * @return 返回当前影响的行数
	 */
	public int update(String json, String newJson) {
		return getJdbcTemplate().update(new UpdatePreparedStatementCreator(json, newJson));
	}
}
