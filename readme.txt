目标:支持分库分表,简单好用,底耦合

主要流程：SqlSessionTemplate->解析SQL(提取表名,Where条件)->SQL路由->创建SESSION-->使用Mybatis执行SQL->合并结果集-->关闭SESSION

1.SqlSessionTemplate:
在Mybatis的入口处做了一层Proxy.在这个Proxy中,在Sql真实执行前后嵌入BayMax的逻辑代码。

2.解析SQL
其实就是提取了一下SQL中的表明, where条件中的KEY,VALUE.注意:分区列只能是以 user_id='123'这种名值对的形式出现,否则全表扫描。
这里所说的全表扫描不是真正意义上的全表扫描,是指这条SQL会被发送到所有的数据分区上执行,让后合并结果.

3.SQL路由
在XML对逻辑表配置路由表达式,把第二部中提取的WHERE条件代入路由表达式计算,得出路由目标数据源名称/目标表名.
替换SQL中的逻辑表名为目标表名.

4.创建SESSION



1. 只做简单的SQL解析。
2. SQL提取 tableName,rule中存在对应tableName则路由
3. SQL提取 对应shardingKeys的参数。
4. 根据shardingKeys的参数带入rule计算。
5. 可能返回多个 路由目标SQL对象。
6. 分别执行多个路由对象。
7. 合并List,Map,boolean,Number
8. 不考虑group by,order by
9. 不考虑limit,返回结果可能多余limit,需要自己排序后手动截取。

下一版本考虑一下问题
1. 聚合函数