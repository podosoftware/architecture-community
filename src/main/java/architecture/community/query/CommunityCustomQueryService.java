package architecture.community.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import architecture.community.query.dao.CustomQueryJdbcDao;
import architecture.community.util.CommunityContextHelper;
import architecture.community.web.model.json.DataSourceRequest;
import architecture.ee.jdbc.sqlquery.factory.Configuration;
import architecture.ee.jdbc.sqlquery.mapping.BoundSql;
import architecture.ee.spring.jdbc.ExtendedJdbcDaoSupport;

public class CommunityCustomQueryService implements CustomQueryService {

	private Logger logger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	@Qualifier("sqlConfiguration")
	private Configuration sqlConfiguration;

	@Autowired
	@Qualifier("customQueryJdbcDao")
	private CustomQueryJdbcDao customQueryJdbcDao;
	
	
	public CommunityCustomQueryService() {
		
	}
	
	
	public <T> T queryForObject (DataSourceRequest dataSourceRequest, Class<T> requiredType) {				
		BoundSql sqlSource = customQueryJdbcDao.getBoundSqlWithAdditionalParameter(dataSourceRequest.getStatement(), getAdditionalParameter(dataSourceRequest));		
		if( dataSourceRequest.getParameters().size() > 0 )
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), requiredType, getSqlParameterValues( dataSourceRequest.getParameters() ) );
		else	
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), requiredType );
	}
	
	public <T> T queryForObject (DataSourceRequest dataSourceRequest, RowMapper<T> rowmapper) {			
		BoundSql sqlSource = customQueryJdbcDao.getBoundSqlWithAdditionalParameter(dataSourceRequest.getStatement(), getAdditionalParameter(dataSourceRequest));
		if( dataSourceRequest.getParameters().size() > 0 )
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), rowmapper, getSqlParameterValues( dataSourceRequest.getParameters() ) );
		else	
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), rowmapper );
	}
	
	public Map<String, Object> queryForMap (DataSourceRequest dataSourceRequest) {				
		BoundSql sqlSource = customQueryJdbcDao.getBoundSqlWithAdditionalParameter(dataSourceRequest.getStatement(), getAdditionalParameter(dataSourceRequest));		
		if( dataSourceRequest.getParameters().size() > 0 )
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForMap( sqlSource.getSql(), getSqlParameterValues( dataSourceRequest.getParameters() ) );
		else	
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForMap( sqlSource.getSql() );
	}

	
	public <T> List<T> list(DataSourceRequest dataSourceRequest, RowMapper<T> rowmapper) {			
		BoundSql sqlSource = customQueryJdbcDao.getBoundSqlWithAdditionalParameter(dataSourceRequest.getStatement(), getAdditionalParameter(dataSourceRequest));
		if( dataSourceRequest.getPageSize() > 0 ){	
			if( dataSourceRequest.getParameters().size() > 0 )
				return customQueryJdbcDao.getExtendedJdbcTemplate().query( sqlSource.getSql(), dataSourceRequest.getSkip(),  dataSourceRequest.getPageSize(), rowmapper , getSqlParameterValues( dataSourceRequest.getParameters() ) );		
			else
				return customQueryJdbcDao.getExtendedJdbcTemplate().query( sqlSource.getSql(), dataSourceRequest.getSkip(),  dataSourceRequest.getPageSize(), rowmapper );					
		}else {
			if( dataSourceRequest.getParameters().size() > 0 )
				return customQueryJdbcDao.getExtendedJdbcTemplate().query(sqlSource.getSql(), rowmapper, getSqlParameterValues( dataSourceRequest.getParameters() ) );
			else	
				return customQueryJdbcDao.getExtendedJdbcTemplate().query(sqlSource.getSql(), rowmapper );
		}
	}
	
	public <T> T list(DataSourceRequest dataSourceRequest, ResultSetExtractor<T> extractor) {				
		logger.debug("Paging not support yet.");		
		BoundSql sqlSource = customQueryJdbcDao.getBoundSqlWithAdditionalParameter(dataSourceRequest.getStatement(), getAdditionalParameter(dataSourceRequest));
		if( dataSourceRequest.getParameters().size() > 0 )
			return customQueryJdbcDao.getExtendedJdbcTemplate().query(sqlSource.getSql(), extractor, getSqlParameterValues( dataSourceRequest.getParameters() ) );
		else	
			return customQueryJdbcDao.getExtendedJdbcTemplate().query(sqlSource.getSql(), extractor );
	}	
	
	

	public List<Map<String, Object>> list( String statement, List<ParameterValue> values) {
		if (values.size() > 0)
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForList(customQueryJdbcDao.getBoundSql(statement).getSql(), getSqlParameterValues(values).toArray());
		else
			return customQueryJdbcDao.getExtendedJdbcTemplate().queryForList(customQueryJdbcDao.getBoundSql(statement).getSql());
	}
	
	public List<Map<String, Object>> list(String source, String statement, List<ParameterValue> values) {
		DataSource dataSource = CommunityContextHelper.getComponent(source, DataSource.class);
		ExtendedJdbcDaoSupport dao = new ExtendedJdbcDaoSupport(sqlConfiguration);
		dao.setDataSource(dataSource);
		if (values.size() > 0)
			return dao.getExtendedJdbcTemplate().queryForList(dao.getBoundSql(statement).getSql(), getSqlParameterValues(values).toArray());
		else
			return dao.getExtendedJdbcTemplate().queryForList(dao.getBoundSql(statement).getSql());
	}

	public <T> List<T> list( String statement, List<ParameterValue> values, RowMapper<T> rowmapper) {
		if (values.size() > 0)
			return customQueryJdbcDao.getExtendedJdbcTemplate().query(customQueryJdbcDao.getBoundSql(statement).getSql(), rowmapper, getSqlParameterValues(values).toArray());
		else
			return customQueryJdbcDao.getExtendedJdbcTemplate().query(customQueryJdbcDao.getBoundSql(statement).getSql(), rowmapper);
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public <T> T execute(DaoCallback<T> action) throws DataAccessException {
		Assert.notNull(action, "Callback object must not be null");
		T result = action.process(customQueryJdbcDao);
		return result;
	}	
	
	/**
	 * 외부에서 전달된 인자들을 스프링이 인식하는 형식의 값을 변경하여 처리한다.
	 * @param values
	 * @return
	 */
	private List<SqlParameterValue> getSqlParameterValues (List<ParameterValue> values ){
		ArrayList<SqlParameterValue> al = new ArrayList<SqlParameterValue>();	
		for( ParameterValue v : values)
		{
			al.add(new SqlParameterValue(v.getJdbcType(), v.getValueText()) );
		}
		return al;
	}
	
	/**
	 * 다이나믹 쿼리 처리를 위하여 필요한 파라메터들을 Map 형식의 데이터로 생성한다. 
	 * @param dataSourceRequest
	 * @return
	 */
	protected Map<String, Object> getAdditionalParameter( DataSourceRequest dataSourceRequest ){
		Map<String, Object> additionalParameter = new HashMap<String, Object>();
		additionalParameter.put("filter", dataSourceRequest.getFilter());
		additionalParameter.put("sort", dataSourceRequest.getSort());		
		additionalParameter.put("data", dataSourceRequest.getData());		
		additionalParameter.put("user", dataSourceRequest.getUser());		
		return additionalParameter;
	}
}
