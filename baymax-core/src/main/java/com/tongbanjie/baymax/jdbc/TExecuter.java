package com.tongbanjie.baymax.jdbc;

import com.tongbanjie.baymax.datasource.MultipleDataSource;
import com.tongbanjie.baymax.exception.BayMaxException;
import com.tongbanjie.baymax.exception.TraceContext;
import com.tongbanjie.baymax.jdbc.model.*;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.router.model.ExecutePlan;
import com.tongbanjie.baymax.router.model.ExecuteType;
import com.tongbanjie.baymax.router.model.TargetSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sidawei on 16/1/29.
 */
public class TExecuter {

    private final static Logger logger = LoggerFactory.getLogger(TExecuter.class);

    private IRouteService               routeService;
    private MultipleDataSource          dataSource;
    private Map<DataSource, Connection> openedConnection;
    private TConnection                 currentConnection;

    public TExecuter(IRouteService routeService, MultipleDataSource dataSource, TConnection currentConnection, Map<DataSource, Connection> openedConnection){
        this.routeService = routeService;
        this.dataSource = dataSource;
        this.openedConnection = openedConnection;
        this.currentConnection = currentConnection;
    }

    public ResultSetHandler execute(StatementCreateCommand createCommand, ExecuteCommand executeCommand, Map<Integer, ParameterCommand> parameterCommand, TStatement stmt) throws SQLException{
        TraceContext trace = new TraceContext();
        try {
            return execute(createCommand, executeCommand, parameterCommand, stmt, trace);
        }catch (BayMaxException e){
            logger.error("BayMax Execute SQL Error : trace{"+trace.toString()+"}" ,e);
            throw new SQLException(e);
        }catch(SQLException e){
            logger.error("BayMax Execute SQL Error : trace{"+trace.toString()+"}" ,e);
            throw e;
        }
    }

    public ResultSetHandler execute(StatementCreateCommand createCommand, ExecuteCommand executeCommand, Map<Integer, ParameterCommand> parameterCommand, TStatement stmt, TraceContext trace) throws SQLException {

        boolean userPreparedStatement = createCommand.getMethod() != StatementCreateMethod.createStatement ? true : false;
        String sql = null;
        if(userPreparedStatement){
            sql = ((TPreparedStatement)stmt).getSql();
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
            DataSource targetDataSource = targetPartition == null ? dataSource.getDefaultDataSource() : dataSource.getDataSourceByName(targetPartition);
            Connection conn = openedConnection.get(targetDataSource);	// 尝试获取一个已经打开的Connection
            if (conn == null) {
                conn = targetDataSource.getConnection();				// 打开一个Connection
                conn.setAutoCommit(currentConnection.getAutoCommit());
                conn.setTransactionIsolation(currentConnection.getTransactionIsolation());
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
            ExecuteMethod.MethodReturnType methodReturnType = method.getReturnType();	// 确定方法的返回类型
            if(methodReturnType == ExecuteMethod.MethodReturnType.int_type){
                resultType = false;										// executeUpdate
            }else if(methodReturnType == ExecuteMethod.MethodReturnType.result_set_type){
                resultType = true;										// executeQueary
            }else if(methodReturnType == ExecuteMethod.MethodReturnType.boolean_type){
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

    private void meargeResult(){

    }
}
