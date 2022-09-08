package org.egov.ukdcustomservice.repository;

import java.util.List;
import java.util.Set;

import org.egov.ukdcustomservice.web.models.NotificationRequest;
import org.egov.ukdcustomservice.web.models.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PropertyNotifyRepository {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Value("${pt.paypending.search.pagination.default.limit}")
	private Long defaultLimit;

	@Value("${pt.paypending.search.pagination.default.offset}")
	private Long defaultOffset;

	@Value("${pt.paypending.search.pagination.max.search.limit}")
	private Long maxSearchLimit;

	private static final String QUERY = "select property.propertyid as propertyid, (SUM(dd.taxamount)-SUM(dd.collectionamount)) as pendingamount, property.tenantid as tenantid, property.lastmodifiedtime as plastmodifiedtime "
			+ "from egbs_demand_v1 as demand left outer join eg_pt_property as property on property.propertyid = demand.consumercode "
			+ "left outer join egbs_demanddetail_v1 as dd ON demand.id = dd.demandid and  demand.ispaymentcompleted =false ";

	private static final String PAGINATION_WRAPPER = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY plastmodifiedtime DESC, propertyid) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";

	private String addPaginationWrapper(String query, List<Object> preparedStmtList,
			NotificationRequest notificationRequest) {

		Long limit = defaultLimit;
		Long offset = defaultOffset;
		String finalQuery = PAGINATION_WRAPPER.replace("{}", query);

		if (notificationRequest.getLimit() != null && notificationRequest.getLimit() <= maxSearchLimit)
			limit = notificationRequest.getLimit();

		if (notificationRequest.getLimit() != null && notificationRequest.getLimit() > maxSearchLimit)
			limit = maxSearchLimit;

		if (notificationRequest.getOffset() != null)
			offset = notificationRequest.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		for (Object o : preparedStmtList)
			log.info("preparedStmtList:::"+o.toString());
		log.info("finalQuery::::::"+finalQuery);
		return finalQuery;
	}

	public List<Notifications> getNotifications(NotificationRequest notificationRequest,
			List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);
		if (notificationRequest.getTenantId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("property.tenantid=?");
			preparedStmtList.add(notificationRequest.getTenantId());
		}

		Set<String> propertyIds = notificationRequest.getPropertyId();
		if (!CollectionUtils.isEmpty(propertyIds)) {

			addClauseIfRequired(preparedStmtList, builder);
			builder.append("property.propertyid IN (").append(createQuery(propertyIds)).append(")");
			addToPreparedStatementWithUpperCase(preparedStmtList, propertyIds);
		}
		
		addClauseIfRequired(preparedStmtList, builder);
		builder.append("property.status='ACTIVE'");
		
		builder.append(" group by propertyid,property.tenantid,plastmodifiedtime ");
		
		return jdbcTemplate.query(addPaginationWrapper(builder.toString(), preparedStmtList, notificationRequest),
				preparedStmtList.toArray(),
				(rs, rowNum) -> new Notifications(rs.getString("propertyId"), rs.getString("pendingamount"),
						rs.getString("tenantid")));

	}

	private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND ");
		}
	}

	private String createQuery(Set<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatementWithUpperCase(List<Object> preparedStmtList, Set<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id.toUpperCase());
		});
	}

}