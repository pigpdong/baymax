/**
 * copy from spirng-mybatis
 */
package com.tongbanjie.baymax.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 两个点
 * 1. 创建connection
 * 2. 创建session
 */
/**
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions
 * from Spring {@code TransactionSynchronizationManager}. Also works if no
 * transaction is active.
 *
 * @version $Id: SqlSessionUtils.java 3816 2011-06-19 05:58:18Z eduardo.macarron
 *          $
 */
public final class SqlSessionUtils {

	private static final Logger logger = LoggerFactory.getLogger(SqlSessionUtils.class);

	/**
	 * This class can't be instantiated, exposes static utility methods only.
	 */
	private SqlSessionUtils() {
		// do nothing
	}

	/**
	 * Creates a new MyBatis {@code SqlSession} from the
	 * {@code SqlSessionFactory} provided as a parameter and using its
	 * {@code DataSource} and {@code ExecutorType}
	 *
	 * @param sessionFactory
	 *            a MyBatis {@code SqlSessionFactory} to create new sessions
	 * @return a MyBatis {@code SqlSession}
	 * @throws TransientDataAccessResourceException
	 *             if a transaction is active and the {@code SqlSessionFactory}
	 *             is not using a {@code SpringManagedTransactionFactory}
	 */
	public static SqlSession getSqlSession(SqlSessionFactory sessionFactory) {
		// si.dawei
		throw new UnsupportedOperationException("暂不支持");

		/*
		 * ExecutorType executorType =
		 * sessionFactory.getConfiguration().getDefaultExecutorType(); return
		 * getSqlSession(sessionFactory, executorType, null);
		 */
	}

