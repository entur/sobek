DROP TABLE IF EXISTS id_generator;
CREATE TABLE id_generator(table_name VARCHAR (50), id_value bigint, CONSTRAINT id_constraint UNIQUE (table_name, id_value));

CREATE index table_name_index ON id_generator(table_name);
CREATE index id_value_index ON id_generator(id_value);
