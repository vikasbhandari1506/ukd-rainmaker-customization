package org.egov.ukdcustomservice.repository;

import java.util.List;

import org.egov.ukdcustomservice.web.models.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PropertyNotifyRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private String query = "SELECT usr.name, usr.mobilenumber, demand.consumercode, demand.businessservice, property.tenantid, (SUM(dd.taxamount)-SUM(dd.collectionamount)) as pendingamount FROM eg_user as usr"
            + " INNER JOIN eg_pt_owner_v2 as owner ON usr.uuid = owner.userid and usr.mobilenumber IS NOT NULL AND usr.mobilenumber NOT IN ('9999999999') AND usr.type = 'CITIZEN'"
            + " JOIN (SELECT DISTINCT property, assessmentnumber FROM eg_pt_propertydetail_v2) as pd ON owner.propertydetail = pd.assessmentnumber"
            + " INNER JOIN eg_pt_property_v2 as property ON pd.property = property.propertyid AND property.tenantid= ?"
            + " JOIN egbs_demand_v1 as demand ON property.propertyid = demand.consumercode"
            + " JOIN egbs_demanddetail_v1 as dd ON demand.id = dd.demandid GROUP BY usr.name,"
            + " usr.mobilenumber, property.tenantid, demand.consumercode, demand.businessservice;";

    public List<Notifications> getNotifications(String tenantid) {

        return jdbcTemplate.query(query, new Object[] { tenantid },
                (rs, rowNum) -> new Notifications(rs.getString("mobilenumber"), rs.getString("name"),
                        rs.getString("pendingamount"), rs.getString("consumercode"), rs.getString("businessservice"),
                        rs.getString("tenantid")));

    }

}