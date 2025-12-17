CREATE TABLE equipment
(
    id                          BIGINT      NOT NULL,
    dtype                       VARCHAR(31) NOT NULL,
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
    value                       VARCHAR(255),
    lang                        VARCHAR(5),
    description_value           VARCHAR(255),
    description_lang            VARCHAR(255),
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
    out_of_service              BOOLEAN,
    number_of_flights           DECIMAL,
    note_value                  VARCHAR(255),
    note_lang                   VARCHAR(255),
    content_value               VARCHAR(255),
    content_lang                VARCHAR(255),
    CONSTRAINT pk_equipment PRIMARY KEY (id)
);


CREATE TABLE equipment_key_values
(
    equipment_id   BIGINT       NOT NULL,
    key_values_id  BIGINT       NOT NULL,
    key_values_key VARCHAR(255) NOT NULL,
    CONSTRAINT pk_equipment_keyvalues PRIMARY KEY (equipment_id, key_values_key)
);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT uc_equipment_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT fk_equkeyval_on_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id);

ALTER TABLE equipment_key_values
    ADD CONSTRAINT fk_equkeyval_on_value FOREIGN KEY (key_values_id) REFERENCES value (id);


CREATE SEQUENCE IF NOT EXISTS public.equipment_seq
    INCREMENT 10
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.equipment_seq
    OWNER TO sobek;
