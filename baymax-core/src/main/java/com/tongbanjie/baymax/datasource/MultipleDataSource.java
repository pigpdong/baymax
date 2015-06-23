package com.tongbanjie.baymax.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.tongbanjie.baymax.jdbc.TConnection;
import com.tongbanjie.baymax.router.RouteService;
import com.tongbanjie.baymax.router.impl.DefaultRouteService;

/**
 * DataSource分发器
 * 
 * 一个原始的DataSource被包装为了一个{@link DataSourceGroup}
 * 这个类主要个根据DataSource的identity返回对应的{@link DataSourceGroup}
 * 
 * @author dawei
 *
 */
public class MultipleDataSource extends DataSourceDispatcher implements DataSource{
	private RouteService routeService;
	
	public void init() throws Exception{
		super.init();
		routeService = new DefaultRouteService(super.getTableRules(), this);
	}

	public RouteService getRouteService() {
		return routeService;
	}

	//----------------------------------------------------------------//
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		return;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
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

	@Override
	public Connection getConnection() throws SQLException {
		return new TConnection(routeService, this);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/*----------------------------------------------------------------*/
	
	/**
	 * 以分区名返回一个数据源
	 */
	public DataSource getDataSourceByName(String parttionName){
		return super.getDataSourceByName(parttionName);
	}
	
	/**
	 * 在这个分区对应的数据源上打开一个Connection
	 * @param parttionName
	 * @return
	 */
	public Connection getRealConnection(String parttionName){
		try {
			return getDataSourceByName(parttionName).getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 在默认数据源上打开一个Connection
	 * @return
	 */
	public Connection getDefaultConnection(){
		try {
			return super.getDefaultDataSource().getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
