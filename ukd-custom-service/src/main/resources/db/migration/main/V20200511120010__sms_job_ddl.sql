
CREATE TABLE eg_cstm_bulk_sms_job (
    id bigint NOT NULL,
    status character varying(50),
    createddate timestamp,
    lastmodifieddate timestamp,
    createdby bigint,
    lastmodifiedby bigint,
    tenantid character varying(256) not null
);

CREATE SEQUENCE eg_cstm_bulk_sms_job
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;