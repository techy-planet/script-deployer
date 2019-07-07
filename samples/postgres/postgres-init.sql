CREATE TABLE public.schema_history (
	"path" varchar(255) NOT NULL,
	checksum varchar(255) NULL,
	create_date timestamp NULL,
	"sequence" int8 NULL,
	"type" varchar(255) NULL,
	update_date timestamp NULL,
	"version" int8 NULL,
	CONSTRAINT schema_history_pkey PRIMARY KEY (path)
);