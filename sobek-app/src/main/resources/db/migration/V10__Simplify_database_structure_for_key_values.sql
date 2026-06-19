ALTER TABLE passenger_capacity_key_values
    DROP CONSTRAINT fk1px95rscwii4e37t469uxm0f6;

ALTER TABLE deck_key_values
    DROP CONSTRAINT fk39w5ehqf27pyr08sp15noytqt;

ALTER TABLE vehicle_equipment_profile_key_values
    DROP CONSTRAINT fk64bd9ffaj36c37c70e8lhrfj2;

ALTER TABLE train_key_values
    DROP CONSTRAINT fk69vy3a09ikk101t624gv8fhhb;

ALTER TABLE vehicle_key_values
    DROP CONSTRAINT fk7e6dgl7bvmhpmtfx1fif9i1ah;

ALTER TABLE deck_space_key_values
    DROP CONSTRAINT fk_decspakeyval_on_value;

ALTER TABLE equipment_key_values
    DROP CONSTRAINT fk_equkeyval_on_value;

ALTER TABLE locatable_spot_key_values
    DROP CONSTRAINT fk_locspokeyval_on_value;

ALTER TABLE spot_affinity_key_values
    DROP CONSTRAINT fk_spoaffkeyval_on_value;

ALTER TABLE vehicle_model_key_values
    DROP CONSTRAINT fke9kanp31wgd4296cbbkbeudlv;

ALTER TABLE vehicle_type_key_values
    DROP CONSTRAINT fkfpvdooa1fqfxa0j9fnw0e0yih;

ALTER TABLE value_items
    DROP CONSTRAINT fknuulrwd9o0m7ocvcntkig5csj;

ALTER TABLE deck_plan_key_values
    DROP CONSTRAINT fkp2w8p403qkxvcja1ctebht92v;

ALTER TABLE passenger_entrance_key_values
    DROP CONSTRAINT fkrj560iwjbe4s0r5pgja1xpc92;

CREATE TABLE key_value
(
    id    BIGINT NOT NULL,
    key   VARCHAR(255),
    value VARCHAR(255),
    CONSTRAINT pk_keyvalue PRIMARY KEY (id)
);

insert into key_value(id, key, value)
select value_id, key_values_key,items from passenger_capacity_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from deck_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from vehicle_equipment_profile_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from train_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from vehicle_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from deck_space_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from equipment_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from locatable_spot_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from spot_affinity_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from vehicle_model_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from vehicle_type_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from deck_plan_key_values join value_items on value_id=key_values_id
union all
select value_id, key_values_key,items from passenger_entrance_key_values join value_items on value_id=key_values_id;

/* Clean up some inconsistent data in test */
delete FROM public.passenger_capacity_key_values
where not exists(select * from key_value where id=key_values_id);
delete FROM public.vehicle_key_values
where not exists(select * from key_value where id=key_values_id);
delete FROM public.vehicle_type_key_values
where not exists(select * from key_value where id=key_values_id);
delete FROM public.vehicle_model_key_values
where not exists(select * from key_value where id=key_values_id);

ALTER TABLE schematic_map_key_values
    ADD CONSTRAINT uc_schematic_map_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE schematic_map_members
    ADD CONSTRAINT uc_schematic_map_members_members UNIQUE (members_id);

ALTER TABLE deck_key_values
    ADD CONSTRAINT fk_deckeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE deck_plan_key_values
    ADD CONSTRAINT fk_decplakeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE deck_space_key_values
    ADD CONSTRAINT fk_decspakeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT fk_equkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE locatable_spot_key_values
    ADD CONSTRAINT fk_locspokeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE passenger_capacity_key_values
    ADD CONSTRAINT fk_pascapkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE passenger_entrance_key_values
    ADD CONSTRAINT fk_pasentkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE schematic_map_key_values
    ADD CONSTRAINT fk_schmapkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE schematic_map_key_values
    ADD CONSTRAINT fk_schmapkeyval_on_schematic_map FOREIGN KEY (schematic_map_id) REFERENCES schematic_map (id);

