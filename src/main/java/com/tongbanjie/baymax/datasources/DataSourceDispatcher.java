package com.tongbanjie.baymax.datasources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * DataSource分发器
 * 
 * @author dawei
 *
 */
public class DataSourceDispatcher implements InitializingBean {
	private Set<NativeDataSourceHandler> nativeDataSourceHandlerSet = new HashSet<NativeDataSourceHandler>();
	private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
	private DataSource defaultDataSource;

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void afterPropertiesSet() throws Exception {
		if (CollectionUtils.isEmpty(nativeDataSourceHandlerSet)) {
			return;
		}
		for (NativeDataSourceHandler nativeHandler : getNativeDataSourceHandlerSet()) {
			Assert.hasText(nativeHandler.getIdentity());
			Assert.notNull(nativeHandler.getTargetDataSource());
			DataSource dataSourceToUse = nativeHandler.getTargetDataSource();
			dataSources.put(nativeHandler.getIdentity(), new LazyConnectionDataSourceProxy(dataSourceToUse));
			if(defaultDataSource == null){
				// TODO default need be setting in spring xml config
				defaultDataSource = nativeHandler.getTargetDataSource();
			}
		}
	}
	
	public DataSource getDefaultDataSource(){
		return this.defaultDataSource;
	}
	
	public DataSource getDataSourceWhithParttionName(String parttionName){
		return dataSources.get(parttionName);
	}

	public Set<NativeDataSourceHandler> getNativeDataSourceHandlerSet() {
		return nativeDataSourceHandlerSet;
	}

	public void setNativeDataSourceHandlerSet(Set<NativeDataSourceHandler> nativeDataSourceHandlerSet) {
		this.nativeDataSourceHandlerSet = nativeDataSourceHandlerSet;
	}
}
