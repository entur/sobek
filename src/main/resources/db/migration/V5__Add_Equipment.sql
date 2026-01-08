ALTER TABLE passenger_spot
    DROP CONSTRAINT fk2pjm2pakaifla5yq7r1cxqhhc;

ALTER TABLE passenger_space_passenger_spots
    DROP CONSTRAINT fk7heqymvtxduigg4yh6p0i0bti;

ALTER TABLE passenger_spot_key_values
    DROP CONSTRAINT fkli3dtmouxlvsui6gosuq80ug3;

ALTER TABLE passenger_spot_key_values
    DROP CONSTRAINT fksb8dp7sv2tk61e5y8jsn65nd7;

CREATE SEQUENCE IF NOT EXISTS sequence_per_table_generator START WITH 1 INCREMENT BY 50;

CREATE TABLE deck_spot_columns
(
    deck_id         BIGINT NOT NULL,
    spot_columns_id BIGINT NOT NULL
);

CREATE TABLE equipment
(
    id                          BIGINT      NOT NULL,
    dtype                       VARCHAR(31) NOT NULL,
    out_of_service              BOOLEAN,
    version_comment             VARCHAR(255),
    changed_by                  VARCHAR(255),
    created                     TIMESTAMP WITHOUT TIME ZONE,
    changed                     TIMESTAMP WITHOUT TIME ZONE,
    version                     BIGINT      NOT NULL,
    netex_id                    VARCHAR(255),
    name_value                  VARCHAR(255),
    name_lang                   VARCHAR(5),
    private_code_value          VARCHAR(255),
    private_code_type           VARCHAR(255),
    description_value           VARCHAR(255),
    description_lang            VARCHAR(255),
    note_value                  VARCHAR(255),
    note_lang                   VARCHAR(255),
    from_date                   TIMESTAMP WITHOUT TIME ZONE,
    to_date                     TIMESTAMP WITHOUT TIME ZONE,
    height                      DECIMAL,
    width                       DECIMAL,
    height_from_floor           DECIMAL,
    brand_graphic               VARCHAR(255),
    sign_graphic                VARCHAR(255),
    machine_readable            BOOLEAN,
    length                      DECIMAL,
    has_power_supply            BOOLEAN,
    has_usb_power_socket        BOOLEAN,
    sign_content_type           VARCHAR(255),
    units                       DECIMAL,
    gender                      SMALLINT,
    number_of_toilets           DECIMAL,
    seat_back_height            DECIMAL,
    seat_depth                  DECIMAL,
    is_foldup                   BOOLEAN,
    is_reclining                BOOLEAN,
    maximum_recline             DECIMAL,
    is_reversible               BOOLEAN,
    can_rotate                  BOOLEAN,
    direction_of_use            SMALLINT,
    passengers_per_minute       DECIMAL,
    relative_weighting          DECIMAL,
    safe_for_guide_dog          BOOLEAN,
    low_floor                   BOOLEAN,
    high_floor                  BOOLEAN,
    hoist                       BOOLEAN,
    hoist_operating_radius      DECIMAL,
    ramp                        BOOLEAN,
    bearing_capacity            DECIMAL,
    number_of_steps             DECIMAL,
    boarding_height             DECIMAL,
    equipment_length            DECIMAL,
    equipment_width             DECIMAL,
    gap_to_platform             DECIMAL,
    width_of_access_area        DECIMAL,
    height_of_access_area       DECIMAL,
    automatic_doors             BOOLEAN,
    guide_dogs_allowed          BOOLEAN,
    depth                       DECIMAL,
    step_height                 DECIMAL,
    step_length                 DECIMAL,
    step_colour_contrast        BOOLEAN,
    step_condition              SMALLINT,
    handrail_type               SMALLINT,
    handrail_height             DECIMAL,
    lower_handrail_height       DECIMAL,
    tactile_writing             BOOLEAN,
    door                        BOOLEAN,
    door_orientation            SMALLINT,
    door_handle_outside         SMALLINT,
    door_handle_inside          SMALLINT,
    revolving_door              BOOLEAN,
    door_type                   SMALLINT,
    number_of_gates             DECIMAL,
    staffing                    SMALLINT,
    entrance_requires_staffing  BOOLEAN,
    entrance_requires_ticket    BOOLEAN,
    entrance_requires_passport  BOOLEAN,
    drop_kerb_outside           BOOLEAN,
    acoustic_sensor             BOOLEAN,
    automatic_door              BOOLEAN,
    door_control_element_height DECIMAL,
    glass_door                  BOOLEAN,
    airlock                     BOOLEAN,
    wheelchair_passable         BOOLEAN,
    wheelchair_unaided          BOOLEAN,
    audio_or_video_intercom     BOOLEAN,
    entrance_attention          SMALLINT,
    doorstep_mark               BOOLEAN,
    necessary_force_to_open     SMALLINT,
    suitable_for_cycles         BOOLEAN,
    audio_passthrough_indicator BOOLEAN,
    ramp_doorbell               BOOLEAN,
    recognizable                BOOLEAN,
    turning_space_position      SMALLINT,
    wheelchair_turning_circle   DECIMAL,
    continuous_handrail         BOOLEAN,
    without_riser               BOOLEAN,
    spiral_stair                BOOLEAN,
    number_of_flights           DECIMAL,
    content_value               VARCHAR(255),
    content_lang                VARCHAR(255),
    CONSTRAINT pk_equipment PRIMARY KEY (id)
);

