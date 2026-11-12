-- Fleet: a group of vehicles (NeTEx Fleet). Versioned like a vehicle.
-- The members are vehicle rows and the transport types are vehicle type rows. When a vehicle or a vehicle type gets a
-- new version, the references in the two join tables follow it to the new row.
CREATE TABLE fleet
(
    id                BIGINT NOT NULL PRIMARY KEY,
    netex_id          VARCHAR(255),
    changed           TIMESTAMP(6) WITH TIME ZONE,
    created           TIMESTAMP(6) WITH TIME ZONE,
    from_date         TIMESTAMP(6) WITH TIME ZONE,
    to_date           TIMESTAMP(6) WITH TIME ZONE,
    version           BIGINT NOT NULL,
    changed_by        VARCHAR(255),
    version_comment   VARCHAR(255),
    name_lang         VARCHAR(5),
    name_value        VARCHAR(255),
    short_name_lang   VARCHAR(5),
    short_name_value  VARCHAR(255),
    description_lang  VARCHAR(5),
    description_value VARCHAR(255),
    type              VARCHAR(255),
    value             VARCHAR(255),
    data_owner_ref    VARCHAR(255)
);

CREATE INDEX idx_fleet_netex_id ON fleet (netex_id);

CREATE TABLE fleet_key_values
(
    fleet_id      BIGINT NOT NULL REFERENCES fleet (id),
    key_values_id BIGINT NOT NULL UNIQUE REFERENCES key_value (id)
);

CREATE TABLE fleet_members
(
    fleet_id   BIGINT NOT NULL REFERENCES fleet (id),
    members_id BIGINT NOT NULL REFERENCES vehicle (id),
    PRIMARY KEY (fleet_id, members_id)
);

CREATE INDEX idx_fleet_members_member ON fleet_members (members_id);

CREATE TABLE fleet_transport_types
(
    fleet_id           BIGINT NOT NULL REFERENCES fleet (id),
    transport_types_id BIGINT NOT NULL REFERENCES vehicle_type (id),
    PRIMARY KEY (fleet_id, transport_types_id)
);

CREATE INDEX idx_fleet_transport_types_type ON fleet_transport_types (transport_types_id);

-- Hibernate id sequence (sequence per table, increment 10)
CREATE SEQUENCE IF NOT EXISTS fleet_seq START WITH 1 INCREMENT BY 10 CACHE 1;

-- NeTEx id sequence
CREATE SEQUENCE IF NOT EXISTS netex_fleet_seq START WITH 1 INCREMENT BY 1 CACHE 1;