ALTER TABLE schematic_map_members
    ADD CONSTRAINT fk_schmapmem_on_schematic_map FOREIGN KEY (schematic_map_id) REFERENCES schematic_map (id);

ALTER TABLE schematic_map_members
    ADD CONSTRAINT fk_schmapmem_on_schematic_map_member FOREIGN KEY (members_id) REFERENCES schematic_map_member (id);

ALTER TABLE spot_affinity_key_values
    ADD CONSTRAINT fk_spoaffkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE train_key_values
    ADD CONSTRAINT fk_trakeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE vehicle_equipment_profile_key_values
    ADD CONSTRAINT fk_vehequprokeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE vehicle_key_values
    ADD CONSTRAINT fk_vehkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE vehicle_model_key_values
    ADD CONSTRAINT fk_vehmodkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

ALTER TABLE vehicle_type_key_values
    ADD CONSTRAINT fk_vehtypkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

DROP TABLE value CASCADE;

DROP TABLE value_items CASCADE;

ALTER TABLE deck_key_values
    DROP CONSTRAINT deck_key_values_pkey;

ALTER TABLE deck_plan_key_values
    DROP CONSTRAINT deck_plan_key_values_pkey;

ALTER TABLE passenger_capacity_key_values
    DROP CONSTRAINT passenger_capacity_key_values_pkey;

ALTER TABLE passenger_entrance_key_values
    DROP CONSTRAINT passenger_entrance_key_values_pkey;

ALTER TABLE deck_space_key_values
    DROP CONSTRAINT pk_deck_space_keyvalues;

ALTER TABLE equipment_key_values
    DROP CONSTRAINT pk_equipment_keyvalues;

ALTER TABLE locatable_spot_key_values
    DROP CONSTRAINT pk_locatable_spot_keyvalues;

ALTER TABLE schematic_map_key_values
    DROP CONSTRAINT pk_null_keyvalues;

ALTER TABLE spot_affinity_key_values
    DROP CONSTRAINT pk_spot_affinity_keyvalues;

ALTER TABLE train_key_values
    DROP CONSTRAINT train_key_values_pkey;

ALTER TABLE vehicle_equipment_profile_key_values
    DROP CONSTRAINT vehicle_equipment_profile_key_values_pkey;

ALTER TABLE vehicle_key_values
    DROP CONSTRAINT vehicle_key_values_pkey;

ALTER TABLE vehicle_model_key_values
    DROP CONSTRAINT vehicle_model_key_values_pkey;

ALTER TABLE vehicle_type_key_values
    DROP CONSTRAINT vehicle_type_key_values_pkey;

ALTER TABLE deck_key_values
    DROP COLUMN key_values_key;

ALTER TABLE deck_plan_key_values
    DROP COLUMN key_values_key;

ALTER TABLE deck_space_key_values
    DROP COLUMN key_values_key;

ALTER TABLE equipment_key_values
    DROP COLUMN key_values_key;

ALTER TABLE locatable_spot_key_values
    DROP COLUMN key_values_key;

ALTER TABLE passenger_capacity_key_values
    DROP COLUMN key_values_key;

ALTER TABLE passenger_entrance_key_values
    DROP COLUMN key_values_key;

ALTER TABLE schematic_map_key_values
    DROP COLUMN key_values_key;

ALTER TABLE spot_affinity_key_values
    DROP COLUMN key_values_key;

ALTER TABLE train_key_values
    DROP COLUMN key_values_key;

ALTER TABLE vehicle_equipment_profile_key_values
    DROP COLUMN key_values_key;

ALTER TABLE vehicle_key_values
    DROP COLUMN key_values_key;

ALTER TABLE vehicle_model_key_values
    DROP COLUMN key_values_key;

ALTER TABLE vehicle_type_key_values
    DROP COLUMN key_values_key;


CREATE SEQUENCE public.key_value_seq
    START WITH 5000
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

