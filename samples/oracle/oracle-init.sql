CREATE TABLE SCHEMA_HISTORY (
PATH VARCHAR2(255 CHAR) NOT NULL ENABLE, 
UPDATEDATE TIMESTAMP (6), 
CHECKSUM VARCHAR2(255 CHAR), 
CREATEDATE TIMESTAMP (6), 
deploymentreqno VARCHAR2(255 CHAR) NULL,
pattern VARCHAR2(255 CHAR) NULL,
SEQUENCE NUMBER(19,0), 
TYPE VARCHAR2(255 CHAR), 
VERSION NUMBER(19,0), 
 PRIMARY KEY (PATH, updatedate)
 );