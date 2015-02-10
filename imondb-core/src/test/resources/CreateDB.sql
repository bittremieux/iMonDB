-- CREATE DATABASE iMonDB;

USE root;

DROP TABLE IF EXISTS imon_value;
DROP TABLE IF EXISTS imon_metadata;
DROP TABLE IF EXISTS imon_run;
DROP TABLE IF EXISTS imon_instrument_properties;
DROP TABLE IF EXISTS imon_property;
DROP TABLE IF EXISTS imon_event;
DROP TABLE IF EXISTS imon_instrument;
DROP TABLE IF EXISTS imon_cv;

--
-- Table structure for table `imon_cv`
--

CREATE TABLE imon_cv (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  label varchar(20) NOT NULL,
  name varchar(200) NOT NULL,
  uri varchar(200) NOT NULL,
  version varchar(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (label)
) DEFAULT CHARACTER SET utf8;


--
-- Table structure for table `imon_instrument`
--

CREATE TABLE imon_instrument (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  type varchar(10) NOT NULL,
  l_imon_cv_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (name),
  CONSTRAINT FOREIGN KEY (l_imon_cv_id) REFERENCES imon_cv (id)
) DEFAULT CHARACTER SET utf8; 

--
-- Table structure for table `imon_event`
--

CREATE TABLE imon_event (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  attachment longblob,
  attachment_name varchar(255) DEFAULT NULL,
  eventdate datetime NOT NULL,
  extra text,
  problem text,
  solution text,
  type varchar(255) NOT NULL,
  l_imon_instrument_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (l_imon_instrument_id,eventdate),
  FOREIGN KEY (l_imon_instrument_id) REFERENCES imon_instrument (id)
) DEFAULT CHARACTER SET utf8;

--
-- Table structure for table `imon_property`
--

CREATE TABLE imon_property (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  accession varchar(255) NOT NULL,
  isnumeric bit(1) NOT NULL,
  name varchar(200) NOT NULL,
  type varchar(20) NOT NULL,
  l_imon_cv_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (accession),
  FOREIGN KEY (l_imon_cv_id) REFERENCES imon_cv (id)
) DEFAULT CHARACTER SET utf8;


--
-- Table structure for table `imon_instrument_properties`
--

CREATE TABLE imon_instrument_properties (
  l_imon_instrument_id bigint(20) NOT NULL,
  l_imon_property_id bigint(20) NOT NULL,
  PRIMARY KEY (l_imon_instrument_id,l_imon_property_id),
  FOREIGN KEY (l_imon_instrument_id) REFERENCES imon_instrument (id),
  FOREIGN KEY (l_imon_property_id) REFERENCES imon_property (id)
) DEFAULT CHARACTER SET utf8;


--
-- Table structure for table `imon_run`
--

CREATE TABLE imon_run (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  sampledate datetime NOT NULL,
  storage_name varchar(255) NOT NULL,
  l_imon_instrument_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (l_imon_instrument_id,name),
  FOREIGN KEY (l_imon_instrument_id) REFERENCES imon_instrument (id)
) DEFAULT CHARACTER SET utf8;

--
-- Table structure for table `imon_metadata`
--

CREATE TABLE imon_metadata (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  value varchar(100) NOT NULL,
  l_imon_run_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (l_imon_run_id) REFERENCES imon_run (id)
) DEFAULT CHARACTER SET utf8;


--
-- Table structure for table `imon_value`
--

CREATE TABLE imon_value (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  firstvalue varchar(200) DEFAULT NULL,
  max double DEFAULT NULL,
  mean double DEFAULT NULL,
  median double DEFAULT NULL,
  min double DEFAULT NULL,
  n int(11) DEFAULT NULL,
  n_diffvalues int(11) DEFAULT NULL,
  q1 double DEFAULT NULL,
  q3 double DEFAULT NULL,
  sd double DEFAULT NULL,
  l_imon_property_id bigint(20) NOT NULL,
  l_imon_run_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (l_imon_run_id) REFERENCES imon_run (id),
  FOREIGN KEY (l_imon_property_id) REFERENCES imon_property (id)
) DEFAULT CHARACTER SET utf8;

