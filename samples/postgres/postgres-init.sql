CREATE TABLE public.schema_history (
	"path" varchar(255) NOT NULL,
	updatedate timestamp NOT NULL,
	checksum varchar(255) NULL,
	createdate timestamp NULL,
	deploymentreqno varchar(255) NULL,
	pattern varchar(255) NULL,
	"sequence" int8 NULL,
	"type" varchar(255) NULL,
	"version" int8 NULL,
	CONSTRAINT schema_history_pkey PRIMARY KEY (path, updatedate)
);