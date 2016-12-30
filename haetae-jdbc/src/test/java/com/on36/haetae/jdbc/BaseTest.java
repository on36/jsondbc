package com.on36.haetae.jdbc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zhanghr
 * @date 2016年12月26日
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring.xml" })
public class BaseTest extends AbstractJUnit4SpringContextTests {
	@Autowired
	private JSONDB db;

	// @Test
	public void testInsert() {
		String json = "{\"test\":{\"id\":3,\"username\":\"lisi\",\"password\":\"789\",\"createTime\":\"2016-12-27 12:23:23\"}}";
		int result = db.insert(json);
		System.out.println(result);
		assertEquals(true, result > 0);
	}

	// @Test
	public void testDelete() {
		String json = "{\"test\":{\"$or\":[{\"username\":\"zhangsan\"},{\"createTime\":{\"$ge\":\"2016-12-08\",\"$le\":\"2016-12-11\"}}]}}";
		int result = db.remove(json);
		assertEquals(true, result > 0);
	}

	// @Test
	public void testUpdate() {
		String json = "{\"test_copy\":{\"username\":\"zhangsan\"}}";
		String newJson = "{\"createTime\":\"2016-12-08 13:13:13\",\"age\":{\"$inc\":1}}";
		int result = db.update(json, newJson);
		assertEquals(true, result > 0);
	}

	@Test
	public void testQuery() {
		boolean result = true;

		// String json = dao.query(
		// "{\"test\":{\"username\":\"lisi\",\"createTime\":{\"$lt\":\"2016-12-09\"}}}");
		String json = db.page(
				"{\"test_copy\":{\"$join\":{\"role\":{\"$refer\":{\"roleid\":\"rid\"},\"roleid\":\"1;drop table test;\",\"$columns\":[\"rolename\"]}},\"$columns\":[\"id\",\"username\"],\"$or\":[{\"age\":{\"$le\":20}},{\"age\":{\"$eq\":25}}],\"username\":{\"$NIN\":[\"lisi\"]},\"$page\":[0,10]}}");
		System.out.println(json);
		json = db.query(
				"{\"test_copy\":{\"$columns\":[\"id\",\"username\"],\"age\":{\"$between\":[25,30]},\"username\":{\"$NIN\":[\"lisi\"]},\"$sort\":{\"username\":\"desc\",\"id\":\"asc\"}}}");
		System.out.println(json);
		json = db.aggregate(
				"{\"test_copy\":{\"$or\":[{\"age\":{\"$le\":20}},{\"age\":{\"$eq\":25}}],\"username\":{\"$NIN\":[\"lisi\"]},\"$aggre\":{\"age\":\"avg\",\"id\":\"avg\"}}}");
		System.out.println(json);
		assertEquals(true, result);
	}

}
