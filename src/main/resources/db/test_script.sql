-- TODO: Please use the target database syntax to add comments on the table and columns
CREATE TABLE IF NOT EXISTS skeletonResource (
	id varchar(100) NOT NULL COMMENT 'skeletonResource id',
	name varchar(100) NOT NULL COMMENT 'skeletonResource name',
	active int NULL DEFAULT 0 COMMENT 'Indicates if the record is active (1) or not (0)',
	CONSTRAINT name_UNIQUE UNIQUE (name),
	CONSTRAINT skeletonResource_PK PRIMARY KEY (id)
);

COMMENT ON TABLE skeletonResource IS 'skeletonResource table';
