-- A fleet can be shared by its data owner with everyone, or with a set of organisations. The organisations that can see
-- a shared fleet also see its vehicles and vehicle types (read only). The sharing belongs to a version of the fleet, and is
-- carried to the next version.
ALTER TABLE fleet
    ADD COLUMN shared_with_all BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE fleet_shared_with
(
    fleet_id         BIGINT       NOT NULL REFERENCES fleet (id),
    organisation_ref VARCHAR(255) NOT NULL,
    PRIMARY KEY (fleet_id, organisation_ref)
);

CREATE INDEX idx_fleet_shared_with_organisation ON fleet_shared_with (organisation_ref);
