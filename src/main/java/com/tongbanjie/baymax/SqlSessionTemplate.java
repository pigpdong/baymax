package com.tongbanjie.baymax;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
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
import com.tongbanjie.baymax.parser.SqlParser;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.utils.SqlSessionUtils;

public class SqlSessionTemplate implements SqlSession {

	private final SqlSessionFactory sqlSessionFactory;

	private final ExecutorType executorType;

	private final SqlSession sqlSessionProxy;

	private final PersistenceExceptionTranslator exceptionTranslator;

	private final DataSourceDispatcher dataSourceDispatcher;
	
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

	private class SqlSessionInterceptor implements InvocationHandler {
		// 入口
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String statement = (String) args[0];
			Object parameter = args[1];

			// 参数准备
			BoundSql boundSql = sqlSessionFactory.getConfiguration().getMappedStatement(statement).getBoundSql(parameter);

			// Parser
			SqlParser parser = new SqlParser();
			System.out.println(JSON.toJSONString(parser.parse(boundSql), true));
			
			// 路由
			RouteResult routeResult = routeService.doRoute(boundSql.getSql(), boundSql.getParameterMappings());
			List<Sql> sqlList = routeResult.getSqlList();
			
			if(routeResult.getResultType() == RouteResultType.NO){
				// TODO
				dataSourceDispatcher.getDefaultDataSource();
			}
			
			// 循环执行
			// 循环执行前应该启用事务,应为对调用者来说,这应该是一个整体
			List<Object> sqlActionResult = new ArrayList<Object>();
			for (Sql sql : sqlList) {
				SqlHandler.set(sql);
				final SqlSession sqlSession = SqlSessionUtils.getSqlSession(SqlSessionTemplate.this.sqlSessionFactory,
						SqlSessionTemplate.this.executorType, SqlSessionTemplate.this.exceptionTranslator,
						sql.getDataSource());
				sqlActionResult.add(this.invoke(proxy, method, args, sqlSession));
				SqlHandler.clear();
			}// 循环执行完毕应该释放事务
			// 结果合并  TODO 虽然现在还没有实现一条原始SQL更具参数被分发到不同数据分区上执行,但是对于全表扫描还是需要进行结果合并.
			// TODO 结果排序
			Class<?> resultClass = method.getReturnType();
			if(resultClass == List.class){
				List<Object> mearge = new ArrayList<Object>();
				for(Object obj : sqlActionResult){
					mearge.addAll((List<?>)obj);
				}
				return mearge;
			}else if(resultClass == Boolean.class){
				if(sqlActionResult == null || sqlActionResult.size() == 0){
					return false;
				}
				boolean mearge = true;
				for(Object obj : sqlActionResult){
					mearge = mearge && (Boolean)obj;
				}
				return mearge;
			}else if(resultClass == Number.class){
				
			}
			return null;
		}

		// 执行
		public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
			try {
				Object result = method.invoke(sqlSession, args);
				if (!SqlSessionUtils.isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
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
				SqlSessionUtils.closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
			}
		}
	}
}