	/**
	 * If a Spring transaction is active it uses {@code DataSourceUtils} to get
	 * a Spring managed {@code Connection}, then creates a new
	 * {@code SqlSession} with this connection and synchronizes it with the
	 * transaction. If there is not an active transaction it gets a connection
	 * directly from the {@code DataSource} and creates a {@code SqlSession}
	 * with it.
	 *
	 * @param sessionFactory
	 *            a MyBatis {@code SqlSessionFactory} to create new sessions
	 * @param executorType
	 *            The executor type of the SqlSession to create
	 * @param exceptionTranslator
	 *            Optional. Translates SqlSession.commit() exceptions to Spring
	 *            exceptions.
	 * @throws TransientDataAccessResourceException
	 *             if a transaction is active and the {@code SqlSessionFactory}
	 *             is not using a {@code SpringManagedTransactionFactory}
	 * @see SpringManagedTransactionFactory
	 */
	public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType,
			PersistenceExceptionTranslator exceptionTranslator, DataSource targetDataSource) {

		Assert.notNull(sessionFactory, "No SqlSessionFactory specified");
		Assert.notNull(executorType, "No ExecutorType specified");
		Assert.notNull(targetDataSource, "No DataSource specified");

		DataSourceWrap dataSourceWrap = new DataSourceWrap(targetDataSource);
		SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(dataSourceWrap);

		if (holder != null && holder.isSynchronizedWithTransaction()) {
			if (holder.getExecutorType() != executorType) {
				throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
			}

			holder.requested();

			if (logger.isDebugEnabled()) {
				logger.debug("Fetched SqlSession [" + holder.getSqlSession() + "] from current transaction");
			}

			return holder.getSqlSession();
		}

		// si.dawei 使用参数中的Datasource
		// DataSource dataSource =
		// sessionFactory.getConfiguration().getEnvironment().getDataSource();

		// SqlSessionFactoryBean unwraps TransactionAwareDataSourceProxies but
		// we keep this check for the case that SqlSessionUtils is called from
		// custom code
		boolean transactionAware = (targetDataSource instanceof TransactionAwareDataSourceProxy);
		Connection conn;
		try {
			conn = transactionAware ? targetDataSource.getConnection() : DataSourceUtils.getConnection(targetDataSource);
		} catch (SQLException e) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC Connection for SqlSession", e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Creating SqlSession with JDBC Connection [" + conn + "]");
		}

		// Assume either DataSourceTransactionManager or the underlying
		// connection pool already dealt with enabling auto commit.
		// This may not be a good assumption, but the overhead of checking
		// connection.getAutoCommit() again may be expensive (?) in some drivers
		// (see DataSourceTransactionManager.doBegin()). One option would be to
		// only check for auto commit if this function is being called outside
		// of DSTxMgr, but to do that we would need to be able to call
		// ConnectionHolder.isTransactionActive(), which is protected and not
		// visible to this class.
		//SqlSession session = sessionFactory.openSession(executorType, conn);
		SqlSession session = openSessionFromConnection(sessionFactory.getConfiguration(), executorType, conn, targetDataSource);

		// Register session holder and bind it to enable synchronization.
		//
		// Note: The DataSource should be synchronized with the transaction
		// either through DataSourceTxMgr or another tx synchronization.
		// Further assume that if an exception is thrown, whatever started the
		// transaction will
		// handle closing / rolling back the Connection associated with the
		// SqlSession.
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			if (!(sessionFactory.getConfiguration().getEnvironment().getTransactionFactory() instanceof SpringManagedTransactionFactory)
					&& /* conn==dataSource已经绑定的conn */DataSourceUtils.isConnectionTransactional(conn, targetDataSource)) {
				throw new TransientDataAccessResourceException(
				/**
				 * conn==dataSource已经绑定的conn,如果已经绑定了,就不该进来了.
				 */
				"SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Registering transaction synchronization for SqlSession [" + session + "]");
			}
			holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
			TransactionSynchronizationManager.bindResource(dataSourceWrap, holder);
			TransactionSynchronizationManager.registerSynchronization(new SqlSessionSynchronization(holder, targetDataSource));
			holder.setSynchronizedWithTransaction(true);
			holder.requested();
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("SqlSession [" + session + "] was not registered for synchronization because synchronization is not active");
			}
		}

		return session;
	}

	/**
	 * 官方流程是在DefaultSqlSessionFactory中创建Session,同时调用SpringManagedTransactionFactory创建Transaction
	 * 这个改为直接在Util中创建Session所以DefaultSqlSessionFactory和SpringManagedTransactionFactory就没用啦啦啦啦.
	 * 这么做的原因是如果使用官方的SpringManagedTransactionFactory创建的Transaction，Transaction内部关联的DataSource都是同一个（SessionFactory）关联的。
	 * 所以不同DataSource创建的Transacton中关联的DataSource就不对了。
	 * 这导致在事务提交后，通过DataSourceUtil relase Connection, 要释放的connection和当前ThreadLocal中的DataSource关联的ConnectonHoder关联的Connection不一致。
	 * 这就导致了DataSourceUtil直接把Connection Close了，而不是交给后面的spirng拦截器处理。
	 * if (dataSource != null) {
	 * //dataSource永远是同一个，应为Mybatis是更具Transactio调用Close方法的，而Transaction中的DataSouce又都是同一个。
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource); 
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// It's the transactional Connection: Don't close it.
				conHolder.released();
				return;
			}
		}
		
		没有把这段代码抽到一个XXXTransactionManager中去是应为，好烦啊！，额且又要在spring中配置，又要重写SessionFactory，所以直接在这里hack了 =。=
	 * @param configuration
	 * @param execType
	 * @param connection
	 * @param dataSource
	 * @return
	 */
	private static SqlSession openSessionFromConnection(Configuration configuration, ExecutorType execType, Connection connection, DataSource dataSource) {
		try {
			boolean autoCommit;
			try {
				autoCommit = connection.getAutoCommit();
			} catch (SQLException e) {
				// Failover to true, as most poor drivers
				// or databases won't support transactions
				autoCommit = true;
			}
			connection = wrapConnection(connection);
			Transaction tx = new SpringManagedTransaction(connection, dataSource);// 官方流程是在SpringManagedTransactionFactory中new
			Executor executor = configuration.newExecutor(tx, execType);
			return new DefaultSqlSession(configuration, executor, autoCommit);
		} catch (Exception e) {
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
		} finally {
			ErrorContext.instance().reset();
		}
	}

	private static Connection wrapConnection(Connection connection) {
		if (logger.isDebugEnabled()) {
			return ConnectionLogger.newInstance(connection);
		} else {
			return connection;
		}
	}

	/**
	 * invoce之后统一关闭Session Checks if {@code SqlSession} passed as an argument is
	 * managed by Spring {@code TransactionSynchronizationManager} If it is not,
	 * it closes it, otherwise it just updates the reference counter and lets
	 * Spring call the close callback when the managed transaction ends
	 *
	 * @param session
	 * @param sessionFactory
	 * @param dataSource
	 */
	public static void closeSqlSession(SqlSession session, DataSource dataSource) {

		Assert.notNull(session, "No SqlSession specified");
		Assert.notNull(dataSource, "No DataSource specified");

		SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(new DataSourceWrap(dataSource));
		if ((holder != null) && (holder.getSqlSession() == session)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Releasing transactional SqlSession [" + session + "]");
			}
			holder.released();// --
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing no transactional SqlSession [" + session + "]");
			}
			session.close();
		}
	}

	/**
	 * invoce之后commit非事务connection Returns if the {@code SqlSession} passed as
	 * an argument is being managed by Spring
	 *
	 * @param session
	 *            a MyBatis SqlSession to check
	 * @param sessionFactory
	 *            the SqlSessionFactory which the SqlSession was built with
	 * @return true if session is transactional, otherwise false
	 */
	public static boolean isSqlSessionTransactional(SqlSession session, DataSource dataSource) {
		Assert.notNull(session, "No SqlSession specified");
		Assert.notNull(dataSource, "No DataSource specified");

		SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(new DataSourceWrap(dataSource));

		return (holder != null) && (holder.getSqlSession() == session);
	}

	/**
	 * Callback for cleaning up resources. It cleans
	 * TransactionSynchronizationManager and also commits and closes the
	 * {@code SqlSession}. It assumes that {@code Connection} life cycle will be
	 * managed by {@code DataSourceTransactionManager} or
	 * {@code JtaTransactionManager}
	 */
	private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {

		private final SqlSessionHolder holder;

		private final DataSourceWrap dataSourceWrap;

		public SqlSessionSynchronization(SqlSessionHolder holder, DataSource dataSource) {
			Assert.notNull(holder, "Parameter 'holder' must be not null");
			Assert.notNull(dataSource, "Parameter 'dataSource' must be not null");

			this.holder = holder;
			dataSourceWrap = new DataSourceWrap(dataSource);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getOrder() {
			// order right before any Connection synchronization
			return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void suspend() {
			TransactionSynchronizationManager.unbindResource(dataSourceWrap);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void resume() {
			TransactionSynchronizationManager.bindResource(dataSourceWrap, this.holder);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void beforeCommit(boolean readOnly) {
			// Connection commit or rollback will be handled by
			// ConnectionSynchronization or
			// DataSourceTransactionManager.
			// But, do cleanup the SqlSession / Executor, including flushing
			// BATCH statements so
			// they are actually executed.
			// SpringManagedTransaction will no-op the commit over the jdbc
			// connection
			if (TransactionSynchronizationManager.isActualTransactionActive()) { // boolean标识由事务管理器调用clear()清除
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Transaction synchronization committing SqlSession [" + this.holder.getSqlSession() + "]");
					}
					this.holder.getSqlSession().commit();// 如果是spring事务，不会真的commit，里面有判断
				} catch (PersistenceException p) {
					if (this.holder.getPersistenceExceptionTranslator() != null) {
						DataAccessException translated = this.holder.getPersistenceExceptionTranslator().translateExceptionIfPossible(p);
						if (translated != null) {
							throw translated;
						}
					}
					throw p;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void afterCompletion(int status) {
			// unbind the SqlSession from tx synchronization
			// Note, assuming DefaultSqlSession, rollback is not needed because
			// rollback on
			// SpringManagedTransaction will no-op anyway. In addition, closing
			// the session cleans
			// up the same internal resources as rollback.
			if (!this.holder.isOpen()) { // referenceCount<=0
				TransactionSynchronizationManager.unbindResource(dataSourceWrap);
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]");
					}
					this.holder.getSqlSession().close();
				} finally {
					this.holder.reset();
				}
			}
		}
	}

	public static class DataSourceWrap {
		private final static String TOKEN = "DataSourceWrap";

		private DataSource dataSource;

		public DataSourceWrap(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public int hashCode() {
			return TOKEN.hashCode() + dataSource.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return dataSource == obj || dataSource.equals(((DataSourceWrap) obj).getDataSource());
		}

		public DataSource getDataSource() {
			return dataSource;
		}

	}

	public static void main(String[] args) {
		DataSource d1 = new SingleConnectionDataSource();
		DataSourceWrap w1 = new DataSourceWrap(d1);
		DataSourceWrap w2 = new DataSourceWrap(d1);
		System.out.println(w1.hashCode() == w2.hashCode());
		System.out.println(w1.equals(w2));
		System.out.println(w1.dataSource.equals(w2.dataSource));
		System.out.println(d1.equals(d1));
	}

}