CREATE SEQUENCE equipment_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE equipment_seq OWNER TO sobek;


CREATE TABLE equipment_key_values
(
    equipment_id   BIGINT       NOT NULL,
    key_values_id  BIGINT       NOT NULL,
    key_values_key VARCHAR(255) NOT NULL,
    CONSTRAINT pk_equipment_keyvalues PRIMARY KEY (equipment_id, key_values_key)
);

CREATE TABLE locatable_spot
(
    id                      BIGINT      NOT NULL,
    dtype                   VARCHAR(31) NOT NULL,
    locatable_spot_type     VARCHAR(255),
    spot_column_id          BIGINT,
    spot_row_id             BIGINT,
    orientation             VARCHAR(255),
    width                   DECIMAL,
    length                  DECIMAL,
    height                  DECIMAL,
    centroid                BYTEA,
    polygon_id              BIGINT,
    version_comment         VARCHAR(255),
    changed_by              VARCHAR(255),
    created                 TIMESTAMP WITHOUT TIME ZONE,
    changed                 TIMESTAMP WITHOUT TIME ZONE,
    version                 BIGINT      NOT NULL,
    netex_id                VARCHAR(255),
    label_value             VARCHAR(255),
    label_lang              VARCHAR(5),
    name_value              VARCHAR(255),
    name_lang               VARCHAR(5),
    description_value       VARCHAR(4000),
    description_lang        VARCHAR(5),
    private_code_value      VARCHAR(255),
    private_code_type       VARCHAR(255),
    from_date               TIMESTAMP WITHOUT TIME ZONE,
    to_date                 TIMESTAMP WITHOUT TIME ZONE,
    is_accessible_on_voyage BOOLEAN,
    height_from_floor       DECIMAL,
    table_type              VARCHAR(255),
    has_armrest             BOOLEAN,
    leg_space               DECIMAL,
    has_power               BOOLEAN,
    is_by_window            BOOLEAN,
    is_by_aisle             BOOLEAN,
    is_between_seats        BOOLEAN,
    is_in_front_row         BOOLEAN,
    is_in_end_row           BOOLEAN,
    is_facing_window        BOOLEAN,
    is_facing_aisle         BOOLEAN,
    CONSTRAINT pk_locatablespot PRIMARY KEY (id)
);

CREATE SEQUENCE locatable_spot_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE locatable_spot_seq OWNER TO sobek;

CREATE TABLE locatable_spot_actual_vehicle_equipments
(
    locatable_spot_id            BIGINT NOT NULL,
    actual_vehicle_equipments_id BIGINT NOT NULL
);

CREATE TABLE locatable_spot_key_values
(
    locatable_spot_id BIGINT       NOT NULL,
    key_values_id     BIGINT       NOT NULL,
    key_values_key    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_locatable_spot_keyvalues PRIMARY KEY (locatable_spot_id, key_values_key)
);

CREATE TABLE passenger_entrance_actual_vehicle_equipments
(
    passenger_entrance_id        BIGINT NOT NULL,
    actual_vehicle_equipments_id BIGINT NOT NULL
);

CREATE TABLE passenger_space_actual_vehicle_equipments
(
    passenger_space_id           BIGINT NOT NULL,
    actual_vehicle_equipments_id BIGINT NOT NULL
);

