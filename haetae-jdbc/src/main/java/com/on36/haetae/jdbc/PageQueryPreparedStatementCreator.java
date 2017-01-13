package com.on36.haetae.jdbc;

import com.alibaba.fastjson.JSONArray;
import com.on36.haetae.jdbc.exception.JDBCException;

/**
 * @author zhanghr
 * @date 2016年12月27日
 */
public class PageQueryPreparedStatementCreator extends QueryPreparedStatementCreator {

	private int countByPage;
	private int currentPage;

	public PageQueryPreparedStatementCreator(String json) {
		super(json);
		this.countByPage = 100;
		this.currentPage = 1;
	}

	@Override
	protected String tail() {
		if (this.countByPage < 1)
			throw new JDBCException(String.format("At paged-query the countByPage should not be %s", this.countByPage));
		if (this.currentPage < 0)
			throw new JDBCException(String.format("At paged-query the currentPage should not be %s", this.currentPage));

		if (this.orderBy == null)
			return String.format(" LIMIT %s,%s", (this.currentPage > 0 ? (this.currentPage - 1) : 0) * this.countByPage,
					this.countByPage);
		else
			return String.format(" %s LIMIT %s,%s", orderBy,
					(this.currentPage > 0 ? (this.currentPage - 1) : 0) * this.countByPage, this.countByPage);
	}

	@Override
	protected void fill(String tag, Object object) {
		if (object == null)
			return;
		if (object instanceof JSONArray) {
			switch (tag) {
			case "$PAGE":
				JSONArray data = (JSONArray) object;

				int value = data.getIntValue(1);
				if (value > 0)
					this.countByPage = value;
				int cvalue = data.getIntValue(0);
				if (cvalue > 0)
					this.currentPage = cvalue;
				return;
			default:
				break;
			}
		}

		super.fill(tag, object);
	}
}
