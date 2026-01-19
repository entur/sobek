CREATE SEQUENCE IF NOT EXISTS netex_deck_plan_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_deck_plan_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_vehicle_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_vehicle_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_vehicle_type_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_vehicle_type_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_vehicle_model_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_vehicle_model_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_passenger_capacity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_passenger_capacity_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_alternative_name_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_alternative_name_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_deck_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_deck_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_accessibility_limitation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_accessibility_limitation_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_passenger_entrance_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_passenger_entrance_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_vehicle_equipment_profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_vehicle_equipment_profile_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_spot_row_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_spot_row_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_train_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_train_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_accessibility_assessment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_accessibility_assessment_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_vehicle_equipment_profile_member_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_vehicle_equipment_profile_member_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_schematic_map_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_schematic_map_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_schematic_map_member_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_schematic_map_member_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_deck_space_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_deck_space_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_deck_space_capacity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_deck_space_capacity_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_spot_column_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_spot_column_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_locatable_spot_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_locatable_spot_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_equipment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_equipment_seq OWNER TO sobek;

CREATE SEQUENCE IF NOT EXISTS netex_spot_affinity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_spot_affinity_seq OWNER TO sobek;

drop table IF EXISTS id_generator;