1. 只做简单的SQL解析。
2. SQL提取 tableName,rule中存在对应tableName则路由
3. SQL提取 对应partitionKeys的参数。
4. 根据partitionKeys的参数带入rule计算。
5. 返回多个 路由对象。
6. 分别执行多个路由对象。
7. 合并List，Map,boolean,integer
8. 不考虑group by,order by
9. 不考虑limit,返回结果可能多余limit,需要自己排序后手动截取。

下一版本考虑一下问题
1. 聚合函数