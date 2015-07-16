package com.tongbanjie.baymax.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tongbanjie.baymax.datasource.MultipleDataSource;

/**
 * 表创建器
 * 
 * @author dawei
 *
 */
public abstract class TableCreater {

	private final static Logger logger = LoggerFactory.getLogger(TableCreater.class);

	MultipleDataSource dataSource;

	public void init(MultipleDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 触发自动建表事件
	 * @param suffix
	 */
	public abstract void createTable(String suffix);

	/**
	 * 自动建表
	 * <p>
	 * 表创建成功或已经存在返回true
	 * <p>
	 * 表创建失败返回false
	 * @param tableName
	 * @param likeTableName
	 * @param partition
	 * @return
	 */
	public boolean createTableInDB(String tableName, String likeTableName, String partition) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = dataSource.getDataSourceByName(partition).getConnection();
			stmt = conn.createStatement();
			stmt.execute("CREATE TABLE `" + tableName + "` LIKE `" + likeTableName + "`");
			return true;
		} catch (SQLException e) {
			logger.error("Auto create table error " + tableName + " " + likeTableName + " " + partition + " " + e.getErrorCode(), e);
			if(e.getErrorCode() == 1050){
				return true;
			}
		}finally{
			if(stmt != null){
				try{
					stmt.close();
				}catch(Exception e){
					
				}
			}
			if(conn != null){
				try{
					conn.close();
				}catch(Exception e){
					
				}
			}
		}
		return false;
	}
}
