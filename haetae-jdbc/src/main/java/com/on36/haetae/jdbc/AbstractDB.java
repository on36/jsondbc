package com.on36.haetae.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDB {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
}
