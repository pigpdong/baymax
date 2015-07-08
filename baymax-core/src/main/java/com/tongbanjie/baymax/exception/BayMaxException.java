package com.tongbanjie.baymax.exception;

import java.util.Map;

import com.tongbanjie.baymax.jdbc.model.ExecuteCommand;
import com.tongbanjie.baymax.jdbc.model.ParameterCommand;
import com.tongbanjie.baymax.jdbc.model.StatementCreateCommand;

public class BayMaxException extends RuntimeException {

	private static final long serialVersionUID = -378533410763590157L;
	
	private String sql;
	private StatementCreateCommand createCommand;
	private ExecuteCommand executeCommand; 
	private Map<Integer, ParameterCommand> parameterCommand;

	public BayMaxException(String message) {
		super(message);
	}

	public BayMaxException(String message, Exception e) {
		super(message, e);
	}
	
	public BayMaxException(String message, Exception e, String sql, StatementCreateCommand createCommand, ExecuteCommand executeCommand, Map<Integer, ParameterCommand> parameterCommand){
		this(message, e);
		this.sql = sql;
		this.createCommand = createCommand;
		this.executeCommand = executeCommand;
		this.parameterCommand = parameterCommand;
	}
	
	public BayMaxException(String message, Exception e, String sql){
		this(message, e);
		this.sql = sql;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + String.format("sql:{%s} createCommand:{%s} executeCommand:{%s} parameterCommand:{%s}",
				sql,
				createCommand !=null ? createCommand:"",
				executeCommand != null ? executeCommand:"",
				parameterCommand != null ? parameterCommand : "");
	}

}
