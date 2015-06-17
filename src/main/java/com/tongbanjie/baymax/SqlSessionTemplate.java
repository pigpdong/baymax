package com.tongbanjie.baymax;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.baymax.datasources.DataSourceDispatcher;
import com.tongbanjie.baymax.model.RouteResult;
import com.tongbanjie.baymax.model.RouteResultType;
import com.tongbanjie.baymax.model.Sql;
import com.tongbanjie.baymax.model.SqlHandler;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.utils.SqlSessionUtils;

/**
 * 所有数据库操作的入口
 * SqlSessionTemplate->解析SQL->SQL路由->使用Mybatis执行SQL->合并结果集
 * 
 * @author dawei
 *
 */
public class SqlSessionTemplate implements SqlSession {

	/**
	 * Mybatis原始的SessionFactory
	 * 如果一个Sql被路由到了其他的DataSource,虽然还是用这个SessionFactory来获取一个新的Session,但是这个新的Session关联的是路由目标的DataSource
	 * 而不是SessionFactory初始化时默认的DataSource.
	 */
	private final SqlSessionFactory sqlSessionFactory;

	/**
	 * Sql执行类型(默认)
	 */
	private final ExecutorType executorType;

	/**
	 * SessionProxy用来嵌入BayMax的入口点.在这里捕获原始SQL,解析,路由,执行
	 */
	private final SqlSession sqlSessionProxy;

	/**
	 * 一个异常翻译器,把JDBC异常翻译为Spring异常.
	 */
	private final PersistenceExceptionTranslator exceptionTranslator;

	/**
	 * 一个多数据源管理器,用来更具数据分区的名称,返回目标数据源实例.
	 */
	private final DataSourceDispatcher dataSourceDispatcher;
	
	/**
	 * 一个SQL路由服务,用来把一个原始SQL,路由到对应的DataSource实例,替换Sql表名.
	 */
	private final IRouteService routeService;

