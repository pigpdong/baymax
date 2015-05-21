package com.tongbanjie.baymax.datasources;

import javax.sql.DataSource;

/**
 * 原始DataSource包装器
 * 
 * @author dawei
 *
 */
public class NativeDataSourceHandler {
	/**
	 * the identity of to-be-exposed DataSource.
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

	/**
	 * we will initialize proper thread pools which stand in front of data
	 * sources as per connection pool size. <br>
	 * usually, they will have same number of objects.<br>
	 * you have to set a proper size for this attribute as per your data source
	 * attributes. In case you forget it, we set a default value with
	 * "number of CPU" * 5.
	 */
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
		NativeDataSourceHandler other = (NativeDataSourceHandler) obj;
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
