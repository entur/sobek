ALTER TABLE train
    ADD form_drag_coefficient DECIMAL;

ALTER TABLE train
    ADD fuel_types VARCHAR(255);

ALTER TABLE train
    ADD hybrid_category SMALLINT;

ALTER TABLE train
    ADD maximum_engine_effectkw DECIMAL;

ALTER TABLE train
    ADD propulsion_types VARCHAR(255);

ALTER TABLE train
    ADD roll_resistance_coefficient DECIMAL;

ALTER TABLE vehicle_type
    ADD form_drag_coefficient DECIMAL;

ALTER TABLE vehicle_type
    ADD hybrid_category SMALLINT;

ALTER TABLE vehicle_type
    ADD maximum_engine_effectkw DECIMAL;

ALTER TABLE vehicle_type
    ADD roll_resistance_coefficient DECIMAL;
