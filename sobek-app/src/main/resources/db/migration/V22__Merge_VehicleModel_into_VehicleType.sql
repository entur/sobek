-- The vehicle model is folded into the vehicle type: each vehicle type has at most one model, and the model has
-- only manufacturer, range and full charge that the type does not already have.
-- Name and description of a model are not carried over: the type has its own. Neither are its key values, which
-- are only allowed to be the id the model was imported with (imported-id): any other key value stops the migration,
-- for a manual cleanup.

ALTER TABLE vehicle_type
    ADD COLUMN manufacturer_value character varying(255),
    ADD COLUMN manufacturer_lang  character varying(5),
    ADD COLUMN range              numeric(38, 2),
    ADD COLUMN full_charge        numeric(38, 2);

DO
$$
    DECLARE
        ambiguous       text;
        other_key_values text;
    BEGIN
        -- A type with more than one model cannot be folded without losing data: stop the migration
        SELECT string_agg(type_netex_id, ', ')
        INTO ambiguous
        FROM (SELECT vt.netex_id AS type_netex_id
              FROM vehicle_model vm
                       JOIN vehicle_type vt ON vt.id = vm.transport_type_id
              GROUP BY vt.netex_id
              HAVING count(DISTINCT vm.netex_id) > 1) t;

        IF ambiguous IS NOT NULL THEN
            RAISE EXCEPTION 'Vehicle types with more than one vehicle model: %', ambiguous;
        END IF;

        -- The key values of a model would be lost: only the imported ids may go, anything else must be cleaned up by hand
        SELECT string_agg(DISTINCT vm.netex_id || ' (' || kv.key || ')', ', ')
        INTO other_key_values
        FROM vehicle_model vm
                 JOIN vehicle_model_key_values vmkv ON vmkv.vehicle_model_id = vm.id
                 JOIN key_value kv ON kv.id = vmkv.key_values_id
        WHERE kv.key <> 'imported-id';

        IF other_key_values IS NOT NULL THEN
            RAISE EXCEPTION 'Vehicle models with key values other than imported-id, to be cleaned up manually before the migration: %',
                other_key_values;
        END IF;
    END
$$;

-- The latest version of the model of each type is applied to all versions of the type
UPDATE vehicle_type vt
SET manufacturer_value = m.manufacturer_value,
    manufacturer_lang  = m.manufacturer_lang,
    range              = m.range,
    full_charge        = m.full_charge
FROM (SELECT DISTINCT ON (type.netex_id) type.netex_id AS type_netex_id,
                                         vm.manufacturer_value,
                                         vm.manufacturer_lang,
                                         vm.range,
                                         vm.full_charge
      FROM vehicle_model vm
               JOIN vehicle_type type ON type.id = vm.transport_type_id
      ORDER BY type.netex_id, vm.version DESC) m
WHERE vt.netex_id = m.type_netex_id;

ALTER TABLE vehicle
    DROP COLUMN vehicle_model_id;

-- What is left of the key values of the models is imported ids: remove them with the models, from the shared key_value table too
WITH removed AS (DELETE FROM vehicle_model_key_values RETURNING key_values_id)
DELETE
FROM key_value
WHERE id IN (SELECT key_values_id FROM removed);

DROP TABLE vehicle_model_key_values;
DROP TABLE vehicle_model;
DROP SEQUENCE IF EXISTS vehicle_model_seq;
DROP SEQUENCE IF EXISTS netex_vehicle_model_seq;
