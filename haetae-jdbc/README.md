为什么会有这个项目？（Haetae-JDBC-JSON）

===================================

 目前，WEB项目多数以SQL语句来操作关系型数据，针对目前的RESTFUL+微服务的趋势，为了操作数据格式统一，

 才有本项目的想法，实现写库与读库都通过JSON格式来操作，不再依赖SQL语句，现在还只能是开发和测试版本，

 请不要用于任何生产环境。所有JSON操作方式参考MongoDB
 
 

特性

-----------------------------------

 1，基于JSON格式的操作关系型数据库，目前只在MySQL 5.6上测试通过，其它数据库还末测试

 2，基于JSON格式支持对单表的增删改查

 3，支持对单表的聚合查询，如SUM，COUNT，AVG

 4，支持对单表条件操作符，如OR,AND,LIKE,<=,<,>,>=,<>,IN,NOT IN,BETWEEN等

 5，支持对单表组合复杂条件查询

TODO LIST

-----------------------------------

 1，多表关联操作
 

使用指导

-----------------------------------

参考表结构

列名|类型|备注
----|----|----
ID|INT|主键，自增长
USERNAME|VARCHAR(64)|用户名
PASSWORD|VARCHAR(64)|密码
AGE|INT|年龄
CREATETIME|DATETIME|创建时间

####新增操作

JSON格式

	{
		"test":{//对应表名
			"username":"lisi",//对应字段名
			"password":"789",//对应字段名
			"createTime":"2016-12-27 12:23:23"//对应字段名
		}
	}

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDao dao;
		@Test
		public void testInsert() {
			String json = "{\"test\":{\"username\":\"lisi\",\"password\":\"789\",\"createTime\":\"2016-12-27 12:23:23\"}}";
			int result = dao.insert(json, true);
			System.out.println(result);
			assertEquals(true, result > 0);
		}
	}

####删除操作

1，根据主键删除

JSON格式

	{
		"test":{//对应表名
			"id":5//对应主键
		}
	}

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDao dao;
		@Test
		public void testDelete() {
			String json = "{\"test\":{\"id\",5}}";
			int result = dao.delete(json);
			assertEquals(true, result > 0);
		}
	}

2，根据其它条件删除
JSON格式

	{
		"test":{//对应表名
			"username":"zhangsan"//对应字段
		}
	}

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDao dao;
		@Test
		public void testDelete() {
			String json = "{\"test\":{\"username\":\"zhangsan\"}}";
			int result = dao.delete(json);
			assertEquals(true, result > 0);
		}
	}

####更新操作

条件JSON格式

	{
		"test":{//对应表名
			"username":"zhangsan"//对应字段
		}
	}
	
更新JSON格式
	
	{
		"age":25,//对应字段
		"createTime":"2016-12-27 13:13:13"
	}
实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDao dao;
		@Test
		public void testUdate() {
			String json = "{\"test\":{\"username\":\"zhangsan\"}}";
			String newJson = "{\"createTime\":\"2016-12-27 13:13:13\",\"age\":25}";
			int result = dao.update(json, newJson);
			assertEquals(true, result > 0);
		}
	}

####查询操作

根据主键查询数据

JSON格式

	{
		"test":{//对应表名
			"id":5//对应主键
		}
	}

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDao dao;
		@Test
		public void testQuery() {
			String json = "{\"test\":{\"id\",5}}";
			String result = dao.get(json);
			System.out.println(json);
		}
	}

返回结果

	{
		"test":{
			"password":"789",
			"createtime":"2016-12-08 13:13:13",
			"id":5,
			"age":18,
			"username":"zhangsan"
		}
	}

待添加。。。。。


## 作者

[点击联系我](mailto:say_hello_plz@qq.com)<br />
