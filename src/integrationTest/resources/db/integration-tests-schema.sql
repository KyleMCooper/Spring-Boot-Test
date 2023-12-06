-- TODO: integration tests are intended to be ran on a mysql test container, change the syntax if required
CREATE TABLE IF NOT EXISTS skeletonResource (
	id varchar(100) NOT NULL COMMENT 'skeletonResource id',
	name varchar(100) NOT NULL COMMENT 'skeletonResource name',
	active int NULL DEFAULT 0 COMMENT 'Indicates if the record is active (1) or not (0)',
	CONSTRAINT name_UNIQUE UNIQUE (name),
	CONSTRAINT skeletonResource_PK PRIMARY KEY (id)
)
COMMENT 'skeletonResource table';