	// TODO
	public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, DataSourceDispatcher dataSourceDispatcher, IRouteService routeService) {
		this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType(), dataSourceDispatcher, routeService);
	}

	// TODO
	public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
			PersistenceExceptionTranslator exceptionTranslator, DataSourceDispatcher dataSourceDispatcher,
			 IRouteService routeService) {

		Assert.notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
		Assert.notNull(executorType, "Property 'executorType' is required");

		this.sqlSessionFactory = sqlSessionFactory;
		this.executorType = executorType;
		this.exceptionTranslator = exceptionTranslator;
		this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(),
				new Class[] { SqlSession.class }, new SqlSessionInterceptor());
		this.dataSourceDispatcher = dataSourceDispatcher;
		this.routeService = routeService;
	}

	// TODO
	public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
			DataSourceDispatcher dataSourcesDispatcher, IRouteService routeService) {
		this(sqlSessionFactory, executorType, new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment()
				.getDataSource(), true), dataSourcesDispatcher, routeService);
	}

	@Override
	public Object selectOne(String statement) {
		return this.selectOne(statement, null);
	}

	@Override
	public Object selectOne(String statement, Object parameter) {
		return sqlSessionProxy.selectOne(statement, parameter);
	}

	@Override
	public List<?> selectList(String statement) {
		return this.selectList(statement, null);
	}

	@Override
	public List<?> selectList(String statement, Object parameter) {
		return sqlSessionProxy.selectList(statement, parameter);
	}

	@Override
	public List<?> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return sqlSessionProxy.selectList(statement, parameter, rowBounds);
	}

	/**
	 * @return Map<?, Map<?, ?>> ==> Map<mapKey, Map<columnName, columnValue>>
	 */
	@Override
	public Map<?, ?> selectMap(String statement, String mapKey) {
		return sqlSessionProxy.selectMap(statement, mapKey);
	}

	@Override
	public Map<?, ?> selectMap(String statement, Object parameter, String mapKey) {
		return sqlSessionProxy.selectMap(statement, parameter, mapKey);
	}

	@Override
	public Map<?, ?> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		return sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler) {
		sqlSessionProxy.select(statement, parameter, handler);
	}

	@Override
	public void select(String statement, ResultHandler handler) {
		this.select(statement, null, RowBounds.DEFAULT, handler);
	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		sqlSessionProxy.select(statement, parameter, rowBounds, handler);
	}

	@Override
	public int insert(String statement) {
		return this.insert(statement, null);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return sqlSessionProxy.insert(statement, parameter);
	}

	@Override
	public int update(String statement) {
		return this.update(statement, null);
	}

	@Override
	public int update(String statement, Object parameter) {
		return sqlSessionProxy.update(statement, parameter);
	}

	@Override
	public int delete(String statement) {
		return this.delete(statement, null);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return sqlSessionProxy.delete(statement, parameter);
	}

	@Override
	public void commit() {
		throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
	}

	@Override
	public void commit(boolean force) {
		throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
	}

	@Override
	public void rollback() {
		throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
	}

	@Override
	public void rollback(boolean force) {
		throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
	}

	@Override
	public List<BatchResult> flushStatements() {
		return sqlSessionProxy.flushStatements();
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
	}

	@Override
	public void clearCache() {
		sqlSessionProxy.clearCache();
	}

	@Override
	public Configuration getConfiguration() {
		return sqlSessionFactory.getConfiguration();
	}

	@Override
	public <T> T getMapper(Class<T> type) {
		return getConfiguration().getMapper(type, this);
	}

	@Override
	public Connection getConnection() {
		return sqlSessionProxy.getConnection();
	}

	/**
	 * 入口,在执行SQL前切入分库分表逻辑.
	 * @author dawei
	 *
	 */
	private class SqlSessionInterceptor implements InvocationHandler {
		// 入口
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String statement = (String) args[0];
			Object parameter = args[1];

			// 参数准备
			BoundSql boundSql = sqlSessionFactory.getConfiguration().getMappedStatement(statement).getBoundSql(parameter);

			// 路由
			RouteResult routeResult = routeService.doRoute(boundSql);
			if(routeResult.getResultType() == RouteResultType.NO){
				final SqlSession sqlSession = SqlSessionUtils.getSqlSession(SqlSessionTemplate.this.sqlSessionFactory,
						SqlSessionTemplate.this.executorType, SqlSessionTemplate.this.exceptionTranslator,
						dataSourceDispatcher.getDefaultDataSource());
				return this.invoke(proxy, method, args, sqlSession);
				//无需路由直接返回执行结果
			}
			
			//RouteResultType.PARTITION || RouteResultType.ALL
			// 循环执行
			// 循环执行前应该启用事务,应为对调用者来说,这应该是一个整体
			List<Sql> sqlList = routeResult.getSqlList();
			List<Object> sqlActionResult = new ArrayList<Object>();
			for (Sql sql : sqlList) {
				SqlHandler.set(sql);
				final SqlSession sqlSession = SqlSessionUtils.getSqlSession(SqlSessionTemplate.this.sqlSessionFactory,
						SqlSessionTemplate.this.executorType, SqlSessionTemplate.this.exceptionTranslator,
						sql.getDataSource());
				sqlActionResult.add(this.invoke(proxy, method, args, sqlSession));
				SqlHandler.clear();
			}// 循环执行完毕应该释放事务
			
			if(sqlActionResult == null || sqlActionResult.size() == 0){
				return null;
			}
			if(sqlActionResult.size() == 1){
				// 只有一个结果 直接return
				return sqlActionResult.get(0);
			}
			
			/**
			 * 结果合并  
			 * selectOne-object
			 * selectList->list
			 * selectMap->map
			 * insert->int
			 * update->int
			 * delete->int
			 * 只有返回类型为List,Int,Map(待测试)的才会有多个执行结果,所以其他类型的就直接return了.
			 * 对于select avg(a),sum(b) from table这种SQL,会分发执行并产生多个执行结果,所以这类查询需要改造业务使用selectList方法,并在业务中合并结果.
			 * 而不能直接使用selectOne方法.// TODO 这个会在后面考虑用BayMax根据语法自动合并。
			 */
			// TODO 结果排序-暂时使用业务排序
			Class<?> resultClass = method.getReturnType();
			if(resultClass == List.class){
				List<Object> mearge = new ArrayList<Object>();
				for(Object obj : sqlActionResult){
					mearge.addAll((List<?>)obj);
				}
				return mearge;
			}else if (resultClass == Map.class) {
				Map<Object, Object> mearge = new HashMap<Object, Object>();
				for(Object map : sqlActionResult){
					mearge.putAll((Map<?, ?>)map);
				}
			} else if (resultClass == Integer.class || "int".equals(resultClass.getName()) || (sqlActionResult.get(0) instanceof Integer)) {
				int mearge = (int)0;
				for(Object obj : sqlActionResult){
					mearge += (Integer)obj;
				}
				return mearge;
			}
			throw new RuntimeException("结果集合并遇到了不支持的类型, 只能需要一个结果, 却返回了多个:resultType"+resultClass + " ,sql:" + statement + " ,param:" + JSON.toJSONString(parameter));
		}

		/**
		 * 执行
		 * @param proxy
		 * @param method
		 * @param args
		 * @param sqlSession
		 * @return
		 * @throws Throwable
		 */
		public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
			try {
				Object result = method.invoke(sqlSession, args);
				if (!SqlSessionUtils.isSqlSessionTransactional(sqlSession, SqlHandler.get().getDataSource())) {
					// commit非事务的connection
					sqlSession.commit();
				}
				return result;
			} catch (Throwable t) {
				Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
				if (SqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
					Throwable translated = SqlSessionTemplate.this.exceptionTranslator
							.translateExceptionIfPossible((PersistenceException) unwrapped);
					if (translated != null) {
						unwrapped = translated;
					}
				}
				throw unwrapped;
			} finally {
				// 统一关闭
				SqlSessionUtils.closeSqlSession(sqlSession, SqlHandler.get().getDataSource());
			}
		}
	}
}
