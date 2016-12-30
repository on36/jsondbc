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

 5，支持对单表排序查询操作符，如SORT
 
 6，支持对单表分页查询操作符，如PAGE
 
 7，支持对单表组合复杂条件查询

TODO LIST
-----------------------------------

 1，多表关联操作
 

使用指导
-----------------------------------

参考表结构

TEST表

列名|类型|备注
----|----|----
ID|INT|主键，自增长
USERNAME|VARCHAR(64)|用户名
PASSWORD|VARCHAR(64)|密码
AGE|INT|年龄
RID|INT|角色ID
CREATETIME|DATETIME|创建时间

ROLE

列名|类型|备注
----|----|----
ROLEID|INT|主键，自增长
ROLENAME|VARCHAR(64)|角色名


##操作符说明

操作符|说明|使用示例
----|----|----
$lt|小于|{"test":{"age":{"$lt":25}}}
$le|小于等于|{"test":{"age":{"$le":25}}}
$gt|大于|{"test":{"age":{"$gt":25}}}
$ge|大于等于|{"test":{"age":{"$ge":25}}}
$eq|等于|{"test":{"age":{"$eq":25}}}
$ne|不等于|{"test":{"age":{"$ne":25}}}
$like|模糊匹配|{"test":{"username":{"$like":"zhang"}}}
$in|包含|{"test":{"username":{"$in":["zhangsan","lisi"]}}}
$nin|不包含|{"test":{"username":{"$nin":["zhangsan","lisi"]}}}
$between|连续区间|{"test":{"age":{"$between":[20,25]}}}
$and|且|{"test":{"$and":[{"age":{"$le":20}},{"username":"zhangsan"}]}}
$or|或|{"test":{"$or":[{"age":{"$le":20}},{"username":"zhangsan"}]}}
$page|分页|{"test":{"$page":[2,10]}}
$columns|查询字段列表|{"test":{"$columns":["id","username","age"]}}
$sort|排序|{"test":{"$sort":{"username":"desc"}}}
$aggre|聚合|{"test":{"$aggre":{"age":"sum"}}}
$refer|关联映射ID|只能跟$join结合使用,如下
$join|JOIN关联|{"test":{"$join":{"role":{"$refer":{"roleid":"rid"},"$columns":["rolename"]}},"$and":[{"age":{"$le":20}},{"username":"zhangsan"}]}}



##示例说明

1,JSONDB.insert

	{
		"test":{"username":"zhangsan","password":"123456","age":21}
	}
等价于

	INSERT INTO TEST(USERNAME,PASSWORD,AGE) VALUES('zhangsan','123456',21);

2,JSONDB.remove

1

	{
		"test":{"id":4}
	}
等价于

	DELETE FROM TEST WHERE ID = 4

2

	{
		"test":{"username":"zhangsan"}
	}
等价于

	DELETE FROM TEST WHERE USERNAME = 'zhangsan'
	
3
	
	{
		"test":{"username":{"$like":"zhang"}}
	}
等价于

	DELETE FROM TEST WHERE USERNAME LIKE '%zhang%'

4

	{
		"test":{"$or":[{"age":{"$ge",25}},{"age":{"$le",20}}]}
	}
等价于
	
	DELETE FROM TEST WHERE AGE >= 25 OR AGE <= 20

5

	{
		"test":{"age":{"$between":[25,30]}}
	}
等价于

	DELETE FROM TEST WHERE BETWEEN 25 AND 30

6

	{
		"test":{"$or":[{"age":{"$ge",25}},{"age":{"$le",20}}],"username":"zhangsan"}
	}
等价于

	DELETE FROM TEST WHERE (AGE >= 25 OR AGE <= 20) AND USERNAME = 'zhangsan'
	
3,JSONDB.update

条件JSON格式

	{
		"test":{"username":"zhangsan"}
	}
	
更新JSON格式
	
	{"age":25,"createTime":"2016-12-27 13:13:13"}
等价于

	UPDATE TEST SET AGE=25,CREATETIME='2016-12-27 13:13:13' WHERE USERNAME = 'zhangsan'
	
4,JSONDB.query

待添加


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
		private JSONDB db;
		@Test
		public void testInsert() {
			String json = "{\"test\":{\"username\":\"lisi\",\"password\":\"789\",\"createTime\":\"2016-12-27 12:23:23\"}}";
			int result = db.insert(json, true);
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
		private JSONDB db;
		@Test
		public void testDelete() {
			String json = "{\"test\":{\"id\",5}}";
			int result = db.remove(json);
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
		private JSONDB db;
		@Test
		public void testDelete() {
			String json = "{\"test\":{\"username\":\"zhangsan\"}}";
			int result = db.remove(json);
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
		private JSONDB db;
		@Test
		public void testUdate() {
			String json = "{\"test\":{\"username\":\"zhangsan\"}}";
			String newJson = "{\"createTime\":\"2016-12-27 13:13:13\",\"age\":25}";
			int result = db.update(json, newJson);
			assertEquals(true, result > 0);
		}
	}

####查询操作

1,根据主键查询数据

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
		private JSONDB db;
		@Test
		public void testQuery() {
			String json = "{\"test\":{\"id\",5}}";
			String result = db.get(json);
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
	
2,根据其它条件查询数据

JSON格式

	{
	    "test":{
	           "$columns":["id","username"],
	           "age":{"$between":[25,30]},
	           "username":{"$NIN":["lisi"]},
	           "$sort":{"username":"desc"}
	     }
	 }

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDB db;
		@Test
		public void testQuery() {
			String json = db.query(
				"{\"test\":{\"$columns\":[\"id\",\"username\"],\"age\":{\"$between\":[25,30]},\"username\":{\"$NIN\":[\"lisi\"]},\"$sort\":{\"username\":\"desc\"}}}");
			String result = db.get(json);
			System.out.println(json);
		}
	}

返回结果

	{
		"test":[
			{"id":13,"username":"wangwu"},
			{"id":16,"username":"mogo"}
		]
	}

3,根据分页查询数据

JSON格式

	{
		"test":{
			"$join":{
				"role":{
					"$refer":{
						"roleid":"rid"
						},
					"$columns":["rolename"]
					}
				},
			"$columns":["id","username"],
			"$or":[
				{
					"age":{"$le":20}
				},
				{
					"age":{"$eq":25}
				}
			],
			"username":{
					"$NIN":["lisi"]
				},
			"$page":[0,10]
		}
	}

实现代码

	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "classpath*:spring.xml" })
	public class BaseTest extends AbstractJUnit4SpringContextTests {
		@Autowired
		private JSONDB db;
		@Test
		public void testQuery() {
			String json = db.page(
				"{\"test\":{\"$join\":{\"role\":{\"$refer\":{\"roleid\":\"rid\"},\"$columns\":[\"rolename\"]}},\"$columns\":[\"id\",\"username\"],\"$or\":[{\"age\":{\"$le\":20}},{\"age\":{\"$eq\":25}}],\"username\":{\"$NIN\":[\"lisi\"]},\"$page\":[0,10]}}");
			String result = db.get(json);
			System.out.println(json);
		}
	}

返回结果

	{
		"test":[
			{
				"rolename":"teacher",
				"id":11,
				"username":"zhangsan"
			}
		]
	}

待添加。。。。。


## 作者

[点击联系我](mailto:say_hello_plz@qq.com)<br />

