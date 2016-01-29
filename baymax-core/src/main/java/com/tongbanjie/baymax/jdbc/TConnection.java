package com.tongbanjie.baymax.jdbc;

import com.tongbanjie.baymax.datasource.MultipleDataSource;
import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.exception.TraceContext;
import com.tongbanjie.baymax.jdbc.adapter.UnsupportedConnectionAdapter;
import com.tongbanjie.baymax.jdbc.model.*;
import com.tongbanjie.baymax.jdbc.model.ExecuteMethod.MethodReturnType;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TConnection extends UnsupportedConnectionAdapter {
	
	private final static Logger logger = LoggerFactory.getLogger(TConnection.class);

	private IRouteService routeService;
	private MultipleDataSource multipleDataSource;
	
	private Map<DataSource, Connection> openedConnection = new ConcurrentHashMap<DataSource, Connection>(2);
	private Connection connectionForMetaData;
	private Set<TStatement> openedStatements = new HashSet<TStatement>(2);
	private boolean isAutoCommit = true; // jdbc规范，新连接为true
	private boolean closed;
	private int transactionIsolation = TRANSACTION_READ_COMMITTED;

	public TConnection(IRouteService routeService, MultipleDataSource multipleDataSource) {
		this.routeService = routeService;
		this.multipleDataSource = multipleDataSource;
	}

	public ResultSetHandler loggerExecuteSql(StatementCreateCommand createCommand, ExecuteCommand executeCommand, Map<Integer, ParameterCommand> parameterCommand, TStatement stmt, TraceContext trace) throws SQLException {
		checkClosed();
		boolean userPreparedStatement = createCommand.getMethod() != StatementCreateMethod.createStatement ? true : false;
		String sql = null;
		if(userPreparedStatement){
			sql = ((TPreparedStatement)stmt).sql;
		}else{
			sql = (String) executeCommand.getArgs()[0];
		}
		
		trace.setSql(sql);
		trace.setCreateCommand(createCommand);
		trace.setExecuteCommand(executeCommand);
		trace.setParameterCommand(parameterCommand);

		ExecutePlan plan = routeService.doRoute(sql, parameterCommand);	// 路由
		
		if(logger.isDebugEnabled()){
			logger.debug("BayMax execute SQL:" + plan.toString());
		}

		if (plan.getExecuteType() != ExecuteType.ALL && plan.getExecuteType() != ExecuteType.PARTITION && plan.getExecuteType() != ExecuteType.NO) {
			throw new SQLException("执行计划不正确" + plan.toString());	// 检查执行计划
		}
		
		ResultSetHandler resultSetHandler = new ResultSetHandler();
		List<TargetSql> sqlList = plan.getSqlList();					// 所有要执行的SQL
		List<ResultSet> resultSet = new ArrayList<ResultSet>(sqlList.size());
		stmt.closeOpenedStatement();									// 关闭上一个SQL打开的Statement,一个JDBC规范的Statement只能保持最近一个开发的ResultSet,所以如果用户在同一个Statement上执行SQL,意味着前面的ResultSet可以被关闭了。
		boolean resultType = false;
		for (TargetSql target : sqlList) {
			String targetPartition = target.getPartition();
			DataSource targetDataSource = targetPartition == null ? multipleDataSource.getDefaultDataSource() : multipleDataSource.getDataSourceByName(targetPartition);
			Connection conn = openedConnection.get(targetDataSource);	// 尝试获取一个已经打开的Connection
			if (conn == null) {
				conn = targetDataSource.getConnection();				// 打开一个Connection
				conn.setAutoCommit(getAutoCommit());				
				conn.setTransactionIsolation(transactionIsolation);
				openedConnection.put(targetDataSource, conn);			// 保存Connection
			}
			Statement targetStatement = null;
			if(userPreparedStatement){
				Object[] args = createCommand.getArgs();
				args[0] = target.getTargetSql();
				targetStatement = createCommand.getMethod().prepareStatement(conn, args); 	//打开PrepareadStatement
				for(ParameterCommand command : parameterCommand.values()){					// 设置SQL参数
					command.getParameterMethod().setParameter((PreparedStatement)targetStatement, command.getArgs());
				}
			}else{
				targetStatement = conn.createStatement();				// 打开普通Statement
			}
			stmt.addOpenedStatement(targetStatement);					// 保存Statement
			ExecuteMethod method = executeCommand.getMethod(); 
			Object[] args = executeCommand.getArgs();
			if(!userPreparedStatement){
				args[0] = target.getTargetSql();							// 替换SQL
			}
			Object methodResult = method.executeMethod(targetStatement, executeCommand.getArgs());	// 执行SQL
			MethodReturnType methodReturnType = method.getReturnType();	// 确定方法的返回类型
			if(methodReturnType == MethodReturnType.int_type){
				resultType = false;										// executeUpdate
			}else if(methodReturnType == MethodReturnType.result_set_type){
				resultType = true;										// executeQueary
			}else if(methodReturnType == MethodReturnType.boolean_type){
				resultType = (Boolean) methodResult; 					//
			}else {
				throw new BayMaxException("");
			}
			if (resultType) {
				resultSet.add(targetStatement.getResultSet());			// 保存结果集
			}else{
				resultSetHandler.addUpdateCount(targetStatement.getUpdateCount());// 保存影响的行数
				if(createCommand.getMethod().autoGeneratedKeys() || executeCommand.getMethod().autoGeneratedKeys()){
					// 需要返回自增键
					stmt.setGeneratedKeysResultSet(targetStatement.getGeneratedKeys());
				}
			}
			resultSetHandler.setResultType(resultType);					// 保存返回类型
		}
		resultSetHandler.setResultSet(new TResultSet(resultSet, stmt));
		if(resultType){
			stmt.setCurrentResultSet(resultSetHandler.getResultSet());	// 把resultSetHandler的值直接保存到Statement,减少Statement对他的依赖
		}else{
			stmt.setCurrentUpdateCount(resultSetHandler.getUpdateCount());
		}
		return resultSetHandler;
	}
	
	public ResultSetHandler executeSql(StatementCreateCommand createCommand, ExecuteCommand executeCommand, Map<Integer, ParameterCommand> parameterCommand, TStatement stmt) throws SQLException {
		TraceContext trace = new TraceContext();
		try{
			return loggerExecuteSql(createCommand, executeCommand, parameterCommand, stmt, trace);
		}catch(SQLException e){
			logger.error("BayMax Execute SQL Error : trace{"+trace.toString()+"}" ,e);
			throw e;
		}
	}

	@Override
	public Statement createStatement() throws SQLException {
		checkClosed();
		StatementCreateCommand command = new StatementCreateCommand(StatementCreateMethod.createStatement, null);
		TStatement stmt = new TStatement(this, command);
		openedStatements.add(stmt);
		return stmt;
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		// TODO 暂不不支持设置statement的可保存性,并发性等
		throw new UnsupportedOperationException();
		//return createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkClosed();
		StatementCreateCommand command = new StatementCreateCommand(StatementCreateMethod.prepareStatement_sql, new Object[]{sql});
		PreparedStatement stmt = new TPreparedStatement(this, command, sql);
		openedStatements.add((TStatement) stmt);
		return stmt;
	}

	// JDBC事务相关的autoCommit设置、commit/rollback、TransactionIsolation等
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkClosed();
		if (isAutoCommit() == autoCommit) {
			return;
		}
		this.isAutoCommit = autoCommit;
		if(openedConnection.size() != 0){
			Iterator<Entry<DataSource, Connection>>  ite = openedConnection.entrySet().iterator();
			while(ite.hasNext()){
				ite.next().getValue().setAutoCommit(autoCommit);
			}
		}
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		checkClosed();
		return this.isAutoCommit;
	}

	@Override
	public void commit() throws SQLException {
		checkClosed();
		if(isAutoCommit){
			return;
		}
		SQLException sqlException = null;
		boolean first = true;
		for(Connection conn : openedConnection.values()){
			try{
				conn.commit();
			}catch(SQLException e){
				if(sqlException == null){
					sqlException = e;
				}
				if(first){
					// 第一次Commit就抛异常了,直接break
					break;
				}
			}
			first = false;
		}
//		if(connectionForMetaData != null){
//			try{
//				connectionForMetaData.commit();
//			}catch(SQLException e){
//				sqlException = e;
//			}
//		}
		if(sqlException != null){
			throw sqlException;
		}
	}

	@Override
	public void rollback() throws SQLException {
		checkClosed();
		if(isAutoCommit){
			return;
		}
		SQLException sqlException = null;
		for(Connection conn : openedConnection.values()){
			try{
				conn.rollback();
			}catch(SQLException e){
				if(sqlException == null){
					sqlException = e;
				}
			}
		}
		// 3.metaData
//        if(connectionForMetaData != null){
//			try{
//				connectionForMetaData.rollback();
//			}catch(SQLException e){
//				sqlException = e;
//			}
//		}
		if(sqlException != null){
			throw sqlException;
		}
	}

	@Override
	public void close() throws SQLException {
		if(closed){
			return;
		}
		List<SQLException> exceptions = new LinkedList<SQLException>();
		// 1.statements
        try {
            // 关闭statement
        	Iterator<TStatement> stmtIte = openedStatements.iterator();
        	while(stmtIte.hasNext()){
        		try {
        			stmtIte.next().close();
                } catch (SQLException e) {
                    exceptions.add(e);
                }
        	}
        } catch(Exception e){
        	logger.error("关闭Tconnection 关闭TStatement异常", e);
        }finally {
            openedStatements.clear();
        }
        // 2.connections
        Iterator<Connection> connIte = openedConnection.values().iterator();
        while(connIte.hasNext()){
        	try{
        		connIte.next().close();
        	}catch(SQLException e){
        		exceptions.add(e);
        	}
        }
        // 3.metaData
        if(connectionForMetaData != null && !connectionForMetaData.isClosed()){
			try{
				connectionForMetaData.close();
				connectionForMetaData = null;
			}catch(SQLException e){
				exceptions.add(e);
			}
		}
        closed = true;
        openedConnection.clear();
        if(exceptions.size() > 0){
        	SQLException exception = exceptions.get(0);
        	if(exceptions.size() > 1){
        		for(int i = 1; i<exceptions.size(); i++){
        			exception.setNextException(exceptions.get(i));
        		}
        	}
        	throw exception;
        }
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	/**
	 * 如果已经有有效的Connection则拿一个,如果没有则在默认的DataSource上创建一个.
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		checkClosed();
		if(connectionForMetaData == null){
			if(openedConnection.size() > 0){
				connectionForMetaData = openedConnection.entrySet().iterator().next().getValue();
			}else{
				connectionForMetaData = multipleDataSource.getDefaultConnection();
			}
		}
		if(connectionForMetaData.isClosed()){
			throw new SQLException("No operations allowed after connection closed.");
		}
		return connectionForMetaData.getMetaData();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		this.transactionIsolation = level;
		if(openedConnection.size() != 0){
			Iterator<Entry<DataSource, Connection>>  ite = openedConnection.entrySet().iterator();
			while(ite.hasNext()){
				ite.next().getValue().setTransactionIsolation(level);
			}
		}
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		checkClosed();
		return transactionIsolation;
	}

	/**
	 * 自增
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		checkClosed();
		StatementCreateCommand command = new StatementCreateCommand(StatementCreateMethod.prepareStatement_sql_autoGeneratedKeys, new Object[]{sql, autoGeneratedKeys});
		PreparedStatement stmt = new TPreparedStatement(this, command, sql);
		openedStatements.add((TStatement) stmt);
		return stmt;
	}

	/**
	 * 结果集的保持时间
	 */
	@Override
	public int getHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		return;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return false; // 始终可读写
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	private void checkClosed() throws SQLException {
		if (isClosed()) {
			throw new SQLException("No operations allowed after connection closed.");
		}
	}

	public boolean isAutoCommit() {
		return isAutoCommit;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	/************************************************/

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return isClosed();
	}
}
