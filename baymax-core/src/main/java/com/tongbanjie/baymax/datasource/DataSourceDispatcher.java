package com.tongbanjie.baymax.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import com.tongbanjie.baymax.router.TableRule;

public class DataSourceDispatcher {
	private Set<DataSourceGroup> dataSourceGroupSet = new HashSet<DataSourceGroup>();
	private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
	private Set<DataSource> dataSourceSet = new HashSet<DataSource>();
	private DataSource defaultDataSource;
	@Autowired
	private List<TableRule> tableRules;

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void init() throws Exception {
		if (CollectionUtils.isEmpty(dataSourceGroupSet)) {
			return;
		}
		for (DataSourceGroup nativeHandler : dataSourceGroupSet) {
			Assert.hasText(nativeHandler.getIdentity());
			Assert.notNull(nativeHandler.getTargetDataSource());
			DataSource dataSourceToUse = nativeHandler.getTargetDataSource();
			//DataSource ds = new LazyConnectionDataSourceProxy(dataSourceToUse);
			DataSource ds = dataSourceToUse;
			dataSources.put(nativeHandler.getIdentity(), ds);
			dataSourceSet.add(ds);
			if(defaultDataSource == null){
				// TODO default need be setting in spring xml config
				defaultDataSource = nativeHandler.getTargetDataSource();
			}
		}
		
	}
	
	public DataSource getDefaultDataSource(){
		return this.defaultDataSource;
	}
	
	public DataSource getDataSourceByName(String parttionName){
		return dataSources.get(parttionName);
	}

	public Set<DataSource> getDataSourceSet() {
		return dataSourceSet;
	}

	public List<TableRule> getTableRules() {
		return tableRules;
	}

	public void setTableRules(List<TableRule> tableRules) {
		this.tableRules = tableRules;
	}

	public Set<DataSourceGroup> getDataSourceGroupSet() {
		return dataSourceGroupSet;
	}

	public void setDataSourceGroupSet(Set<DataSourceGroup> dataSourceGroupSet) {
		this.dataSourceGroupSet = dataSourceGroupSet;
	}

	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public void setDataSourceSet(Set<DataSource> dataSourceSet) {
		this.dataSourceSet = dataSourceSet;
	}

	public void setDefaultDataSource(DataSource defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}
}
