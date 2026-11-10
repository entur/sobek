-- Train and CompoundTrain are stored in vehicle_type as subclasses of VehicleType (single table inheritance).

-- The old, never populated train table and its sequences are replaced by the columns below.
DROP TABLE IF EXISTS train_key_values CASCADE;
DROP TABLE IF EXISTS train CASCADE;
DROP SEQUENCE IF EXISTS train_seq;
DROP SEQUENCE IF EXISTS netex_train_seq;

ALTER TABLE vehicle_type
    ADD COLUMN dtype VARCHAR(31),
    ADD COLUMN number_of_cars INTEGER,
    ADD COLUMN train_size_type VARCHAR(255);

-- Every existing row is a plain vehicle type
UPDATE vehicle_type
SET dtype = 'VehicleType';

ALTER TABLE vehicle_type
    ALTER COLUMN dtype SET NOT NULL;

-- Components of a Train
CREATE TABLE train_component
(
    id                             BIGINT NOT NULL PRIMARY KEY,
    netex_id                       VARCHAR(255),
    changed                        TIMESTAMP(6) WITH TIME ZONE,
    created                        TIMESTAMP(6) WITH TIME ZONE,
    from_date                      TIMESTAMP(6) WITH TIME ZONE,
    to_date                        TIMESTAMP(6) WITH TIME ZONE,
    version                        BIGINT NOT NULL,
    label_lang                     VARCHAR(5),
    label_value                    VARCHAR(255),
    description_lang               VARCHAR(5),
    description_value              VARCHAR(255),
    train_id                       BIGINT REFERENCES vehicle_type (id),
    train_element_ref              VARCHAR(255),
    operational_orientation        VARCHAR(255),
    forward_coupling_through_access VARCHAR(255),
    owner_train_id                 BIGINT REFERENCES vehicle_type (id),
    component_order                INTEGER
);

CREATE INDEX idx_train_component_owner ON train_component (owner_train_id);
CREATE INDEX idx_train_component_train ON train_component (train_id);

-- Components of a CompoundTrain
CREATE TABLE train_in_compound_train
(
    id                         BIGINT NOT NULL PRIMARY KEY,
    netex_id                   VARCHAR(255),
    changed                    TIMESTAMP(6) WITH TIME ZONE,
    created                    TIMESTAMP(6) WITH TIME ZONE,
    from_date                  TIMESTAMP(6) WITH TIME ZONE,
    to_date                    TIMESTAMP(6) WITH TIME ZONE,
    version                    BIGINT NOT NULL,
    label_lang                 VARCHAR(5),
    label_value                VARCHAR(255),
    description_lang           VARCHAR(5),
    description_value          VARCHAR(255),
    train_id                   BIGINT REFERENCES vehicle_type (id),
    train_element_type_ref     VARCHAR(255),
    operational_orientation    VARCHAR(255),
    owner_compound_train_id    BIGINT REFERENCES vehicle_type (id),
    component_order            INTEGER
);

CREATE INDEX idx_train_in_compound_train_owner ON train_in_compound_train (owner_compound_train_id);
CREATE INDEX idx_train_in_compound_train_train ON train_in_compound_train (train_id);

-- Hibernate id sequences (sequence per table, increment 10)
CREATE SEQUENCE IF NOT EXISTS train_component_seq START WITH 1 INCREMENT BY 10 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS train_in_compound_train_seq START WITH 1 INCREMENT BY 10 CACHE 1;

-- NeTEx id sequences
CREATE SEQUENCE IF NOT EXISTS netex_train_component_seq START WITH 1 INCREMENT BY 1 CACHE 1;
CREATE SEQUENCE IF NOT EXISTS netex_train_in_compound_train_seq START WITH 1 INCREMENT BY 1 CACHE 1;
