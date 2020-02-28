package org.sourcehot.spring.dao.impl;

import org.sourcehot.spring.dao.HsLog;
import org.sourcehot.spring.dao.HsLogDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class HsLogDaoImpl extends JdbcDaoSupport implements HsLogDao {


	@Override
	public List<HsLog> findAll() {
		return this.getJdbcTemplate().query("select * from hs_log", new HsLogRowMapper());
	}

	@Override
	public void save(HsLog hsLog) {
		this.getJdbcTemplate().update("insert into hs_log (SOURCE) values(?)"
				,

				hsLog.getSource());
	}

	@Override
	public HsLog byId(Integer id) {
		HsLog hsLog = this.getJdbcTemplate().queryForObject(
				"select source from hs_log  where id = ?", new Object[]{id}, HsLog.class);
		return hsLog;
	}

	class HsLogRowMapper implements RowMapper<HsLog> {

		public HsLog mapRow(ResultSet rs, int rowNum) throws SQLException {

			HsLog log = new HsLog();
			log.setId(rs.getInt("id"));
			log.setSource(rs.getString("source"));
			return log;
		}

	}
}
