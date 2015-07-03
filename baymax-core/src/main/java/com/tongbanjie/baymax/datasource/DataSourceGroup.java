package com.tongbanjie.baymax.datasource;

import javax.sql.DataSource;

/**
 * 原始DataSource包装器
 * 
 * 一个包装器里面可能包含四个个DataSource,一个{@link targetDataSource}一个{@link standbyDataSource}
 * 这两个DataSource用来做读写分离。
 * 另外还有两个DataSource {@link targetDetectorDataSource},{@link standbyDetectorDataSource}用来做心跳检测。
 * 之所以要分离出转门用作心跳检测的DataSource是应为用来执行业务的DataSource链接有可能被用完，但是这时候数据库还是活的，导致心跳检测不准确.
 * 
 * @author dawei
 *
 */
public class DataSourceGroup {
	
	/**
	 * 命名,唯一标识
	 */
	private String identity;
	/**
	 * active data source
	 */
	private DataSource targetDataSource;
	/**
	 * detecting data source for active data source
	 * 专为检测而存在的dataSource，为了避免业务的dataSource被占满而导致无法收到检测心跳。
	 */
	private DataSource targetDetectorDataSource;
	/**
	 * standby data source
	 */
	private DataSource standbyDataSource;
	/**
	 * detecting datasource for standby data source
	 */
	private DataSource standbyDetectorDataSource;

	private int poolSize = Runtime.getRuntime().availableProcessors() * 5;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public DataSource getTargetDataSource() {
		return targetDataSource;
	}

	public void setTargetDataSource(DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	public DataSource getTargetDetectorDataSource() {
		return targetDetectorDataSource;
	}

	public void setTargetDetectorDataSource(DataSource targetDetectorDataSource) {
		this.targetDetectorDataSource = targetDetectorDataSource;
	}

	public DataSource getStandbyDataSource() {
		return standbyDataSource;
	}

	public void setStandbyDataSource(DataSource standbyDataSource) {
		this.standbyDataSource = standbyDataSource;
	}

	public DataSource getStandbyDetectorDataSource() {
		return standbyDetectorDataSource;
	}

	public void setStandbyDetectorDataSource(DataSource standbyDetectorDataSource) {
		this.standbyDetectorDataSource = standbyDetectorDataSource;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getPoolSize() {
		return poolSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataSourceGroup other = (DataSourceGroup) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CobarDataSourceDescriptor [identity=" + identity + ", poolSize=" + poolSize + ", standbyDataSource=" + standbyDataSource
				+ ", standbyDetectorDataSource=" + standbyDetectorDataSource + ", targetDataSource=" + targetDataSource
				+ ", targetDetectorDataSource=" + targetDetectorDataSource + "]";
	}

}
