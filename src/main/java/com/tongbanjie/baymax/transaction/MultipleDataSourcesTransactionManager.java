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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import com.tongbanjie.baymax.datasources.DataSourceDispatcher;
import com.tongbanjie.baymax.datasources.NativeDataSourceHandler;

/**
 * use {@link org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy}
 * to wrap all of the data sources we may use in TransactionManager and DAOs.
 * {@link org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy}
 * will only fetch a connection when first statement get executed. So even we
 * start transaction on such data sources which are wrapped by
 * {@link org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy},
 * there is no performance penalty at not.
 *
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
 *
 * @author fujohnwang
 * @since 1.0, Jan 28, 2010
 */
public class MultipleDataSourcesTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {
	protected transient Logger logger = org.slf4j.LoggerFactory.getLogger(MultipleDataSourcesTransactionManager.class);

	private static final long serialVersionUID = 4712923770419532385L;

	private DataSourceDispatcher dataSourceDispatcher;
	Set<NativeDataSourceHandler> nativeDataSourceHandlerSet;

	@Override
	protected Object doGetTransaction() throws TransactionException {
		MutipleTransaction transaction = new MutipleTransaction();
		Map<DataSource, ConnectionHolderWrap> connectionHolders = new HashMap<DataSource, ConnectionHolderWrap>();
		transaction.setConnectionHolders(connectionHolders);
		for (DataSource ds : dataSourceDispatcher.getDataSourceSet()) {
			connectionHolders.put(ds, null);
		}
		return transaction;
	}

	/**
	 * We need to disable transaction synchronization so that the shared
	 * transaction synchronization state will not collide with each other. BUT,
	 * for LOB creators to use, we have to pay attention here:
	 * <ul>
	 * <li>if the LOB creator use standard preparedStatement methods, this
	 * transaction synchronization setting is OK;</li>
	 * <li>if the LOB creator don't use standard PS methods, you have to find
	 * other way to make sure the resources your LOB creator used should be
	 * cleaned up after the transaction.</li>
	 * </ul>
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
				if (holder == null || holder.isSynchronizedWithTransaction()) {
					Connection newCon = dataSource.getConnection();
					if (logger.isDebugEnabled()) {
						logger.debug("Acquired Connection [{}] for JDBC transaction", newCon);
					}
					holder = new ConnectionHolderWrap(newCon);
					connectionHolders.put(dataSource, holder);
					txObject.setNewConnectionHolder(true);
				}
				holder.setSynchronizedWithTransaction(true);
				con = holder.getConnection();
				Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
				txObject.setPreviousIsolationLevel(previousIsolationLevel);
				if (con.getAutoCommit()) {
					txObject.setMustRestoreAutoCommit(true);
					if (logger.isDebugEnabled()) {
						logger.debug("Switching JDBC Connection [{}] to manual commit", con);
					}
					con.setAutoCommit(false);
				}
				holder.setTransactionActive(true);

				int timeout = determineTimeout(definition);
				if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
					txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
				}

				// Bind the session holder to the thread.
				if (txObject.isNewConnectionHolder()) {
					TransactionSynchronizationManager.bindResource(dataSource, holder);
				}
			} catch (Exception e) {
				DataSourceUtils.releaseConnection(con, dataSource);
				throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", e);
			}
		}
	}

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

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		MutipleTransaction txObject = (MutipleTransaction) transaction;
		Map<DataSource, ConnectionHolderWrap> connectionHolders = txObject.getConnectionHolders();
		Iterator<Entry<DataSource, ConnectionHolderWrap>> ite = connectionHolders.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<DataSource, ConnectionHolderWrap> entry = ite.next();
			DataSource dataSource = entry.getKey();
			ConnectionHolderWrap holder = entry.getValue();
			// Remove the connection holder from the thread, if exposed.
			if (txObject.isNewConnectionHolder()) {
				TransactionSynchronizationManager.unbindResource(dataSource);
			}

			// Reset connection.
			Connection con = holder.getConnection();
			try {
				if (txObject.isMustRestoreAutoCommit()) {
					con.setAutoCommit(true);
				}
				DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
			} catch (Throwable ex) {
				logger.debug("Could not reset JDBC Connection after transaction", ex);
			}

			if (txObject.isNewConnectionHolder()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
				}
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
	 * 事务类
	 * 
	 * @author dawei
	 *
	 */
	public static class MutipleTransaction extends JdbcTransactionObjectSupport {

		Map<DataSource, ConnectionHolderWrap> connectionHolders;

		private boolean newConnectionHolder;

		private boolean mustRestoreAutoCommit;
		
		private Integer previousIsolationLevel;

		private boolean savepointAllowed = false;

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

		@Override
		protected void setTransactionActive(boolean transactionActive) {
			super.setTransactionActive(transactionActive);
		}

	}
}
