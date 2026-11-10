-- In NeTEx, the content of a TrainComponent is a train element (the TrainRef beside it is only the owning train),
-- and a TrainInCompoundTrain holds a Train. So a train component does not point at another train, and a train in a
-- compound train is never a train element type. Dropping the columns also drops their indexes and foreign keys.
ALTER TABLE train_component
    DROP COLUMN train_id;

ALTER TABLE train_in_compound_train
    DROP COLUMN train_element_type_ref;
