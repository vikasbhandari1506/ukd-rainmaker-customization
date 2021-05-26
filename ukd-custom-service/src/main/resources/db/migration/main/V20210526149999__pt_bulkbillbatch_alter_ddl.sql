ALTER TABLE eg_bulkbill_batch ADD COLUMN tenantid CHARACTER VARYING (256) NOT NULL;
ALTER TABLE eg_bulkbill_batch ADD COLUMN recordCount bigint NOT NULL;