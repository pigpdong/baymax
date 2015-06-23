package com.tongbanjie.baymax.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import com.tongbanjie.baymax.datasources.DataSourceDispatcher;
import com.tongbanjie.baymax.datasources.NativeDataSourceHandler;

/**
 * 基于{@link DataSourceTransactionManager}改造的多数据源事务管理器。 使用了
 * {@link org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy}
 * 来包装原始的数据源，LazyConnectionDataSourceProxy获取的connection只有在第一次获取
 * statement对象时才会真正的从池中获取一个connection。 所以在这个事务管理器里面，一开始就对每个数据源都创建一个事务是没有性能问题的，应为
 * 只要后面没有用到某个数据源关联的事务，真实的connection就不会被获取更不会被commit。
 * 
 * TODO 暂时不支持savePoint
 *
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
 *
 * @author dawei
 */
public class MultipleDataSourcesTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {
	protected transient Logger logger = org.slf4j.LoggerFactory.getLogger(MultipleDataSourcesTransactionManager.class);

	private static final long serialVersionUID = 4712923770419532385L;

	private DataSourceDispatcher dataSourceDispatcher;
	Set<NativeDataSourceHandler> nativeDataSourceHandlerSet;

	/**
	 * 获取一个在多数据源之上的事物
	 */
	@Override
	protected Object doGetTransaction() throws TransactionException {
		MutipleTransaction transaction = new MutipleTransaction();
		Map<DataSource, ConnectionHolderWrap> connectionHolders = new HashMap<DataSource, ConnectionHolderWrap>();
		transaction.setConnectionHolders(connectionHolders);
		for (DataSource ds : dataSourceDispatcher.getDataSourceSet()) {
			// 先从ThreadLocal中获取ConnectionHolder，用于AbstractPlatformTransactionManager后续判断当前是否已经有活跃事务了。
			// 如果已经有获取事务了AbstractPlatformTransactionManager会通过事务定义判断是暂停开器新事物，还是直接使用旧事物。
			// 如果没有活跃事务，则调用这里的doBegin开始新的事物。
			ConnectionHolderWrap conHolder = (ConnectionHolderWrap) TransactionSynchronizationManager.getResource(ds);
			connectionHolders.put(ds, conHolder);
		}
		return transaction;
	}

