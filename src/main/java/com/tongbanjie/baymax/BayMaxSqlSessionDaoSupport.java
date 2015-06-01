package com.tongbanjie.baymax;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;

/**
 * 使用BayMax的入口.
 * 你的Dao需要基础这个类,这个就能自动注入一个实现了Mybatis{@link SqlSession}的{@link SqlSessionTemplate}对象
 * 使用这个对应就和使用原生的Mybatis一样。
 * 
 * @author dawei
 *
 */
public abstract class BayMaxSqlSessionDaoSupport extends DaoSupport{

	  private SqlSessionTemplate sqlSession;

	  private boolean externalSqlSession;
	  
	  @Autowired(required = true)
	  private BayMaxContext context;

	  @Autowired(required = false)
	  public final void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
	    if (!this.externalSqlSession) {
	      this.sqlSession = new SqlSessionTemplate(sqlSessionFactory, context.getDataSourceDispatcher(), context.getRouteService());
	    }
	  }

	  @Autowired(required = false)
	  public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
	    this.sqlSession = sqlSessionTemplate;
	    this.externalSqlSession = true;
	  }

	  /**
	   * Users should use this method to get a SqlSession to call its statement methods
	   * This is SqlSession is managed by spring. Users should not commit/rollback/close it
	   * because it will be automatically done.
	   *
	   * @return Spring managed thread safe SqlSession
	   */
	  public final SqlSessionTemplate getSqlSession() {
	    return this.sqlSession;
	  }

	  /**
	   * {@inheritDoc}
	   */
	  protected void checkDaoConfig() {
	    //notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
	  }
}
