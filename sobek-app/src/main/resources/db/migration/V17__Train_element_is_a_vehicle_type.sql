-- TrainElement is stored in vehicle_type as a subclass of VehicleType, like Train and CompoundTrain.
-- Its size, capacity and equipment fields are the ones a vehicle type already has; only the kind of element is new.
ALTER TABLE vehicle_type
    ADD COLUMN train_element_type VARCHAR(255);

-- A train component places a train element, which is now an object of its own and not an id held as text.
-- The text column is dropped: components saved so far only held the id of an element that Sobek did not store.
ALTER TABLE train_component
    DROP COLUMN train_element_ref,
    ADD COLUMN train_element_id BIGINT REFERENCES vehicle_type (id);

CREATE INDEX idx_train_component_train_element ON train_component (train_element_id);
