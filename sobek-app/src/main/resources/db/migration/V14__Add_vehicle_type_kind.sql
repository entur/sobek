ALTER TABLE vehicle_type
    ADD vehicle_type_kind VARCHAR(255);

-- All existing vehicle types describe real vehicles
UPDATE vehicle_type
SET vehicle_type_kind = 'PHYSICAL';

ALTER TABLE vehicle_type
    ALTER COLUMN vehicle_type_kind SET NOT NULL;
