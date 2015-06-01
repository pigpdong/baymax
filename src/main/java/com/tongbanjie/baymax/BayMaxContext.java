package com.tongbanjie.baymax;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.tongbanjie.baymax.datasources.DataSourceDispatcher;
import com.tongbanjie.baymax.router.IRouteService;
import com.tongbanjie.baymax.router.ITableRule;
import com.tongbanjie.baymax.router.impl.DefaultRouteService;

/**
 * BayMax的上下文对象,一个Spring上下中最好只初始化一个BayMax,你初始化多个也没意义.
 * 他包含了BayMax工作时一些必须的内部组件.
 * 
 * @author dawei
 *
 */
public class BayMaxContext implements InitializingBean,ApplicationListener<ContextRefreshedEvent> {
	
	private DataSourceDispatcher dataSourceDispatcher;
	
	@Autowired
	private List<ITableRule> tableRules;
	
	private IRouteService routeService;
	
	@Autowired(required = true)
	private SqlSessionFactory sqlSessionFactory;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		checkContextInte();
	}
	

	@Override
	public void afterPropertiesSet() throws Exception {
		routeService = new DefaultRouteService(tableRules, dataSourceDispatcher);
		
	}
	
	private void checkContextInte(){
		if(dataSourceDispatcher == null){
			throw new RuntimeException(DataSourceDispatcher.class+" must be inject into "+BayMaxContext.class);
		}
		if(tableRules == null || tableRules.size() == 0){
			throw new RuntimeException("there is no ITableRule find in xml config.");
		}
		// TODO check dispatcher,check default datasource.
	}

	public DataSourceDispatcher getDataSourceDispatcher() {
		return dataSourceDispatcher;
	}

	public void setDataSourceDispatcher(DataSourceDispatcher dataSourceDispatcher) {
		this.dataSourceDispatcher = dataSourceDispatcher;
	}

	public IRouteService getRouteService() {
		return routeService;
	}

	public List<ITableRule> getTableRules() {
		return tableRules;
	}

	public void setTableRules(List<ITableRule> tableRules) {
		this.tableRules = tableRules;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public void setRouteService(IRouteService routeService) {
		this.routeService = routeService;
	}
}