	/**
	 * 对多数据源事务初始化 循环所有的数据源实例，获取一个connection，setAutoCommit(false)
	 */
	@Override
	protected void doBegin(Object transactionObject, TransactionDefinition definition) throws TransactionException {
		MutipleTransaction txObject = (MutipleTransaction) transactionObject;
		Map<DataSource, ConnectionHolderWrap> connectionHolders = txObject.getConnectionHolders();
		Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<DataSource, ConnectionHolderWrap> entry = ite.next();
			DataSource dataSource = entry.getKey();
			ConnectionHolderWrap holder = entry.getValue();
			Connection con = null;
			try {
				// hoder没有connetcion-获取一个
				if (holder == null || holder.isSynchronizedWithTransaction()) {
					Connection newCon = dataSource.getConnection();
					if (logger.isDebugEnabled()) {
						logger.debug("Acquired Connection [{}] for JDBC transaction", newCon);
					}
					holder = new ConnectionHolderWrap(newCon);
					connectionHolders.put(dataSource, holder);
					txObject.setNewConnectionHolder(true);
				}
				// 设置hoder的connection被加入到spring事务中
				holder.setSynchronizedWithTransaction(true);
				con = holder.getConnection();
				// 初始化-readOnly，Isolation
				Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
				txObject.setPreviousIsolationLevel(previousIsolationLevel);
				if (con.getAutoCommit()) {
					txObject.setMustRestoreAutoCommit(true);
					if (logger.isDebugEnabled()) {
						logger.debug("Switching JDBC Connection [{}] to manual commit", con);
					}
					con.setAutoCommit(false);
				}
				// 设置事务为活跃的
				holder.setTransactionActive(true);
				// 设置超时时间
				int timeout = determineTimeout(definition);
				if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
					holder.setTimeoutInSeconds(timeout);
				}

				// 绑定ConnectionHolder到ThreadLocal
				// 要判断ConnectionHolder是不是新建的，如果不是新建的，就不用绑定了
				// 应为每次通过aop都会创建一个新的MutipleTransaction实例，但实例中的ConnectionHolder默认是ThreadLocal中获取的
				// 所以不是新创建的就不用重复绑定了
				if (txObject.isNewConnectionHolder()) {
					TransactionSynchronizationManager.bindResource(dataSource, holder);
				}
			} catch (Exception e) {
				// (如果ThreadLocal中有Holder, 且Holser中的connection==this connection
				// )说明还在spring事务中，则把Hoder中的Connection这只为Null
				// 否则说明不在spring事务中了，则直接Close connection
				DataSourceUtils.releaseConnection(con, dataSource);
				throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", e);
			}
		}
	}

	/**
	 * 事务提交 循环所有数据源管理的DataSource，循环Commit
	 * 如有有某个Commit失败了，catch掉，接着Commit其他的，保证能commit都会被commit
	 */
	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		MutipleTransaction transaction = (MutipleTransaction) status.getTransaction();
		Map<DataSource, ConnectionHolderWrap> connectionHolders = transaction.getConnectionHolders();
		Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
		TransactionException lastException = null;
		while (ite.hasNext()) {
			Entry<DataSource, ConnectionHolderWrap> entry = ite.next();
			ConnectionHolderWrap holder = entry.getValue();
			Connection con = holder.getConnection();
			if (status.isDebug()) {
				logger.debug("Committing JDBC transaction on Connection [{}]", con);
			}
			try {
				con.commit();
			} catch (SQLException ex) {
				logger.error("Error in commit", ex);
				lastException = new TransactionSystemException("Could not commit JDBC transaction", ex);
			}
		}
		if (lastException != null) {
			throw lastException;
			// Rollback will ensue as long as rollbackOnCommitFailure=true
		}
	}

	/**
	 * 循环回滚，逻辑同commit
	 */
	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		MutipleTransaction transaction = (MutipleTransaction) status.getTransaction();
		Map<DataSource, ConnectionHolderWrap> connectionHolders = transaction.getConnectionHolders();
		Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
		TransactionException lastException = null;
		while (ite.hasNext()) {
			Entry<DataSource, ConnectionHolderWrap> entry = ite.next();
			ConnectionHolderWrap holder = entry.getValue();
			Connection con = holder.getConnection();
			if (status.isDebug()) {
				logger.debug("Rolling back JDBC transaction on Connection [{}]", con);
			}
			try {
				con.rollback();
			} catch (SQLException e) {
				logger.error("Error in roll back", e);
				lastException = new TransactionSystemException("Could not roll back JDBC transaction", e);
			}
		}
		if (lastException != null) {
			throw lastException;
		}
	}

	/**
	 * 事务结束后回收资源 循环ConnectionHolder-释放
	 */
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		MutipleTransaction txObject = (MutipleTransaction) transaction;
		Map<DataSource, ConnectionHolderWrap> connectionHolders = txObject.getConnectionHolders();
		Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<DataSource, ConnectionHolderWrap> entry = ite.next();
			DataSource dataSource = entry.getKey();
			ConnectionHolderWrap holder = entry.getValue();
			// 如果这个ThreadLocal是在这次AOP的MutipleTransaction实例中创建的则移除。
			// 应为多次AOP，事务传播时，MutipleTransaction对象不是同一个，但包含的ConnectionHolder可能一样
			// 所以在第二次AOP中不能把上次AOP打开的Connection给清除掉了
			// ThreadLocal中移除ConnectionHolder, if exposed.
			if (txObject.isNewConnectionHolder()) {
				TransactionSynchronizationManager.unbindResource(dataSource);
			}

			// 重置Connection.
			Connection con = holder.getConnection();
			try {
				if (txObject.isMustRestoreAutoCommit()) {
					con.setAutoCommit(true);
				}
				// Isolation
				DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
			} catch (Throwable ex) {
				logger.debug("Could not reset JDBC Connection after transaction", ex);
			}

			if (txObject.isNewConnectionHolder()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
				}
				// 断掉connection的引用
				DataSourceUtils.releaseConnection(con, dataSource);
			}
			holder.clear();
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataSourceDispatcher);
		nativeDataSourceHandlerSet = dataSourceDispatcher.getNativeDataSourceHandlerSet();
	}

	protected PlatformTransactionManager createTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	public DataSourceDispatcher getDataSourceDispatcher() {
		return dataSourceDispatcher;
	}

	public void setDataSourceDispatcher(DataSourceDispatcher dataSourceDispatcher) {
		this.dataSourceDispatcher = dataSourceDispatcher;
	}

	/**
	 * 判断这个Transaction是否是活跃的
	 */
	@Override
	protected boolean isExistingTransaction(Object transaction) {
		MutipleTransaction txObject = (MutipleTransaction) transaction;
		return (txObject.getConnectionHolders() != null && txObject.isTransactionActive());
	}

	/**
	 * 事务类
	 * 
	 * @see JdbcTransactionObjectSupport
	 * @author dawei
	 *
	 */
	public static class MutipleTransaction implements SavepointManager, SmartTransactionObject {

		/**
		 * 原始的JdbcDataSourceTransactionManager只持有一个ConnectionHolder，
		 * 这里包装为一个列表用来支持多数据源
		 */
		Map<DataSource, ConnectionHolderWrap> connectionHolders;

		// 如果这个ThreadLocal是在这次AOP的MutipleTransaction实例中创建的则移除。
		// 应为多次AOP，事务传播时，MutipleTransaction对象不是同一个，但包含的ConnectionHolder可能一样
		// 所以在第二次AOP中不能把上次AOP打开的Connection给清除掉了
		// TODO 换成Map<DataSource, boolean>
		// 用来判断AOP结束时，是否要清除ThreadLocal中的这个ConnectionHolder
		// doBegin中设置 用于判断这个connectin是否在当前aop打开的，是 才会提交，回滚。
		private boolean newConnectionHolder;

		// doBegin中设置  用于退出AOP时重置
		private boolean mustRestoreAutoCommit;

		// doBegin中设置 用于退出AOP时重置
		private Integer previousIsolationLevel;

		private boolean savepointAllowed = false;

		/**
		 * 是否有活跃的事务
		 * 
		 * @return
		 */
		protected boolean isTransactionActive() {
			boolean transactionActive = false;
			Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
			while (ite.hasNext()) {
				ConnectionHolderWrap connectionHolderWrap = ite.next().getValue();
				if (connectionHolderWrap != null && connectionHolderWrap.isTransactionActive()) {
					transactionActive = true;
				}
			}
			return transactionActive;
		}

		@Override
		public boolean isRollbackOnly() {

			return false;
		}

		public boolean isNewConnectionHolder() {
			return newConnectionHolder;
		}

		public void setNewConnectionHolder(boolean newConnectionHolder) {
			this.newConnectionHolder = newConnectionHolder;
		}

		public boolean isMustRestoreAutoCommit() {
			return mustRestoreAutoCommit;
		}

		public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
			this.mustRestoreAutoCommit = mustRestoreAutoCommit;
		}

		public Map<DataSource, ConnectionHolderWrap> getConnectionHolders() {
			return connectionHolders;
		}

		public void setConnectionHolders(Map<DataSource, ConnectionHolderWrap> connectionHolders) {
			this.connectionHolders = connectionHolders;
		}

		public Integer getPreviousIsolationLevel() {
			return previousIsolationLevel;
		}

		public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
			this.previousIsolationLevel = previousIsolationLevel;
		}

		public boolean isSavepointAllowed() {
			return savepointAllowed;
		}

		public void setSavepointAllowed(boolean savepointAllowed) {
			this.savepointAllowed = savepointAllowed;
		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub

		}

		// @see JdbcTransactionObjectSupport
		@Override
		public Object createSavepoint() throws TransactionException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollbackToSavepoint(Object savepoint) throws TransactionException {
			// TODO Auto-generated method stub

		}

		@Override
		public void releaseSavepoint(Object savepoint) throws TransactionException {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * setTransactionActive()方法是Protecte的,包装下方便调用.
	 * 
	 * @author dawei
	 *
	 */
	public static class ConnectionHolderWrap extends ConnectionHolder {

		public ConnectionHolderWrap(Connection connection) {
			super(connection);
		}

		// doBegin中设置为true，表示当前事务内可用
		@Override
		protected void setTransactionActive(boolean transactionActive) {
			super.setTransactionActive(transactionActive);
		}

		@Override
		protected boolean isTransactionActive() {
			return super.isTransactionActive();
		}

		//doBegin中setSynchronizedWithTransaction(true), 表示这个资源和Transaction绑定了
		//synchronizedWithTransaction
	}
}
