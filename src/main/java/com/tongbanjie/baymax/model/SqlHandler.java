package com.tongbanjie.baymax.model;

public class SqlHandler {
	
	private static ThreadLocal<Sql> sqlHandler = new ThreadLocal<Sql>();
	
	public static Sql get(){
		return sqlHandler.get();
	}
	
	public static void set(Sql sql){
		sqlHandler.set(sql);
	}
	
	public static void clear(){
		sqlHandler.remove();
	}

}