CREATE TABLE passenger_space_luggage_spots
(
    passenger_space_id BIGINT NOT NULL,
    luggage_spots_id   BIGINT NOT NULL
);

CREATE TABLE passenger_space_spot_affinities
(
    passenger_space_id BIGINT NOT NULL,
    spot_affinities_id BIGINT NOT NULL
);

CREATE TABLE spot_affinity
(
    id                 BIGINT NOT NULL,
    spot_affinity_type SMALLINT,
    maximum_spots      DECIMAL,
    version_comment    VARCHAR(255),
    changed_by         VARCHAR(255),
    created            TIMESTAMP WITHOUT TIME ZONE,
    changed            TIMESTAMP WITHOUT TIME ZONE,
    version            BIGINT NOT NULL,
    netex_id           VARCHAR(255),
    name_value         VARCHAR(255),
    name_lang          VARCHAR(5),
    description_value  VARCHAR(255),
    description_lang   VARCHAR(5),
    from_date          TIMESTAMP WITHOUT TIME ZONE,
    to_date            TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_spotaffinity PRIMARY KEY (id)
);

CREATE SEQUENCE spot_affinity_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE spot_affinity_seq OWNER TO sobek;

CREATE TABLE spot_affinity_key_values
(
    spot_affinity_id BIGINT       NOT NULL,
    key_values_id    BIGINT       NOT NULL,
    key_values_key   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_spot_affinity_keyvalues PRIMARY KEY (spot_affinity_id, key_values_key)
);

CREATE TABLE spot_affinity_member
(
    locatable_spot_id BIGINT NOT NULL,
    spot_affinity_id  BIGINT NOT NULL
);

CREATE TABLE spot_column
(
    id                  BIGINT NOT NULL,
    created             TIMESTAMP WITHOUT TIME ZONE,
    changed             TIMESTAMP WITHOUT TIME ZONE,
    version             BIGINT NOT NULL,
    netex_id            VARCHAR(255),
    numbering_from_left BOOLEAN,
    from_date           TIMESTAMP WITHOUT TIME ZONE,
    to_date             TIMESTAMP WITHOUT TIME ZONE,
    label_value         VARCHAR(255),
    label_lang          VARCHAR(5),
    CONSTRAINT pk_spotcolumn PRIMARY KEY (id)
);

CREATE SEQUENCE spot_column_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE spot_column_seq OWNER TO sobek;

