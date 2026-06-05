ALTER TABLE deck_plan
    ADD data_owner_ref VARCHAR(255);

ALTER TABLE train
    ADD data_owner_ref VARCHAR(255);

ALTER TABLE vehicle
    ADD data_owner_ref VARCHAR(255);

ALTER TABLE vehicle_type
    ADD data_owner_ref VARCHAR(255);