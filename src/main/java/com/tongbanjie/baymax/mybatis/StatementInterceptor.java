package com.tongbanjie.baymax.mybatis;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import com.tongbanjie.baymax.model.Sql;
import com.tongbanjie.baymax.model.SqlHandler;
import com.tongbanjie.baymax.utils.Log;
import com.tongbanjie.baymax.utils.ReflectionUtils;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class StatementInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

		String sql = statementHandler.getBoundSql().getSql();
		Log.debug("----Original Sql [" + sql + "]");

		String targetSql = SqlHandler.get().getTargetSql();
		Log.debug("----Converted Sql [" + targetSql + "]");
		
		Sql target = SqlHandler.get();
		if(target != null && target.isSqlReWrite()){
			if (!sql.equals(targetSql)) {
				// 需要SQL重写
				ReflectionUtils.setFieldValue(statementHandler.getBoundSql(), "sql", target.getTargetSql());
				if(target.getReWriteParameter() != null){
					//需要参数重写
					ReflectionUtils.setFieldValue(statementHandler.getBoundSql(), "parameterMappings", target.getReWriteParameter());
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
