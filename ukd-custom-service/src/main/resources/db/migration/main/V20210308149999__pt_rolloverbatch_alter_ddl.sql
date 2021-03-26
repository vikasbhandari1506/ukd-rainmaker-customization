ALTER TABLE eg_pt_rolloverbatch ADD COLUMN tenantid CHARACTER VARYING (256) NOT NULL;
ALTER TABLE eg_pt_rolloverbatch ADD COLUMN recordCount bigint NOT NULL;