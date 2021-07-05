package org.egov.ukdcustomservice.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DcbRefreshService {

	@Autowired
	private JdbcTemplate jdbcTemplate; 
	
	public static final String tenantsQuery="select distinct tenantid from eg_pt_property";
	public static final String deleteQuery="delete from  dcb where propertyid in "+
	" ("
	+ "select propertyid from eg_pt_property prop,egbs_demand_v1 demand, egbs_demanddetail_v1 dd " +
	" where demand.id=dd.demandid and demand.consumercode=prop.propertyid and prop.tenantid=':tenantId' and "+
    " ( "
    + "to_timestamp(dd.lastmodifiedtime/1000)>(select case when max(updatedtime) is not null then max(updatedtime) else '2019-04-01' end from dcb where tenantid=':tenantId') "+
    " OR " + 
    " to_timestamp(prop.lastmodifiedtime/1000)>(select case when max(updatedtime) is not null then max(updatedtime) else '2019-04-01' end from dcb where tenantid=':tenantId') "
    + ") "
    + ")" +
    " and tenantid=':tenantId';";
	
	
	public boolean refresh(String tenant)
	{
 
		log.info("Refresh initiated  for "+tenant);
		String insertQuery="insert into dcb (propertyid ,oldpropertyid ,doorno ,mohalla ,propertytype ,createddate ,usage "+
				" ,tenantid ,ownernamemobile ,currenttax ,currentarv ,arreartax ,penaltytax ,rebate ,totaltax ,currentcollected,arrearcollected ,penaltycollected,totalcollected ,updatedtime )"+
				"SELECT distinct prop.propertyid AS propertyid,"+
				" prop.oldpropertyid AS oldpropertyid,"+
				" address.doorno AS doorno,"+
				" msg.message AS mohalla,"+
				" prop.propertytype AS propertytype,"+
				" prop.createdtime AS createddate,"+
				" prop.usagecategory AS usage,"+
				" prop.tenantid AS tenantid,"+
				" ("+
				" SELECT u.NAME"+
				" ||','"+
				" || u.mobilenumber"+
				" FROM eg_pt_owner po,"+
				" eg_user u"+
				" WHERE po.userid=u.uuid"+
				" AND po.propertyid =prop.id limit 1) AS ownernamemobile ,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.taxamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TAX',"+
				" 'SWATCHATHA_TAX',"+
				" 'PT_ROUNDOFF' )"+
				" AND ("+
				" extract(epoch FROM now())*1000 BETWEEN demand.taxperiodfrom AND demand.taxperiodto )"+
				" AND demand.consumercode = prop.propertyid), 0) currenttax,"+
				" COALESCE("+
				" ( "+
				" SELECT sum(unit.arv)"+
				" FROM eg_pt_unit unit "+
				" WHERE unit.propertyid=prop.id),0) currentarv,"+
				" COALESCE("+
				" ("+
				" "+
				" SELECT sum(dd.taxamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TAX',"+
				" 'SWATCHATHA_TAX',"+
				" 'PT_ROUNDOFF' )"+
				" AND ("+
				" extract(epoch FROM now())*1000 NOT BETWEEN demand.taxperiodfrom AND demand.taxperiodto )"+
				" AND demand.consumercode = prop.propertyid), 0) arreartax,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.taxamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TIME_INTEREST',"+
				" 'PT_TIME_PENALTY',"+
				" 'PT_LATE_ASSESSMENT_PENALTY' )"+
				" AND demand.consumercode = prop.propertyid), 0) penaltytax,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.taxamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode LIKE '%REBATE%'"+
				" AND demand.consumercode = prop.propertyid), 0) rebate,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.taxamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND demand.consumercode = prop.propertyid), 0) totaltax,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.collectionamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TAX',"+
				" 'SWATCHATHA_TAX',"+
				" 'PT_ROUNDOFF' )"+
				" AND ("+
				" extract(epoch FROM now())*1000 BETWEEN demand.taxperiodfrom AND demand.taxperiodto )"+
				" AND demand.consumercode = prop.propertyid), 0) currentcollected,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.collectionamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TAX',"+
				" 'SWATCHATHA_TAX',"+
				" 'PT_ROUNDOFF' )"+
				" AND ("+
				" extract(epoch FROM now())*1000 NOT BETWEEN demand.taxperiodfrom AND demand.taxperiodto )"+
				" AND demand.consumercode = prop.propertyid), 0) arrearcollected,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.collectionamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode IN ( 'PT_TIME_INTEREST',"+
				" 'PT_TIME_PENALTY',"+
				" 'PT_LATE_ASSESSMENT_PENALTY' )"+
				" AND demand.consumercode = prop.propertyid), 0) penaltycollected,"+
				" COALESCE("+
				" ("+
				" SELECT sum(dd.collectionamount)"+
				" FROM egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd"+
				" WHERE demand.tenantid = dd.tenantid and businessservice ='PT' AND prop.tenantid= :tenantId"+
				" AND demand.id = dd.demandid"+
				" AND dd.taxheadcode NOT LIKE '%REBATE%'"+
				" AND demand.consumercode = prop.propertyid), 0) totalcollected ,"+
				" now() AS updatedtime"+
				"FROM eg_pt_property prop,"+
				" eg_pt_address address,"+
				" egbs_demand_v1 demand,"+
				" egbs_demanddetail_v1 dd,"+
				" message msg"+
				"WHERE prop.id = address.propertyid"+
				"AND demand.id=dd.demandid and demand.consumercode=prop.propertyid and demand.businessservice ='PT' "+
				"AND ( "+ "to_timestamp(dd.lastmodifiedtime/1000)>(select case when max(updatedtime) is not null then max(updatedtime) else '2019-04-01' end from dcb where tenantid=':tenantId')"
				" OR " + 
				" to_timestamp(prop.lastmodifiedtime/1000)>(select case when max(updatedtime) is not null then max(updatedtime) else '2019-04-01' end from dcb where tenantid=':tenantId') )"+
				"AND upper(replace(address.tenantid, '.', '_'))"+
				" || '_REVENUE_'"+
				" || address.locality = msg.code"+
				"AND msg.locale = 'en_IN'"+
				" AND msg.tenantid=prop.tenantid"+
				"AND prop.tenantid = address.tenantid"+
				"AND prop.tenantid= :tenantId" 
				+ ";";
		
		String deletedQuery=	deleteQuery.replaceAll(":tenantId", tenant);
	  String	insertedQuery=	insertQuery.replaceAll(":tenantId", tenant);
		log.info("deleted existing for "+tenant +" deletedQuery : "+deletedQuery);

		jdbcTemplate.execute(deletedQuery);
		log.info("insert query for "+tenant +" insert : "+insertedQuery);
		
		jdbcTemplate.execute(insertedQuery );
		log.info("refresh completed for "+tenant);


		return true;
	}
	

	public List<String> getTenants() {
		 List<String> tenants;
		 tenants = jdbcTemplate.queryForList(tenantsQuery, String.class);
		 return tenants;
		 
		
	}
	
	


}
