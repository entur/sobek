ALTER TABLE IF EXISTS public.vehicle_type
    RENAME fuel_type TO fuel_types;

ALTER TABLE IF EXISTS public.vehicle_type
    RENAME propulsion_type TO propulsion_types;

ALTER TABLE IF EXISTS public.vehicle_type DROP CONSTRAINT IF EXISTS vehicle_type_propulsion_type_check;

ALTER TABLE IF EXISTS public.vehicle_type DROP CONSTRAINT IF EXISTS vehicle_type_fuel_type_check;