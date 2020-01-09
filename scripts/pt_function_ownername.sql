create or replace function pt_ownername(v_propdetid in character varying(64), in_tenantid in character varying(256), in_onlyname in boolean)  
returns varchar  as  $$
declare
	users eg_user%ROWTYPE;
	v_owner_mobile character varying(4000);
begin  
	--raise notice 'pt_ownername v_propdetid, in_tenantid, in_onlyname : (% % %)',v_propdetid, in_tenantid, in_onlyname;
	for users in (select u.* from eg_pt_owner_v2 po, eg_user u where po.tenantid=in_tenantid and po.userid=u.uuid and po.propertydetail=v_propdetid)
	loop 
		begin
			--raise notice 'pt_ownername users.name, users.mobilenumber : (% %)',users.name, users.mobilenumber;
			if v_owner_mobile <> '' then
			if in_onlyname then
				v_owner_mobile := v_owner_mobile || ',' || users.name;
			else
				v_owner_mobile := v_owner_mobile || ',' || users.name || '|' || users.mobilenumber;
			end if;
			else
			if in_onlyname then
				v_owner_mobile := users.name;
			else
				v_owner_mobile := users.name || '|' || users.mobilenumber;
			end if;
			
			end if;
		exception
		when no_data_found then
		null;   
		end;
	end loop;
	--raise notice 'pt_ownername v_owner_mobile : (%)',v_owner_mobile;
	return v_owner_mobile;   
end; 
$$ language plpgsql;
