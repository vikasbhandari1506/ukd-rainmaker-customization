package org.egov.ukdcustomservice.repository;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.ukdcustomservice.models.PropertyCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PropertyCountRowMapper implements ResultSetExtractor<PropertyCount> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public PropertyCount extractData(ResultSet rs) throws SQLException, DataAccessException {

		PropertyCount migrationCount = new PropertyCount();

		while(rs.next()){
			migrationCount = PropertyCount.builder().id(rs.getString("id")).offset(rs.getLong("batch")).limit(rs.getLong("batchsize"))
					.createdTime(rs.getLong("createdtime")).tenantid(rs.getString("tenantid")).recordCount(rs.getLong("recordCount")).build();
		}
		return migrationCount;
	}

}
