package com.tongbanjie.baymax.exception;

public class BayMaxException extends RuntimeException {

	private static final long serialVersionUID = -378533410763590157L;

	private TraceContext trade;
	
	public BayMaxException(String message) {
		super(message);
	}

	public BayMaxException(String message, Exception e) {
		super(message, e);
	}
	
	public BayMaxException(String message, Exception e, String sql){
		this(message, e);
		trade = new TraceContext(sql);
	}
	
	public BayMaxException(String message, Exception e, TraceContext trade){
		this(message, e);
		this.trade = trade;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + (trade!=null?trade.toString():"");
	}

}