ALTER TABLE deck_spot_columns
    ADD CONSTRAINT uc_deck_spot_columns_spotcolumns UNIQUE (spot_columns_id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT uc_equipment_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE locatable_spot_key_values
    ADD CONSTRAINT uc_locatable_spot_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE locatable_spot_actual_vehicle_equipments
    ADD CONSTRAINT uc_locatablespotactualvehicleequipments_actualvehicleequipments UNIQUE (actual_vehicle_equipments_id, locatable_spot_id);

ALTER TABLE passenger_space_luggage_spots
    ADD CONSTRAINT uc_passenger_space_luggage_spots_luggagespots UNIQUE (luggage_spots_id);

ALTER TABLE passenger_space_spot_affinities
    ADD CONSTRAINT uc_passenger_space_spot_affinities_spotaffinities UNIQUE (spot_affinities_id);

ALTER TABLE passenger_entrance_actual_vehicle_equipments
    ADD CONSTRAINT uc_passengerentranceactualvehicleequipm_actualvehicleequipments UNIQUE (actual_vehicle_equipments_id, passenger_entrance_id);

ALTER TABLE passenger_space_actual_vehicle_equipments
    ADD CONSTRAINT uc_passengerspaceactualvehicleequipment_actualvehicleequipments UNIQUE (actual_vehicle_equipments_id, passenger_space_id);

ALTER TABLE spot_affinity_key_values
    ADD CONSTRAINT uc_spot_affinity_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE locatable_spot
    ADD CONSTRAINT FK_LOCATABLESPOT_ON_POLYGON FOREIGN KEY (polygon_id) REFERENCES persistable_polygon (id);

ALTER TABLE locatable_spot
    ADD CONSTRAINT FK_LOCATABLESPOT_ON_SPOTCOLUMN FOREIGN KEY (spot_column_id) REFERENCES spot_column (id);

ALTER TABLE locatable_spot
    ADD CONSTRAINT FK_LOCATABLESPOT_ON_SPOTROW FOREIGN KEY (spot_row_id) REFERENCES spot_row (id);

ALTER TABLE deck_spot_columns
    ADD CONSTRAINT fk_decspocol_on_deck FOREIGN KEY (deck_id) REFERENCES deck (id);

ALTER TABLE deck_spot_columns
    ADD CONSTRAINT fk_decspocol_on_spot_column FOREIGN KEY (spot_columns_id) REFERENCES spot_column (id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT fk_equkeyval_on_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT fk_equkeyval_on_value FOREIGN KEY (key_values_id) REFERENCES value (id);

ALTER TABLE locatable_spot_actual_vehicle_equipments
    ADD CONSTRAINT fk_locspoactvehequ_on_equipment FOREIGN KEY (actual_vehicle_equipments_id) REFERENCES equipment (id);

ALTER TABLE locatable_spot_actual_vehicle_equipments
    ADD CONSTRAINT fk_locspoactvehequ_on_locatable_spot FOREIGN KEY (locatable_spot_id) REFERENCES locatable_spot (id);

ALTER TABLE locatable_spot_key_values
    ADD CONSTRAINT fk_locspokeyval_on_locatable_spot FOREIGN KEY (locatable_spot_id) REFERENCES locatable_spot (id);

ALTER TABLE locatable_spot_key_values
    ADD CONSTRAINT fk_locspokeyval_on_value FOREIGN KEY (key_values_id) REFERENCES value (id);

ALTER TABLE passenger_entrance_actual_vehicle_equipments
    ADD CONSTRAINT fk_pasentactvehequ_on_equipment FOREIGN KEY (actual_vehicle_equipments_id) REFERENCES equipment (id);

ALTER TABLE passenger_entrance_actual_vehicle_equipments
    ADD CONSTRAINT fk_pasentactvehequ_on_passenger_entrance FOREIGN KEY (passenger_entrance_id) REFERENCES passenger_entrance (id);

ALTER TABLE passenger_space_actual_vehicle_equipments
    ADD CONSTRAINT fk_passpaactvehequ_on_equipment FOREIGN KEY (actual_vehicle_equipments_id) REFERENCES equipment (id);

ALTER TABLE passenger_space_actual_vehicle_equipments
    ADD CONSTRAINT fk_passpaactvehequ_on_passenger_space FOREIGN KEY (passenger_space_id) REFERENCES passenger_space (id);

ALTER TABLE passenger_space_luggage_spots
    ADD CONSTRAINT fk_passpalugspo_on_luggage_spot FOREIGN KEY (luggage_spots_id) REFERENCES locatable_spot (id);

ALTER TABLE passenger_space_luggage_spots
    ADD CONSTRAINT fk_passpalugspo_on_passenger_space FOREIGN KEY (passenger_space_id) REFERENCES passenger_space (id);

ALTER TABLE passenger_space_passenger_spots
    ADD CONSTRAINT fk_passpapasspo_on_passenger_spot FOREIGN KEY (passenger_spots_id) REFERENCES locatable_spot (id);

ALTER TABLE passenger_space_spot_affinities
    ADD CONSTRAINT fk_passpaspoaff_on_passenger_space FOREIGN KEY (passenger_space_id) REFERENCES passenger_space (id);

ALTER TABLE passenger_space_spot_affinities
    ADD CONSTRAINT fk_passpaspoaff_on_spot_affinity FOREIGN KEY (spot_affinities_id) REFERENCES spot_affinity (id);

ALTER TABLE spot_affinity_key_values
    ADD CONSTRAINT fk_spoaffkeyval_on_spot_affinity FOREIGN KEY (spot_affinity_id) REFERENCES spot_affinity (id);

ALTER TABLE spot_affinity_key_values
    ADD CONSTRAINT fk_spoaffkeyval_on_value FOREIGN KEY (key_values_id) REFERENCES value (id);

ALTER TABLE spot_affinity_member
    ADD CONSTRAINT fk_spoaffmem_on_locatable_spot FOREIGN KEY (locatable_spot_id) REFERENCES locatable_spot (id);

ALTER TABLE spot_affinity_member
    ADD CONSTRAINT fk_spoaffmem_on_spot_affinity FOREIGN KEY (spot_affinity_id) REFERENCES spot_affinity (id);

DROP TABLE installed_equipment_version_structure CASCADE;

DROP TABLE passenger_spot CASCADE;

DROP TABLE passenger_spot_key_values CASCADE;
