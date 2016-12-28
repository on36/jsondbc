package com.on36.haetae.jdbc;

import com.alibaba.fastjson.JSONObject;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月27日
 */
public class PageQueryPreparedStatementCreator extends QueryPreparedStatementCreator {

	private int countByPage = 100;
	private int currentPage = 1;

	public PageQueryPreparedStatementCreator(String json, String... columns) {
		super(json, columns);
	}

	@Override
	protected String tail() {
		if (this.countByPage < 1)
			throw new JDBCException(String.format("At paged-query the countByPage should not be %s", this.countByPage));
		if (this.currentPage < 0)
			throw new JDBCException(String.format("At paged-query the currentPage should not be %s", this.currentPage));

		return String.format(" LIMIT %s,%s", (this.currentPage > 0 ? (this.currentPage - 1) : 0) * this.countByPage,
				this.countByPage);
	}

	@Override
	protected void fill(JSONObject data) {
		if (data == null)
			return;

		this.countByPage = data.getIntValue("countByPage");
		this.currentPage = data.getIntValue("currentPage");
	}
}
