ALTER TABLE vehicle_model
    ADD transport_type_id BIGINT;

ALTER TABLE vehicle_model
    ADD CONSTRAINT FK_VEHICLEMODEL_ON_TRANSPORTTYPE FOREIGN KEY (transport_type_id) REFERENCES vehicle_type (id);

ALTER TABLE vehicle_model
    DROP COLUMN transport_type_ref;
