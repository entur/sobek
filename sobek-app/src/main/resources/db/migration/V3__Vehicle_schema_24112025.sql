
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 271 (class 1259 OID 19739)
-- Name: accessibility_assessment; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.accessibility_assessment (
                                                 id bigint NOT NULL,
                                                 netex_id character varying(255),
                                                 changed timestamp(6) with time zone,
                                                 created timestamp(6) with time zone,
                                                 from_date timestamp(6) with time zone,
                                                 to_date timestamp(6) with time zone,
                                                 version bigint NOT NULL,
                                                 mobility_impaired_access character varying(255),
                                                 CONSTRAINT accessibility_assessment_mobility_impaired_access_check CHECK (((mobility_impaired_access)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[])))
);


ALTER TABLE public.accessibility_assessment OWNER TO sobek;

--
-- TOC entry 272 (class 1259 OID 19748)
-- Name: accessibility_assessment_limitations; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.accessibility_assessment_limitations (
                                                             accessibility_assessment_id bigint NOT NULL,
                                                             limitations_id bigint NOT NULL
);


ALTER TABLE public.accessibility_assessment_limitations OWNER TO sobek;

--
-- TOC entry 312 (class 1259 OID 20067)
-- Name: accessibility_assessment_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.accessibility_assessment_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.accessibility_assessment_seq OWNER TO sobek;

--
-- TOC entry 273 (class 1259 OID 19751)
-- Name: accessibility_limitation; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.accessibility_limitation (
                                                 id bigint NOT NULL,
                                                 netex_id character varying(255),
                                                 changed timestamp(6) with time zone,
                                                 created timestamp(6) with time zone,
                                                 from_date timestamp(6) with time zone,
                                                 to_date timestamp(6) with time zone,
                                                 version bigint NOT NULL,
                                                 audible_signals_available character varying(255),
                                                 escalator_free_access character varying(255),
                                                 lift_free_access character varying(255),
                                                 step_free_access character varying(255),
                                                 visual_signs_available character varying(255),
                                                 wheelchair_access character varying(255),
                                                 CONSTRAINT accessibility_limitation_audible_signals_available_check CHECK (((audible_signals_available)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[]))),
    CONSTRAINT accessibility_limitation_escalator_free_access_check CHECK (((escalator_free_access)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[]))),
    CONSTRAINT accessibility_limitation_lift_free_access_check CHECK (((lift_free_access)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[]))),
    CONSTRAINT accessibility_limitation_step_free_access_check CHECK (((step_free_access)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[]))),
    CONSTRAINT accessibility_limitation_visual_signs_available_check CHECK (((visual_signs_available)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[]))),
    CONSTRAINT accessibility_limitation_wheelchair_access_check CHECK (((wheelchair_access)::text = ANY ((ARRAY['TRUE'::character varying, 'FALSE'::character varying, 'UNKNOWN'::character varying, 'PARTIAL'::character varying])::text[])))
);


ALTER TABLE public.accessibility_limitation OWNER TO sobek;

--
-- TOC entry 313 (class 1259 OID 20069)
-- Name: accessibility_limitation_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.accessibility_limitation_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.accessibility_limitation_seq OWNER TO sobek;

--
-- TOC entry 274 (class 1259 OID 19765)
-- Name: alternative_name; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.alternative_name (
                                         id bigint NOT NULL,
                                         netex_id character varying(255),
                                         changed timestamp(6) with time zone,
                                         created timestamp(6) with time zone,
                                         from_date timestamp(6) with time zone,
                                         to_date timestamp(6) with time zone,
                                         version bigint NOT NULL,
                                         abbreviation_lang character varying(255),
                                         abbreviation_value character varying(255),
                                         lang character varying(255),
                                         name_lang character varying(255),
                                         name_value character varying(255),
                                         name_type character varying(255),
                                         named_object_ref bytea,
                                         qualifier_name_lang character varying(255),
                                         qualifier_name_value character varying(255),
                                         short_name_lang character varying(255),
                                         short_name_value character varying(255),
                                         type_of_name character varying(255),
                                         CONSTRAINT alternative_name_name_type_check CHECK (((name_type)::text = ANY ((ARRAY['ALIAS'::character varying, 'TRANSLATION'::character varying, 'COPY'::character varying, 'LABEL'::character varying, 'OTHER'::character varying])::text[])))
);


ALTER TABLE public.alternative_name OWNER TO sobek;

--
-- TOC entry 314 (class 1259 OID 20071)
-- Name: alternative_name_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.alternative_name_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alternative_name_seq OWNER TO sobek;

--
-- TOC entry 275 (class 1259 OID 19774)
-- Name: deck; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck (
                             id bigint NOT NULL,
                             netex_id character varying(255),
                             changed timestamp(6) with time zone,
                             created timestamp(6) with time zone,
                             from_date timestamp(6) with time zone,
                             to_date timestamp(6) with time zone,
                             version bigint NOT NULL,
                             changed_by character varying(255),
                             version_comment character varying(255),
                             description_lang character varying(5),
                             description_value character varying(4000),
                             name_lang character varying(5),
                             name_value character varying(255),
                             private_code_type character varying(255),
                             private_code_value character varying(255),
                             centroid public.geometry,
                             label_lang character varying(5),
                             label_value character varying(255),
                             polygon_id bigint
);


ALTER TABLE public.deck OWNER TO sobek;

--
-- TOC entry 276 (class 1259 OID 19782)
-- Name: deck_deck_spaces; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_deck_spaces (
                                         deck_id bigint NOT NULL,
                                         deck_spaces_id bigint NOT NULL
);


ALTER TABLE public.deck_deck_spaces OWNER TO sobek;

--
-- TOC entry 277 (class 1259 OID 19785)
-- Name: deck_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_key_values (
                                        deck_id bigint NOT NULL,
                                        key_values_id bigint NOT NULL,
                                        key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.deck_key_values OWNER TO sobek;

--
-- TOC entry 279 (class 1259 OID 19793)
-- Name: deck_plan; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_plan (
                                  id bigint NOT NULL,
                                  netex_id character varying(255),
                                  changed timestamp(6) with time zone,
                                  created timestamp(6) with time zone,
                                  from_date timestamp(6) with time zone,
                                  to_date timestamp(6) with time zone,
                                  version bigint NOT NULL,
                                  changed_by character varying(255),
                                  version_comment character varying(255),
                                  description_lang character varying(5),
                                  description_value character varying(255),
                                  name_lang character varying(5),
                                  name_value character varying(255),
                                  orientation character varying(255),
                                  CONSTRAINT deck_plan_orientation_check CHECK (((orientation)::text = ANY ((ARRAY['FORWARDS'::character varying, 'BACKWARDS'::character varying, 'UNKNOWN'::character varying])::text[])))
);


ALTER TABLE public.deck_plan OWNER TO sobek;

--
-- TOC entry 280 (class 1259 OID 19802)
-- Name: deck_plan_decks; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_plan_decks (
                                        deck_plan_id bigint NOT NULL,
                                        decks_id bigint NOT NULL
);


ALTER TABLE public.deck_plan_decks OWNER TO sobek;

--
-- TOC entry 281 (class 1259 OID 19805)
-- Name: deck_plan_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_plan_key_values (
                                             deck_plan_id bigint NOT NULL,
                                             key_values_id bigint NOT NULL,
                                             key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.deck_plan_key_values OWNER TO sobek;

--
-- TOC entry 315 (class 1259 OID 20073)
-- Name: deck_plan_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.deck_plan_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.deck_plan_seq OWNER TO sobek;

--
-- TOC entry 316 (class 1259 OID 20075)
-- Name: deck_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.deck_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.deck_seq OWNER TO sobek;

--
-- TOC entry 278 (class 1259 OID 19790)
-- Name: deck_spot_rows; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.deck_spot_rows (
                                       deck_id bigint NOT NULL,
                                       spot_rows_id bigint NOT NULL
);


ALTER TABLE public.deck_spot_rows OWNER TO sobek;

--
-- TOC entry 282 (class 1259 OID 19810)
-- Name: export_job; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.export_job (
                                   id bigint NOT NULL,
                                   file_name character varying(255),
                                   finished timestamp(6) with time zone,
                                   job_url character varying(255),
                                   message character varying(255),
                                   started timestamp(6) with time zone,
                                   status smallint,
                                   sub_folder character varying(255),
                                   CONSTRAINT export_job_status_check CHECK (((status >= 0) AND (status <= 2)))
);


ALTER TABLE public.export_job OWNER TO sobek;

--
-- TOC entry 317 (class 1259 OID 20077)
-- Name: export_job_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.export_job_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.export_job_seq OWNER TO sobek;



--
-- TOC entry 283 (class 1259 OID 19819)
-- Name: installed_equipment_version_structure; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.installed_equipment_version_structure (
                                                              dtype character varying(31) NOT NULL,
                                                              id bigint NOT NULL,
                                                              netex_id character varying(255),
                                                              changed timestamp(6) with time zone,
                                                              created timestamp(6) with time zone,
                                                              from_date timestamp(6) with time zone,
                                                              to_date timestamp(6) with time zone,
                                                              version bigint NOT NULL,
                                                              out_of_service boolean,
                                                              private_code_type character varying(255),
                                                              private_code_value character varying(255),
                                                              brand_graphic character varying(255),
                                                              height numeric(38,2),
                                                              height_from_floor numeric(38,2),
                                                              machine_readable boolean,
                                                              sign_graphic character varying(255),
                                                              width numeric(38,2),
                                                              content_lang character varying(255),
                                                              content_value character varying(255),
                                                              sign_content_type character varying(255),
                                                              gender smallint,
                                                              number_of_toilets numeric(38,0),
                                                              CONSTRAINT installed_equipment_version_structure_gender_check CHECK (((gender >= 0) AND (gender <= 3))),
                                                              CONSTRAINT installed_equipment_version_structure_sign_content_type_check CHECK (((sign_content_type)::text = ANY ((ARRAY['ENTRANCE'::character varying, 'EXIT'::character varying, 'EMERGENCY_EXIT'::character varying, 'TRANSPORT_MODE'::character varying, 'NO_SMOKING'::character varying, 'TICKETS'::character varying, 'ASSISTANCE'::character varying, 'SOS_PHONE'::character varying, 'TOUCH_POINT'::character varying, 'MEETING_POINT'::character varying, 'TRANSPORT_MODE_POINT'::character varying, 'OTHER'::character varying])::text[])))
);


ALTER TABLE public.installed_equipment_version_structure OWNER TO sobek;

--
-- TOC entry 318 (class 1259 OID 20079)
-- Name: installed_equipment_version_structure_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.installed_equipment_version_structure_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.installed_equipment_version_structure_seq OWNER TO sobek;

--
-- TOC entry 284 (class 1259 OID 19829)
-- Name: multilingual_string_entity; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.multilingual_string_entity (
                                                   id bigint NOT NULL,
                                                   lang character varying(5),
                                                   value character varying(255)
);


ALTER TABLE public.multilingual_string_entity OWNER TO sobek;

--
-- TOC entry 285 (class 1259 OID 19834)
-- Name: passenger_capacity; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_capacity (
                                           id bigint NOT NULL,
                                           netex_id character varying(255),
                                           changed timestamp(6) with time zone,
                                           created timestamp(6) with time zone,
                                           from_date timestamp(6) with time zone,
                                           to_date timestamp(6) with time zone,
                                           version bigint NOT NULL,
                                           changed_by character varying(255),
                                           version_comment character varying(255),
                                           bicycle_rack_capacity numeric(38,0),
                                           fare_class character varying(255),
                                           pram_place_capacity numeric(38,0),
                                           pushchair_capacity numeric(38,0),
                                           seating_capacity numeric(38,0),
                                           special_place_capacity numeric(38,0),
                                           standing_capacity numeric(38,0),
                                           total_capacity numeric(38,0),
                                           wheelchair_place_capacity numeric(38,0),
                                           CONSTRAINT passenger_capacity_fare_class_check CHECK (((fare_class)::text = ANY ((ARRAY['UNKNOWN'::character varying, 'FIRST_CLASS'::character varying, 'SECOND_CLASS'::character varying, 'THIRD_CLASS'::character varying, 'PREFERENTE'::character varying, 'PREMIUM_CLASS'::character varying, 'BUSINESS_CLASS'::character varying, 'STANDARD_CLASS'::character varying, 'TURISTA'::character varying, 'ECONOMY_CLASS'::character varying, 'ANY'::character varying])::text[])))
);


ALTER TABLE public.passenger_capacity OWNER TO sobek;

--
-- TOC entry 286 (class 1259 OID 19843)
-- Name: passenger_capacity_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_capacity_key_values (
                                                      passenger_capacity_id bigint NOT NULL,
                                                      key_values_id bigint NOT NULL,
                                                      key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.passenger_capacity_key_values OWNER TO sobek;

--
-- TOC entry 319 (class 1259 OID 20081)
-- Name: passenger_capacity_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.passenger_capacity_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.passenger_capacity_seq OWNER TO sobek;

--
-- TOC entry 287 (class 1259 OID 19848)
-- Name: passenger_entrance; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_entrance (
                                           id bigint NOT NULL,
                                           netex_id character varying(255),
                                           changed timestamp(6) with time zone,
                                           created timestamp(6) with time zone,
                                           from_date timestamp(6) with time zone,
                                           to_date timestamp(6) with time zone,
                                           version bigint NOT NULL,
                                           changed_by character varying(255),
                                           version_comment character varying(255),
                                           description_lang character varying(5),
                                           description_value character varying(4000),
                                           name_lang character varying(5),
                                           name_value character varying(255),
                                           private_code_type character varying(255),
                                           private_code_value character varying(255),
                                           centroid public.geometry,
                                           height numeric(38,2),
                                           label_lang character varying(5),
                                           label_value character varying(255),
                                           length numeric(38,2),
                                           orientation character varying(255),
                                           width numeric(38,2),
                                           fare_class character varying(255),
                                           public_use boolean,
                                           deck_entrance_type character varying(255),
                                           distance_from_front numeric(38,2),
                                           has_door boolean,
                                           height_from_ground numeric(38,2),
                                           is_automatic boolean,
                                           is_emergency_exit boolean,
                                           sequence_from_front numeric(38,0),
                                           vehicle_side character varying(255),
                                           polygon_id bigint,
                                           CONSTRAINT passenger_entrance_deck_entrance_type_check CHECK (((deck_entrance_type)::text = ANY ((ARRAY['EXTERNAL'::character varying, 'COMMUNICATING'::character varying, 'INTERNAL'::character varying])::text[]))),
    CONSTRAINT passenger_entrance_fare_class_check CHECK (((fare_class)::text = ANY ((ARRAY['UNKNOWN'::character varying, 'FIRST_CLASS'::character varying, 'SECOND_CLASS'::character varying, 'THIRD_CLASS'::character varying, 'PREFERENTE'::character varying, 'PREMIUM_CLASS'::character varying, 'BUSINESS_CLASS'::character varying, 'STANDARD_CLASS'::character varying, 'TURISTA'::character varying, 'ECONOMY_CLASS'::character varying, 'ANY'::character varying])::text[]))),
    CONSTRAINT passenger_entrance_orientation_check CHECK (((orientation)::text = ANY ((ARRAY['FORWARDS'::character varying, 'BACKWARDS'::character varying, 'UNKNOWN'::character varying, 'LEFTWARDS'::character varying, 'RIGHTWARDS'::character varying, 'ANGLED_LEFT'::character varying, 'ANGLED_RIGHT'::character varying, 'ANGLED_BACK_LEFT'::character varying, 'ANGLED_BACK_RIGHT'::character varying])::text[]))),
    CONSTRAINT passenger_entrance_vehicle_side_check CHECK (((vehicle_side)::text = ANY ((ARRAY['LEFT_SIDE'::character varying, 'RIGHT_SIDE'::character varying, 'FRONT_END'::character varying, 'BACK_END'::character varying, 'INTERNAL'::character varying, 'ABOVE'::character varying, 'BELOW'::character varying])::text[])))
);


ALTER TABLE public.passenger_entrance OWNER TO sobek;

--
-- TOC entry 288 (class 1259 OID 19860)
-- Name: passenger_entrance_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_entrance_key_values (
                                                      passenger_entrance_id bigint NOT NULL,
                                                      key_values_id bigint NOT NULL,
                                                      key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.passenger_entrance_key_values OWNER TO sobek;

--
-- TOC entry 320 (class 1259 OID 20083)
-- Name: passenger_entrance_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.passenger_entrance_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.passenger_entrance_seq OWNER TO sobek;

--
-- TOC entry 289 (class 1259 OID 19865)
-- Name: passenger_space; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_space (
                                        id bigint NOT NULL,
                                        netex_id character varying(255),
                                        changed timestamp(6) with time zone,
                                        created timestamp(6) with time zone,
                                        from_date timestamp(6) with time zone,
                                        to_date timestamp(6) with time zone,
                                        version bigint NOT NULL,
                                        changed_by character varying(255),
                                        version_comment character varying(255),
                                        description_lang character varying(5),
                                        description_value character varying(4000),
                                        name_lang character varying(5),
                                        name_value character varying(255),
                                        private_code_type character varying(255),
                                        private_code_value character varying(255),
                                        centroid public.geometry,
                                        height numeric(38,2),
                                        label_lang character varying(5),
                                        label_value character varying(255),
                                        length numeric(38,2),
                                        orientation character varying(255),
                                        width numeric(38,2),
                                        fare_class character varying(255),
                                        public_use boolean,
                                        air_conditioned boolean,
                                        covered character varying(255),
                                        smoking_allowed boolean,
                                        total_capacity numeric(38,0),
                                        passenger_space_type character varying(255),
                                        standing_allowed boolean,
                                        polygon_id bigint,
                                        CONSTRAINT passenger_space_covered_check CHECK (((covered)::text = ANY ((ARRAY['INDOORS'::character varying, 'OUTDOORS'::character varying, 'COVERED'::character varying, 'MIXED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT passenger_space_fare_class_check CHECK (((fare_class)::text = ANY ((ARRAY['UNKNOWN'::character varying, 'FIRST_CLASS'::character varying, 'SECOND_CLASS'::character varying, 'THIRD_CLASS'::character varying, 'PREFERENTE'::character varying, 'PREMIUM_CLASS'::character varying, 'BUSINESS_CLASS'::character varying, 'STANDARD_CLASS'::character varying, 'TURISTA'::character varying, 'ECONOMY_CLASS'::character varying, 'ANY'::character varying])::text[]))),
    CONSTRAINT passenger_space_orientation_check CHECK (((orientation)::text = ANY ((ARRAY['FORWARDS'::character varying, 'BACKWARDS'::character varying, 'UNKNOWN'::character varying, 'LEFTWARDS'::character varying, 'RIGHTWARDS'::character varying, 'ANGLED_LEFT'::character varying, 'ANGLED_RIGHT'::character varying, 'ANGLED_BACK_LEFT'::character varying, 'ANGLED_BACK_RIGHT'::character varying])::text[]))),
    CONSTRAINT passenger_space_passenger_space_type_check CHECK (((passenger_space_type)::text = ANY ((ARRAY['SEATING_AREA'::character varying, 'PASSENGER_CABIN'::character varying, 'VEHICLE_AREA'::character varying, 'LUGGAGE_STORE'::character varying, 'CORRIDOR'::character varying, 'RESTAURANT'::character varying, 'TOILET'::character varying, 'BATHROOM'::character varying, 'OTHER'::character varying])::text[])))
);


ALTER TABLE public.passenger_space OWNER TO sobek;

--
-- TOC entry 290 (class 1259 OID 19877)
-- Name: passenger_space_deck_entrances; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_space_deck_entrances (
                                                       passenger_space_id bigint NOT NULL,
                                                       deck_entrances_id bigint NOT NULL
);


ALTER TABLE public.passenger_space_deck_entrances OWNER TO sobek;

--
-- TOC entry 291 (class 1259 OID 19880)
-- Name: passenger_space_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_space_key_values (
                                                   passenger_space_id bigint NOT NULL,
                                                   key_values_id bigint NOT NULL,
                                                   key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.passenger_space_key_values OWNER TO sobek;

--
-- TOC entry 292 (class 1259 OID 19885)
-- Name: passenger_space_passenger_spots; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_space_passenger_spots (
                                                        passenger_space_id bigint NOT NULL,
                                                        passenger_spots_id bigint NOT NULL
);


ALTER TABLE public.passenger_space_passenger_spots OWNER TO sobek;

--
-- TOC entry 321 (class 1259 OID 20085)
-- Name: passenger_space_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.passenger_space_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.passenger_space_seq OWNER TO sobek;

--
-- TOC entry 293 (class 1259 OID 19888)
-- Name: passenger_spot; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_spot (
                                       id bigint NOT NULL,
                                       netex_id character varying(255),
                                       changed timestamp(6) with time zone,
                                       created timestamp(6) with time zone,
                                       from_date timestamp(6) with time zone,
                                       to_date timestamp(6) with time zone,
                                       version bigint NOT NULL,
                                       changed_by character varying(255),
                                       version_comment character varying(255),
                                       description_lang character varying(5),
                                       description_value character varying(4000),
                                       name_lang character varying(5),
                                       name_value character varying(255),
                                       private_code_type character varying(255),
                                       private_code_value character varying(255),
                                       centroid public.geometry,
                                       height numeric(38,2),
                                       label_lang character varying(5),
                                       label_value character varying(255),
                                       length numeric(38,2),
                                       orientation character varying(255),
                                       width numeric(38,2),
                                       locatable_spot_type character varying(255),
                                       spot_row_ref character varying(255),
                                       has_armrest boolean,
                                       has_power boolean,
                                       is_between_seats boolean,
                                       is_by_aisle boolean,
                                       is_by_window boolean,
                                       is_facing_aisle boolean,
                                       is_facing_window boolean,
                                       is_in_end_row boolean,
                                       is_in_front_row boolean,
                                       leg_space numeric(38,2),
                                       table_type character varying(255),
                                       polygon_id bigint,
                                       CONSTRAINT passenger_spot_locatable_spot_type_check CHECK (((locatable_spot_type)::text = ANY ((ARRAY['SEAT'::character varying, 'BED'::character varying, 'STANDING_SPACE'::character varying, 'WHEELCHAIR_SPACE'::character varying, 'PUSHCHAIR_SPACE'::character varying, 'LUGGAGE_SPACE'::character varying, 'BICYCLE_SPACE'::character varying, 'VEHICLE_SPACE'::character varying, 'SPECIAL_SPACE'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT passenger_spot_orientation_check CHECK (((orientation)::text = ANY ((ARRAY['FORWARDS'::character varying, 'BACKWARDS'::character varying, 'UNKNOWN'::character varying, 'LEFTWARDS'::character varying, 'RIGHTWARDS'::character varying, 'ANGLED_LEFT'::character varying, 'ANGLED_RIGHT'::character varying, 'ANGLED_BACK_LEFT'::character varying, 'ANGLED_BACK_RIGHT'::character varying])::text[]))),
    CONSTRAINT passenger_spot_table_type_check CHECK (((table_type)::text = ANY ((ARRAY['NONE'::character varying, 'FIXED_FLAT'::character varying, 'FOLD_DOWN_FLAT'::character varying, 'SEAT_BACK_FOLDING'::character varying, 'ARM_REST_FOLDING'::character varying, 'SEAT_CLIPON'::character varying, 'OTHER'::character varying, 'UNKNOWN'::character varying])::text[])))
);


ALTER TABLE public.passenger_spot OWNER TO sobek;

--
-- TOC entry 294 (class 1259 OID 19899)
-- Name: passenger_spot_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.passenger_spot_key_values (
                                                  passenger_spot_id bigint NOT NULL,
                                                  key_values_id bigint NOT NULL,
                                                  key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.passenger_spot_key_values OWNER TO sobek;

--
-- TOC entry 322 (class 1259 OID 20087)
-- Name: passenger_spot_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.passenger_spot_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.passenger_spot_seq OWNER TO sobek;

--
-- TOC entry 295 (class 1259 OID 19904)
-- Name: persistable_polygon; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.persistable_polygon (
                                            id bigint NOT NULL,
                                            polygon public.geometry
);


ALTER TABLE public.persistable_polygon OWNER TO sobek;

--
-- TOC entry 323 (class 1259 OID 20089)
-- Name: persistable_polygon_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.persistable_polygon_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.persistable_polygon_seq OWNER TO sobek;


--
-- TOC entry 324 (class 1259 OID 20091)
-- Name: seq_multilingual_string_entity; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.seq_multilingual_string_entity
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_multilingual_string_entity OWNER TO sobek;

--
-- TOC entry 296 (class 1259 OID 19912)
-- Name: spot_row; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.spot_row (
                                 id bigint NOT NULL,
                                 netex_id character varying(255),
                                 changed timestamp(6) with time zone,
                                 created timestamp(6) with time zone,
                                 from_date timestamp(6) with time zone,
                                 to_date timestamp(6) with time zone,
                                 version bigint NOT NULL,
                                 label_lang character varying(5),
                                 label_value character varying(255),
                                 numbering_from_front boolean
);


ALTER TABLE public.spot_row OWNER TO sobek;

--
-- TOC entry 325 (class 1259 OID 20093)
-- Name: spot_row_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.spot_row_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.spot_row_seq OWNER TO sobek;

--
-- TOC entry 297 (class 1259 OID 19920)
-- Name: tag; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.tag (
                            netex_reference character varying(255) NOT NULL,
                            name character varying(255) NOT NULL,
                            comment character varying(255),
                            created timestamp(6) with time zone,
                            created_by character varying(255),
                            removed timestamp(6) with time zone,
                            removed_by character varying(255)
);


ALTER TABLE public.tag OWNER TO sobek;

--
-- TOC entry 298 (class 1259 OID 19928)
-- Name: train; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.train (
                              id bigint NOT NULL,
                              netex_id character varying(255),
                              changed timestamp(6) with time zone,
                              created timestamp(6) with time zone,
                              from_date timestamp(6) with time zone,
                              to_date timestamp(6) with time zone,
                              version bigint NOT NULL,
                              changed_by character varying(255),
                              version_comment character varying(255),
                              description_lang character varying(5),
                              description_value character varying(255),
                              euro_class character varying(255),
                              fuel_type character varying(255),
                              maximum_range numeric(38,2),
                              maximum_velocity numeric(38,2),
                              name_lang character varying(5),
                              name_value character varying(255),
                              type character varying(255),
                              value character varying(255),
                              propulsion_type character varying(255),
                              reversing_direction boolean,
                              self_propelled boolean,
                              short_name_lang character varying(5),
                              short_name_value character varying(255),
                              transport_mode character varying(255),
                              type_of_fuel character varying(255),
                              boarding_height numeric(38,2),
                              first_axle_height numeric(38,2),
                              gap_to_platform numeric(38,2),
                              has_hoist boolean,
                              has_lift_or_ramp boolean,
                              height numeric(38,2),
                              hoist_operating_radius numeric(38,2),
                              length numeric(38,2),
                              low_floor boolean,
                              monitored boolean,
                              weight numeric(38,2),
                              width numeric(38,2),
                              deck_plan_id bigint,
                              CONSTRAINT train_fuel_type_check CHECK (((fuel_type)::text = ANY ((ARRAY['BATTERY'::character varying, 'BIODIESEL'::character varying, 'DIESEL'::character varying, 'DIESEL_BATTERY_HYBRID'::character varying, 'ELECTRIC_CONTACT'::character varying, 'ELECTRICITY'::character varying, 'ETHANOL'::character varying, 'HYDROGEN'::character varying, 'LIQUID_GAS'::character varying, 'TPG'::character varying, 'METHANE'::character varying, 'NATURAL_GAS'::character varying, 'PETROL'::character varying, 'PETROL_BATTERY_HYBRID'::character varying, 'PETROL_LEADED'::character varying, 'PETROL_UNLEADED'::character varying, 'NONE'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT train_propulsion_type_check CHECK (((propulsion_type)::text = ANY ((ARRAY['COMBUSTION'::character varying, 'ELECTRIC'::character varying, 'ELECTRIC_ASSIST'::character varying, 'HYBRID'::character varying, 'HUMAN'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT train_transport_mode_check CHECK (((transport_mode)::text = ANY ((ARRAY['ALL'::character varying, 'UNKNOWN'::character varying, 'BUS'::character varying, 'TROLLEY_BUS'::character varying, 'TRAM'::character varying, 'COACH'::character varying, 'RAIL'::character varying, 'INTERCITY_RAIL'::character varying, 'URBAN_RAIL'::character varying, 'METRO'::character varying, 'AIR'::character varying, 'WATER'::character varying, 'CABLEWAY'::character varying, 'FUNICULAR'::character varying, 'SNOW_AND_ICE'::character varying, 'TAXI'::character varying, 'FERRY'::character varying, 'LIFT'::character varying, 'SELF_DRIVE'::character varying, 'ANY_MODE'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT train_type_of_fuel_check CHECK (((type_of_fuel)::text = ANY ((ARRAY['BATTERY'::character varying, 'BIODIESEL'::character varying, 'DIESEL'::character varying, 'DIESEL_BATTERY_HYBRID'::character varying, 'ELECTRIC_CONTACT'::character varying, 'ELECTRICITY'::character varying, 'ETHANOL'::character varying, 'HYDROGEN'::character varying, 'LIQUID_GAS'::character varying, 'TPG'::character varying, 'METHANE'::character varying, 'NATURAL_GAS'::character varying, 'PETROL'::character varying, 'PETROL_BATTERY_HYBRID'::character varying, 'PETROL_LEADED'::character varying, 'PETROL_UNLEADED'::character varying, 'NONE'::character varying, 'OTHER'::character varying])::text[])))
);


ALTER TABLE public.train OWNER TO sobek;

--
-- TOC entry 299 (class 1259 OID 19940)
-- Name: train_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.train_key_values (
                                         train_id bigint NOT NULL,
                                         key_values_id bigint NOT NULL,
                                         key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.train_key_values OWNER TO sobek;

--
-- TOC entry 326 (class 1259 OID 20095)
-- Name: train_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.train_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.train_seq OWNER TO sobek;

--
-- TOC entry 300 (class 1259 OID 19945)
-- Name: value; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.value (
                              id bigint NOT NULL
);


ALTER TABLE public.value OWNER TO sobek;

--
-- TOC entry 301 (class 1259 OID 19950)
-- Name: value_items; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.value_items (
                                    value_id bigint NOT NULL,
                                    items character varying(255)
);


ALTER TABLE public.value_items OWNER TO sobek;

--
-- TOC entry 327 (class 1259 OID 20097)
-- Name: value_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.value_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.value_seq OWNER TO sobek;

--
-- TOC entry 302 (class 1259 OID 19953)
-- Name: vehicle; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle (
                                id bigint NOT NULL,
                                netex_id character varying(255),
                                changed timestamp(6) with time zone,
                                created timestamp(6) with time zone,
                                from_date timestamp(6) with time zone,
                                to_date timestamp(6) with time zone,
                                version bigint NOT NULL,
                                changed_by character varying(255),
                                version_comment character varying(255),
                                build_date timestamp(6) with time zone,
                                chassis_number character varying(255),
                                description_lang character varying(5),
                                description_value character varying(255),
                                monitored boolean,
                                name_lang character varying(5),
                                name_value character varying(255),
                                operational_number character varying(255),
                                type character varying(255),
                                value character varying(255),
                                registration_date timestamp(6) with time zone,
                                registration_number character varying(255),
                                short_name_lang character varying(5),
                                short_name_value character varying(255),
                                transport_type_id bigint,
                                vehicle_model_id bigint
);


ALTER TABLE public.vehicle OWNER TO sobek;

--
-- TOC entry 304 (class 1259 OID 19966)
-- Name: vehicle_equipment_profile; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_equipment_profile (
                                                  id bigint NOT NULL,
                                                  netex_id character varying(255),
                                                  changed timestamp(6) with time zone,
                                                  created timestamp(6) with time zone,
                                                  from_date timestamp(6) with time zone,
                                                  to_date timestamp(6) with time zone,
                                                  version bigint NOT NULL,
                                                  changed_by character varying(255),
                                                  version_comment character varying(255)
);


ALTER TABLE public.vehicle_equipment_profile OWNER TO sobek;

--
-- TOC entry 305 (class 1259 OID 19974)
-- Name: vehicle_equipment_profile_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_equipment_profile_key_values (
                                                             vehicle_equipment_profile_id bigint NOT NULL,
                                                             key_values_id bigint NOT NULL,
                                                             key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.vehicle_equipment_profile_key_values OWNER TO sobek;

--
-- TOC entry 307 (class 1259 OID 19982)
-- Name: vehicle_equipment_profile_member; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_equipment_profile_member (
                                                         id bigint NOT NULL,
                                                         netex_id character varying(255),
                                                         changed timestamp(6) with time zone,
                                                         created timestamp(6) with time zone,
                                                         from_date timestamp(6) with time zone,
                                                         to_date timestamp(6) with time zone,
                                                         version bigint NOT NULL,
                                                         equipment_ref character varying(255),
                                                         minimum_units numeric(38,0)
);


ALTER TABLE public.vehicle_equipment_profile_member OWNER TO sobek;

--
-- TOC entry 328 (class 1259 OID 20099)
-- Name: vehicle_equipment_profile_member_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.vehicle_equipment_profile_member_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_equipment_profile_member_seq OWNER TO sobek;

--
-- TOC entry 329 (class 1259 OID 20101)
-- Name: vehicle_equipment_profile_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.vehicle_equipment_profile_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_equipment_profile_seq OWNER TO sobek;

--
-- TOC entry 306 (class 1259 OID 19979)
-- Name: vehicle_equipment_profile_vehicle_equipment_profile_members; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_equipment_profile_vehicle_equipment_profile_members (
                                                                                    vehicle_equipment_profile_id bigint NOT NULL,
                                                                                    vehicle_equipment_profile_members_id bigint NOT NULL
);


ALTER TABLE public.vehicle_equipment_profile_vehicle_equipment_profile_members OWNER TO sobek;

--
-- TOC entry 303 (class 1259 OID 19961)
-- Name: vehicle_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_key_values (
                                           vehicle_id bigint NOT NULL,
                                           key_values_id bigint NOT NULL,
                                           key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.vehicle_key_values OWNER TO sobek;

--
-- TOC entry 308 (class 1259 OID 19990)
-- Name: vehicle_model; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_model (
                                      id bigint NOT NULL,
                                      netex_id character varying(255),
                                      changed timestamp(6) with time zone,
                                      created timestamp(6) with time zone,
                                      from_date timestamp(6) with time zone,
                                      to_date timestamp(6) with time zone,
                                      version bigint NOT NULL,
                                      changed_by character varying(255),
                                      version_comment character varying(255),
                                      description_lang character varying(5),
                                      description_value character varying(255),
                                      full_charge numeric(38,2),
                                      manufacturer_lang character varying(5),
                                      manufacturer_value character varying(255),
                                      name_lang character varying(5),
                                      name_value character varying(255),
                                      range numeric(38,2),
                                      transport_type_ref character varying(255)
);


ALTER TABLE public.vehicle_model OWNER TO sobek;

--
-- TOC entry 309 (class 1259 OID 19998)
-- Name: vehicle_model_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_model_key_values (
                                                 vehicle_model_id bigint NOT NULL,
                                                 key_values_id bigint NOT NULL,
                                                 key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.vehicle_model_key_values OWNER TO sobek;

--
-- TOC entry 330 (class 1259 OID 20103)
-- Name: vehicle_model_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.vehicle_model_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_model_seq OWNER TO sobek;

--
-- TOC entry 331 (class 1259 OID 20105)
-- Name: vehicle_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.vehicle_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_seq OWNER TO sobek;

--
-- TOC entry 310 (class 1259 OID 20003)
-- Name: vehicle_type; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_type (
                                     id bigint NOT NULL,
                                     netex_id character varying(255),
                                     changed timestamp(6) with time zone,
                                     created timestamp(6) with time zone,
                                     from_date timestamp(6) with time zone,
                                     to_date timestamp(6) with time zone,
                                     version bigint NOT NULL,
                                     changed_by character varying(255),
                                     version_comment character varying(255),
                                     description_lang character varying(5),
                                     description_value character varying(255),
                                     euro_class character varying(255),
                                     fuel_type character varying(255),
                                     maximum_range numeric(38,2),
                                     maximum_velocity numeric(38,2),
                                     name_lang character varying(5),
                                     name_value character varying(255),
                                     type character varying(255),
                                     value character varying(255),
                                     propulsion_type character varying(255),
                                     reversing_direction boolean,
                                     self_propelled boolean,
                                     short_name_lang character varying(5),
                                     short_name_value character varying(255),
                                     transport_mode character varying(255),
                                     type_of_fuel character varying(255),
                                     boarding_height numeric(38,2),
                                     first_axle_height numeric(38,2),
                                     gap_to_platform numeric(38,2),
                                     has_hoist boolean,
                                     has_lift_or_ramp boolean,
                                     height numeric(38,2),
                                     hoist_operating_radius numeric(38,2),
                                     length numeric(38,2),
                                     low_floor boolean,
                                     monitored boolean,
                                     weight numeric(38,2),
                                     width numeric(38,2),
                                     deck_plan_id bigint,
                                     passenger_capacity_id bigint,
                                     CONSTRAINT vehicle_type_fuel_type_check CHECK (((fuel_type)::text = ANY ((ARRAY['BATTERY'::character varying, 'BIODIESEL'::character varying, 'DIESEL'::character varying, 'DIESEL_BATTERY_HYBRID'::character varying, 'ELECTRIC_CONTACT'::character varying, 'ELECTRICITY'::character varying, 'ETHANOL'::character varying, 'HYDROGEN'::character varying, 'LIQUID_GAS'::character varying, 'TPG'::character varying, 'METHANE'::character varying, 'NATURAL_GAS'::character varying, 'PETROL'::character varying, 'PETROL_BATTERY_HYBRID'::character varying, 'PETROL_LEADED'::character varying, 'PETROL_UNLEADED'::character varying, 'NONE'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT vehicle_type_propulsion_type_check CHECK (((propulsion_type)::text = ANY ((ARRAY['COMBUSTION'::character varying, 'ELECTRIC'::character varying, 'ELECTRIC_ASSIST'::character varying, 'HYBRID'::character varying, 'HUMAN'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT vehicle_type_transport_mode_check CHECK (((transport_mode)::text = ANY ((ARRAY['ALL'::character varying, 'UNKNOWN'::character varying, 'BUS'::character varying, 'TROLLEY_BUS'::character varying, 'TRAM'::character varying, 'COACH'::character varying, 'RAIL'::character varying, 'INTERCITY_RAIL'::character varying, 'URBAN_RAIL'::character varying, 'METRO'::character varying, 'AIR'::character varying, 'WATER'::character varying, 'CABLEWAY'::character varying, 'FUNICULAR'::character varying, 'SNOW_AND_ICE'::character varying, 'TAXI'::character varying, 'FERRY'::character varying, 'LIFT'::character varying, 'SELF_DRIVE'::character varying, 'ANY_MODE'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT vehicle_type_type_of_fuel_check CHECK (((type_of_fuel)::text = ANY ((ARRAY['BATTERY'::character varying, 'BIODIESEL'::character varying, 'DIESEL'::character varying, 'DIESEL_BATTERY_HYBRID'::character varying, 'ELECTRIC_CONTACT'::character varying, 'ELECTRICITY'::character varying, 'ETHANOL'::character varying, 'HYDROGEN'::character varying, 'LIQUID_GAS'::character varying, 'TPG'::character varying, 'METHANE'::character varying, 'NATURAL_GAS'::character varying, 'PETROL'::character varying, 'PETROL_BATTERY_HYBRID'::character varying, 'PETROL_LEADED'::character varying, 'PETROL_UNLEADED'::character varying, 'NONE'::character varying, 'OTHER'::character varying])::text[])))
);


ALTER TABLE public.vehicle_type OWNER TO sobek;

--
-- TOC entry 311 (class 1259 OID 20015)
-- Name: vehicle_type_key_values; Type: TABLE; Schema: public; Owner: sobek
--

CREATE TABLE public.vehicle_type_key_values (
                                                vehicle_type_id bigint NOT NULL,
                                                key_values_id bigint NOT NULL,
                                                key_values_key character varying(255) NOT NULL
);


ALTER TABLE public.vehicle_type_key_values OWNER TO sobek;

--
-- TOC entry 332 (class 1259 OID 20107)
-- Name: vehicle_type_seq; Type: SEQUENCE; Schema: public; Owner: sobek
--

CREATE SEQUENCE public.vehicle_type_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_type_seq OWNER TO sobek;


--
-- TOC entry 4736 (class 0 OID 0)
-- Dependencies: 312
-- Name: accessibility_assessment_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.accessibility_assessment_seq', 1, false);


--
-- TOC entry 4737 (class 0 OID 0)
-- Dependencies: 313
-- Name: accessibility_limitation_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.accessibility_limitation_seq', 1, false);


--
-- TOC entry 4738 (class 0 OID 0)
-- Dependencies: 314
-- Name: alternative_name_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.alternative_name_seq', 1, false);


--
-- TOC entry 4739 (class 0 OID 0)
-- Dependencies: 315
-- Name: deck_plan_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.deck_plan_seq', 1, true);


--
-- TOC entry 4740 (class 0 OID 0)
-- Dependencies: 316
-- Name: deck_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.deck_seq', 1, false);


--
-- TOC entry 4741 (class 0 OID 0)
-- Dependencies: 317
-- Name: export_job_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.export_job_seq', 1, false);


--
-- TOC entry 4742 (class 0 OID 0)
-- Dependencies: 318
-- Name: installed_equipment_version_structure_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.installed_equipment_version_structure_seq', 1, false);


--
-- TOC entry 4743 (class 0 OID 0)
-- Dependencies: 319
-- Name: passenger_capacity_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.passenger_capacity_seq', 1, true);


--
-- TOC entry 4744 (class 0 OID 0)
-- Dependencies: 320
-- Name: passenger_entrance_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.passenger_entrance_seq', 1, false);


--
-- TOC entry 4745 (class 0 OID 0)
-- Dependencies: 321
-- Name: passenger_space_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.passenger_space_seq', 1, false);


--
-- TOC entry 4746 (class 0 OID 0)
-- Dependencies: 322
-- Name: passenger_spot_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.passenger_spot_seq', 1, false);


--
-- TOC entry 4747 (class 0 OID 0)
-- Dependencies: 323
-- Name: persistable_polygon_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.persistable_polygon_seq', 1, false);


--
-- TOC entry 4748 (class 0 OID 0)
-- Dependencies: 324
-- Name: seq_multilingual_string_entity; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.seq_multilingual_string_entity', 1, false);


--
-- TOC entry 4749 (class 0 OID 0)
-- Dependencies: 325
-- Name: spot_row_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.spot_row_seq', 1, false);


--
-- TOC entry 4750 (class 0 OID 0)
-- Dependencies: 326
-- Name: train_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.train_seq', 1, false);


--
-- TOC entry 4751 (class 0 OID 0)
-- Dependencies: 327
-- Name: value_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.value_seq', 1, true);


--
-- TOC entry 4752 (class 0 OID 0)
-- Dependencies: 328
-- Name: vehicle_equipment_profile_member_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.vehicle_equipment_profile_member_seq', 1, false);


--
-- TOC entry 4753 (class 0 OID 0)
-- Dependencies: 329
-- Name: vehicle_equipment_profile_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.vehicle_equipment_profile_seq', 1, false);


--
-- TOC entry 4754 (class 0 OID 0)
-- Dependencies: 330
-- Name: vehicle_model_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.vehicle_model_seq', 1, true);


--
-- TOC entry 4755 (class 0 OID 0)
-- Dependencies: 331
-- Name: vehicle_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.vehicle_seq', 1, true);


--
-- TOC entry 4756 (class 0 OID 0)
-- Dependencies: 332
-- Name: vehicle_type_seq; Type: SEQUENCE SET; Schema: public; Owner: sobek
--

SELECT pg_catalog.setval('public.vehicle_type_seq', 1, true);


--
-- TOC entry 4372 (class 2606 OID 19747)
-- Name: accessibility_assessment accessibility_assessment_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.accessibility_assessment
    ADD CONSTRAINT accessibility_assessment_pkey PRIMARY KEY (id);


--
-- TOC entry 4376 (class 2606 OID 19764)
-- Name: accessibility_limitation accessibility_limitation_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.accessibility_limitation
    ADD CONSTRAINT accessibility_limitation_pkey PRIMARY KEY (id);


--
-- TOC entry 4378 (class 2606 OID 19773)
-- Name: alternative_name alternative_name_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.alternative_name
    ADD CONSTRAINT alternative_name_pkey PRIMARY KEY (id);


--
-- TOC entry 4386 (class 2606 OID 19789)
-- Name: deck_key_values deck_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_key_values
    ADD CONSTRAINT deck_key_values_pkey PRIMARY KEY (deck_id, key_values_key);


--
-- TOC entry 4380 (class 2606 OID 19781)
-- Name: deck deck_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_pkey PRIMARY KEY (id);


--
-- TOC entry 4396 (class 2606 OID 19809)
-- Name: deck_plan_key_values deck_plan_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_key_values
    ADD CONSTRAINT deck_plan_key_values_pkey PRIMARY KEY (deck_plan_id, key_values_key);


--
-- TOC entry 4392 (class 2606 OID 19801)
-- Name: deck_plan deck_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan
    ADD CONSTRAINT deck_plan_pkey PRIMARY KEY (id);


--
-- TOC entry 4400 (class 2606 OID 19818)
-- Name: export_job export_job_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.export_job
    ADD CONSTRAINT export_job_pkey PRIMARY KEY (id);


--
-- TOC entry 4402 (class 2606 OID 19828)
-- Name: installed_equipment_version_structure installed_equipment_version_structure_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.installed_equipment_version_structure
    ADD CONSTRAINT installed_equipment_version_structure_pkey PRIMARY KEY (id);


--
-- TOC entry 4404 (class 2606 OID 19833)
-- Name: multilingual_string_entity multilingual_string_entity_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.multilingual_string_entity
    ADD CONSTRAINT multilingual_string_entity_pkey PRIMARY KEY (id);


--
-- TOC entry 4408 (class 2606 OID 19847)
-- Name: passenger_capacity_key_values passenger_capacity_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_capacity_key_values
    ADD CONSTRAINT passenger_capacity_key_values_pkey PRIMARY KEY (passenger_capacity_id, key_values_key);


--
-- TOC entry 4406 (class 2606 OID 19842)
-- Name: passenger_capacity passenger_capacity_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_capacity
    ADD CONSTRAINT passenger_capacity_pkey PRIMARY KEY (id);


--
-- TOC entry 4416 (class 2606 OID 19864)
-- Name: passenger_entrance_key_values passenger_entrance_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance_key_values
    ADD CONSTRAINT passenger_entrance_key_values_pkey PRIMARY KEY (passenger_entrance_id, key_values_key);


--
-- TOC entry 4412 (class 2606 OID 19859)
-- Name: passenger_entrance passenger_entrance_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance
    ADD CONSTRAINT passenger_entrance_pkey PRIMARY KEY (id);


--
-- TOC entry 4426 (class 2606 OID 19884)
-- Name: passenger_space_key_values passenger_space_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_key_values
    ADD CONSTRAINT passenger_space_key_values_pkey PRIMARY KEY (passenger_space_id, key_values_key);


--
-- TOC entry 4420 (class 2606 OID 19876)
-- Name: passenger_space passenger_space_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space
    ADD CONSTRAINT passenger_space_pkey PRIMARY KEY (id);


--
-- TOC entry 4436 (class 2606 OID 19903)
-- Name: passenger_spot_key_values passenger_spot_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot_key_values
    ADD CONSTRAINT passenger_spot_key_values_pkey PRIMARY KEY (passenger_spot_id, key_values_key);


--
-- TOC entry 4432 (class 2606 OID 19898)
-- Name: passenger_spot passenger_spot_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot
    ADD CONSTRAINT passenger_spot_pkey PRIMARY KEY (id);


--
-- TOC entry 4440 (class 2606 OID 19911)
-- Name: persistable_polygon persistable_polygon_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.persistable_polygon
    ADD CONSTRAINT persistable_polygon_pkey PRIMARY KEY (id);





--
-- TOC entry 4442 (class 2606 OID 19919)
-- Name: spot_row spot_row_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.spot_row
    ADD CONSTRAINT spot_row_pkey PRIMARY KEY (id);


--
-- TOC entry 4444 (class 2606 OID 19927)
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (netex_reference, name);


--
-- TOC entry 4448 (class 2606 OID 19944)
-- Name: train_key_values train_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train_key_values
    ADD CONSTRAINT train_key_values_pkey PRIMARY KEY (train_id, key_values_key);


--
-- TOC entry 4446 (class 2606 OID 19939)
-- Name: train train_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train
    ADD CONSTRAINT train_pkey PRIMARY KEY (id);


--
-- TOC entry 4473 (class 2606 OID 20062)
-- Name: vehicle_model_key_values uk1omaouiixyfartxb8r7ix45ec; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_model_key_values
    ADD CONSTRAINT uk1omaouiixyfartxb8r7ix45ec UNIQUE (key_values_id);


--
-- TOC entry 4382 (class 2606 OID 20023)
-- Name: deck uk2205e95xec7qwrkq76flxc7h0; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT uk2205e95xec7qwrkq76flxc7h0 UNIQUE (polygon_id);


--
-- TOC entry 4418 (class 2606 OID 20039)
-- Name: passenger_entrance_key_values uk27qan4likf3xm5xqq7u5avxxh; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance_key_values
    ADD CONSTRAINT uk27qan4likf3xm5xqq7u5avxxh UNIQUE (key_values_id);


--
-- TOC entry 4424 (class 2606 OID 20043)
-- Name: passenger_space_deck_entrances uk2us9pdddpekgd2gewssu2er4o; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_deck_entrances
    ADD CONSTRAINT uk2us9pdddpekgd2gewssu2er4o UNIQUE (deck_entrances_id);


--
-- TOC entry 4422 (class 2606 OID 20041)
-- Name: passenger_space uk3k0snw6rescjayifos2wklshi; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space
    ADD CONSTRAINT uk3k0snw6rescjayifos2wklshi UNIQUE (polygon_id);


--
-- TOC entry 4398 (class 2606 OID 20033)
-- Name: deck_plan_key_values uk6ecdj6lp1ke1opny508sy8pcx; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_key_values
    ADD CONSTRAINT uk6ecdj6lp1ke1opny508sy8pcx UNIQUE (key_values_id);


--
-- TOC entry 4434 (class 2606 OID 20049)
-- Name: passenger_spot uk6figoc5cqqcny6e0618w0wekw; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot
    ADD CONSTRAINT uk6figoc5cqqcny6e0618w0wekw UNIQUE (polygon_id);


--
-- TOC entry 4467 (class 2606 OID 20060)
-- Name: vehicle_equipment_profile_vehicle_equipment_profile_members uk6xa3h840hmi06ecc47uqeexkj; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_vehicle_equipment_profile_members
    ADD CONSTRAINT uk6xa3h840hmi06ecc47uqeexkj UNIQUE (vehicle_equipment_profile_members_id);


--
-- TOC entry 4374 (class 2606 OID 20021)
-- Name: accessibility_assessment_limitations ukaeu5728ehva06k95lioaubr8s; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.accessibility_assessment_limitations
    ADD CONSTRAINT ukaeu5728ehva06k95lioaubr8s UNIQUE (limitations_id);


--
-- TOC entry 4481 (class 2606 OID 20066)
-- Name: vehicle_type_key_values ukaqjd8yc0j5tje6fhf3f4bw2bv; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type_key_values
    ADD CONSTRAINT ukaqjd8yc0j5tje6fhf3f4bw2bv UNIQUE (key_values_id);


--
-- TOC entry 4388 (class 2606 OID 20027)
-- Name: deck_key_values ukbq4c5fwxf0t5n0hvy0xs49se5; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_key_values
    ADD CONSTRAINT ukbq4c5fwxf0t5n0hvy0xs49se5 UNIQUE (key_values_id);


--
-- TOC entry 4410 (class 2606 OID 20035)
-- Name: passenger_capacity_key_values ukcxisakay9p2p01uv7yrn3nga9; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_capacity_key_values
    ADD CONSTRAINT ukcxisakay9p2p01uv7yrn3nga9 UNIQUE (key_values_id);


--
-- TOC entry 4457 (class 2606 OID 20056)
-- Name: vehicle_key_values ukfdgml3d7ivjnp7nsvc8dg21j0; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_key_values
    ADD CONSTRAINT ukfdgml3d7ivjnp7nsvc8dg21j0 UNIQUE (key_values_id);


--
-- TOC entry 4384 (class 2606 OID 20025)
-- Name: deck_deck_spaces ukfiftfixtomyohsc5ti5dpgmtn; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_deck_spaces
    ADD CONSTRAINT ukfiftfixtomyohsc5ti5dpgmtn UNIQUE (deck_spaces_id);


--
-- TOC entry 4390 (class 2606 OID 20029)
-- Name: deck_spot_rows ukhlmgev6hl4tckx6cmktyelwao; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_spot_rows
    ADD CONSTRAINT ukhlmgev6hl4tckx6cmktyelwao UNIQUE (spot_rows_id);


--
-- TOC entry 4450 (class 2606 OID 20053)
-- Name: train_key_values ukil9qln3trobt2dsaepweynyqk; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train_key_values
    ADD CONSTRAINT ukil9qln3trobt2dsaepweynyqk UNIQUE (key_values_id);


--
-- TOC entry 4438 (class 2606 OID 20051)
-- Name: passenger_spot_key_values ukim2lwkhd73p0ewy3o27ixksd3; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot_key_values
    ADD CONSTRAINT ukim2lwkhd73p0ewy3o27ixksd3 UNIQUE (key_values_id);


--
-- TOC entry 4477 (class 2606 OID 20064)
-- Name: vehicle_type ukmln603qi6kd6butn2wgjvblmx; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type
    ADD CONSTRAINT ukmln603qi6kd6butn2wgjvblmx UNIQUE (passenger_capacity_id);


--
-- TOC entry 4463 (class 2606 OID 20058)
-- Name: vehicle_equipment_profile_key_values uknwm33005w1bts83simlfyxq12; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_key_values
    ADD CONSTRAINT uknwm33005w1bts83simlfyxq12 UNIQUE (key_values_id);


--
-- TOC entry 4414 (class 2606 OID 20037)
-- Name: passenger_entrance uktkbv70649jxuee6yupvw463r6; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance
    ADD CONSTRAINT uktkbv70649jxuee6yupvw463r6 UNIQUE (polygon_id);


--
-- TOC entry 4428 (class 2606 OID 20045)
-- Name: passenger_space_key_values ukuaahm4oyn9guhc8pnasl5w3; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_key_values
    ADD CONSTRAINT ukuaahm4oyn9guhc8pnasl5w3 UNIQUE (key_values_id);


--
-- TOC entry 4430 (class 2606 OID 20047)
-- Name: passenger_space_passenger_spots ukxf28xcsh04wx6sqhkm4su7lb; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_passenger_spots
    ADD CONSTRAINT ukxf28xcsh04wx6sqhkm4su7lb UNIQUE (passenger_spots_id);


--
-- TOC entry 4394 (class 2606 OID 20031)
-- Name: deck_plan_decks ukybo7w2dj4yksoy8dddput5f7; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_decks
    ADD CONSTRAINT ukybo7w2dj4yksoy8dddput5f7 UNIQUE (decks_id);


--
-- TOC entry 4452 (class 2606 OID 19949)
-- Name: value value_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.value
    ADD CONSTRAINT value_pkey PRIMARY KEY (id);


--
-- TOC entry 4465 (class 2606 OID 19978)
-- Name: vehicle_equipment_profile_key_values vehicle_equipment_profile_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_key_values
    ADD CONSTRAINT vehicle_equipment_profile_key_values_pkey PRIMARY KEY (vehicle_equipment_profile_id, key_values_key);


--
-- TOC entry 4469 (class 2606 OID 19989)
-- Name: vehicle_equipment_profile_member vehicle_equipment_profile_member_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_member
    ADD CONSTRAINT vehicle_equipment_profile_member_pkey PRIMARY KEY (id);


--
-- TOC entry 4461 (class 2606 OID 19973)
-- Name: vehicle_equipment_profile vehicle_equipment_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile
    ADD CONSTRAINT vehicle_equipment_profile_pkey PRIMARY KEY (id);


--
-- TOC entry 4459 (class 2606 OID 19965)
-- Name: vehicle_key_values vehicle_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_key_values
    ADD CONSTRAINT vehicle_key_values_pkey PRIMARY KEY (vehicle_id, key_values_key);


--
-- TOC entry 4475 (class 2606 OID 20002)
-- Name: vehicle_model_key_values vehicle_model_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_model_key_values
    ADD CONSTRAINT vehicle_model_key_values_pkey PRIMARY KEY (vehicle_model_id, key_values_key);


--
-- TOC entry 4471 (class 2606 OID 19997)
-- Name: vehicle_model vehicle_model_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_model
    ADD CONSTRAINT vehicle_model_pkey PRIMARY KEY (id);


--
-- TOC entry 4455 (class 2606 OID 19960)
-- Name: vehicle vehicle_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT vehicle_pkey PRIMARY KEY (id);


--
-- TOC entry 4483 (class 2606 OID 20019)
-- Name: vehicle_type_key_values vehicle_type_key_values_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type_key_values
    ADD CONSTRAINT vehicle_type_key_values_pkey PRIMARY KEY (vehicle_type_id, key_values_key);


--
-- TOC entry 4479 (class 2606 OID 20014)
-- Name: vehicle_type vehicle_type_pkey; Type: CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type
    ADD CONSTRAINT vehicle_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4369 (class 1259 OID 19738)
-- Name: id_value_index; Type: INDEX; Schema: public; Owner: sobek
--

--CREATE INDEX id_value_index ON public.id_generator USING btree (id_value);


--
-- TOC entry 4453 (class 1259 OID 20054)
-- Name: items_index; Type: INDEX; Schema: public; Owner: sobek
--

CREATE INDEX items_index ON public.value_items USING btree (items);



--
-- TOC entry 4370 (class 1259 OID 19737)
-- Name: table_name_index; Type: INDEX; Schema: public; Owner: sobek
--

-- CREATE INDEX table_name_index ON public.id_generator USING btree (table_name);


--
-- TOC entry 4497 (class 2606 OID 20174)
-- Name: passenger_capacity_key_values fk1px95rscwii4e37t469uxm0f6; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_capacity_key_values
    ADD CONSTRAINT fk1px95rscwii4e37t469uxm0f6 FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4509 (class 2606 OID 20234)
-- Name: passenger_spot fk2pjm2pakaifla5yq7r1cxqhhc; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot
    ADD CONSTRAINT fk2pjm2pakaifla5yq7r1cxqhhc FOREIGN KEY (polygon_id) REFERENCES public.persistable_polygon(id);


--
-- TOC entry 4489 (class 2606 OID 20134)
-- Name: deck_key_values fk39w5ehqf27pyr08sp15noytqt; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_key_values
    ADD CONSTRAINT fk39w5ehqf27pyr08sp15noytqt FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4486 (class 2606 OID 20119)
-- Name: deck fk44eea0l13l332raghan366lwe; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT fk44eea0l13l332raghan366lwe FOREIGN KEY (polygon_id) REFERENCES public.persistable_polygon(id);


--
-- TOC entry 4493 (class 2606 OID 20159)
-- Name: deck_plan_decks fk4wvol0ns2ya6ph8l3df0ylnww; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_decks
    ADD CONSTRAINT fk4wvol0ns2ya6ph8l3df0ylnww FOREIGN KEY (deck_plan_id) REFERENCES public.deck_plan(id);


--
-- TOC entry 4502 (class 2606 OID 20199)
-- Name: passenger_space fk61o49ug408j3q1422s1apqfh7; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space
    ADD CONSTRAINT fk61o49ug408j3q1422s1apqfh7 FOREIGN KEY (polygon_id) REFERENCES public.persistable_polygon(id);


--
-- TOC entry 4520 (class 2606 OID 20289)
-- Name: vehicle_equipment_profile_key_values fk64bd9ffaj36c37c70e8lhrfj2; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_key_values
    ADD CONSTRAINT fk64bd9ffaj36c37c70e8lhrfj2 FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4513 (class 2606 OID 20254)
-- Name: train_key_values fk69vy3a09ikk101t624gv8fhhb; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train_key_values
    ADD CONSTRAINT fk69vy3a09ikk101t624gv8fhhb FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4487 (class 2606 OID 20124)
-- Name: deck_deck_spaces fk6xsy50t2659mj45rkw4qi5dcw; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_deck_spaces
    ADD CONSTRAINT fk6xsy50t2659mj45rkw4qi5dcw FOREIGN KEY (deck_spaces_id) REFERENCES public.passenger_space(id);


--
-- TOC entry 4484 (class 2606 OID 20109)
-- Name: accessibility_assessment_limitations fk71lv2d2xdl6il9t8lxhiw2oxr; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.accessibility_assessment_limitations
    ADD CONSTRAINT fk71lv2d2xdl6il9t8lxhiw2oxr FOREIGN KEY (limitations_id) REFERENCES public.accessibility_limitation(id);


--
-- TOC entry 4518 (class 2606 OID 20279)
-- Name: vehicle_key_values fk7e6dgl7bvmhpmtfx1fif9i1ah; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_key_values
    ADD CONSTRAINT fk7e6dgl7bvmhpmtfx1fif9i1ah FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4507 (class 2606 OID 20224)
-- Name: passenger_space_passenger_spots fk7heqymvtxduigg4yh6p0i0bti; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_passenger_spots
    ADD CONSTRAINT fk7heqymvtxduigg4yh6p0i0bti FOREIGN KEY (passenger_spots_id) REFERENCES public.passenger_spot(id);


--
-- TOC entry 4499 (class 2606 OID 20184)
-- Name: passenger_entrance fk7slnbe6y84ihqtvuanef8sp65; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance
    ADD CONSTRAINT fk7slnbe6y84ihqtvuanef8sp65 FOREIGN KEY (polygon_id) REFERENCES public.persistable_polygon(id);


--
-- TOC entry 4505 (class 2606 OID 20214)
-- Name: passenger_space_key_values fk81th26cygaxpmja0lay2orey7; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_key_values
    ADD CONSTRAINT fk81th26cygaxpmja0lay2orey7 FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4526 (class 2606 OID 20324)
-- Name: vehicle_type fk83q81ts31vukqcjmlpxg70i2b; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type
    ADD CONSTRAINT fk83q81ts31vukqcjmlpxg70i2b FOREIGN KEY (passenger_capacity_id) REFERENCES public.passenger_capacity(id);


--
-- TOC entry 4495 (class 2606 OID 20169)
-- Name: deck_plan_key_values fk8shpsx4k8q7a2tfm2lpkiese7; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_key_values
    ADD CONSTRAINT fk8shpsx4k8q7a2tfm2lpkiese7 FOREIGN KEY (deck_plan_id) REFERENCES public.deck_plan(id);


--
-- TOC entry 4522 (class 2606 OID 20304)
-- Name: vehicle_equipment_profile_vehicle_equipment_profile_members fk9oj7wg9h65js9h31hjuclxm3q; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_vehicle_equipment_profile_members
    ADD CONSTRAINT fk9oj7wg9h65js9h31hjuclxm3q FOREIGN KEY (vehicle_equipment_profile_id) REFERENCES public.vehicle_equipment_profile(id);


--
-- TOC entry 4491 (class 2606 OID 20149)
-- Name: deck_spot_rows fkbk3cn4dpbwof983wlfq1t2dn1; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_spot_rows
    ADD CONSTRAINT fkbk3cn4dpbwof983wlfq1t2dn1 FOREIGN KEY (deck_id) REFERENCES public.deck(id);


--
-- TOC entry 4524 (class 2606 OID 20309)
-- Name: vehicle_model_key_values fke9kanp31wgd4296cbbkbeudlv; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_model_key_values
    ADD CONSTRAINT fke9kanp31wgd4296cbbkbeudlv FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4528 (class 2606 OID 20329)
-- Name: vehicle_type_key_values fkfpvdooa1fqfxa0j9fnw0e0yih; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type_key_values
    ADD CONSTRAINT fkfpvdooa1fqfxa0j9fnw0e0yih FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4503 (class 2606 OID 20204)
-- Name: passenger_space_deck_entrances fkgh0sitxlgx1tkayufteno2frk; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_deck_entrances
    ADD CONSTRAINT fkgh0sitxlgx1tkayufteno2frk FOREIGN KEY (deck_entrances_id) REFERENCES public.passenger_entrance(id);


--
-- TOC entry 4529 (class 2606 OID 20334)
-- Name: vehicle_type_key_values fkhn6q00n3oexuto8sxnqrk3p8o; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type_key_values
    ADD CONSTRAINT fkhn6q00n3oexuto8sxnqrk3p8o FOREIGN KEY (vehicle_type_id) REFERENCES public.vehicle_type(id);


--
-- TOC entry 4488 (class 2606 OID 20129)
-- Name: deck_deck_spaces fkhrxsnjaujtgv4baqfxk1f2na4; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_deck_spaces
    ADD CONSTRAINT fkhrxsnjaujtgv4baqfxk1f2na4 FOREIGN KEY (deck_id) REFERENCES public.deck(id);


--
-- TOC entry 4492 (class 2606 OID 20144)
-- Name: deck_spot_rows fkiau8wt9j5j6rbo89wahel9n3a; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_spot_rows
    ADD CONSTRAINT fkiau8wt9j5j6rbo89wahel9n3a FOREIGN KEY (spot_rows_id) REFERENCES public.spot_row(id);


--
-- TOC entry 4500 (class 2606 OID 20194)
-- Name: passenger_entrance_key_values fkj3qodcskd7kui0jlvvpf3he3e; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance_key_values
    ADD CONSTRAINT fkj3qodcskd7kui0jlvvpf3he3e FOREIGN KEY (passenger_entrance_id) REFERENCES public.passenger_entrance(id);


--
-- TOC entry 4514 (class 2606 OID 20259)
-- Name: train_key_values fkj63od32lnocjjjsqimmchgkoe; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train_key_values
    ADD CONSTRAINT fkj63od32lnocjjjsqimmchgkoe FOREIGN KEY (train_id) REFERENCES public.train(id);


--
-- TOC entry 4516 (class 2606 OID 20274)
-- Name: vehicle fkjtchj1qk6y3jdm3ycsbaoci6q; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT fkjtchj1qk6y3jdm3ycsbaoci6q FOREIGN KEY (vehicle_model_id) REFERENCES public.vehicle_model(id);


--
-- TOC entry 4485 (class 2606 OID 20114)
-- Name: accessibility_assessment_limitations fkkghye5kl3gcgb4446yva0hqib; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.accessibility_assessment_limitations
    ADD CONSTRAINT fkkghye5kl3gcgb4446yva0hqib FOREIGN KEY (accessibility_assessment_id) REFERENCES public.accessibility_assessment(id);


--
-- TOC entry 4521 (class 2606 OID 20294)
-- Name: vehicle_equipment_profile_key_values fkks051nd8kaiyi14q0ylvwjg7g; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_key_values
    ADD CONSTRAINT fkks051nd8kaiyi14q0ylvwjg7g FOREIGN KEY (vehicle_equipment_profile_id) REFERENCES public.vehicle_equipment_profile(id);


--
-- TOC entry 4525 (class 2606 OID 20314)
-- Name: vehicle_model_key_values fklg5pnvnqumxx6aa0h6etvyswb; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_model_key_values
    ADD CONSTRAINT fklg5pnvnqumxx6aa0h6etvyswb FOREIGN KEY (vehicle_model_id) REFERENCES public.vehicle_model(id);


--
-- TOC entry 4510 (class 2606 OID 20244)
-- Name: passenger_spot_key_values fkli3dtmouxlvsui6gosuq80ug3; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot_key_values
    ADD CONSTRAINT fkli3dtmouxlvsui6gosuq80ug3 FOREIGN KEY (passenger_spot_id) REFERENCES public.passenger_spot(id);


--
-- TOC entry 4498 (class 2606 OID 20179)
-- Name: passenger_capacity_key_values fkmw9t1nykwe3744yb8jm0jlk18; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_capacity_key_values
    ADD CONSTRAINT fkmw9t1nykwe3744yb8jm0jlk18 FOREIGN KEY (passenger_capacity_id) REFERENCES public.passenger_capacity(id);


--
-- TOC entry 4515 (class 2606 OID 20264)
-- Name: value_items fknuulrwd9o0m7ocvcntkig5csj; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.value_items
    ADD CONSTRAINT fknuulrwd9o0m7ocvcntkig5csj FOREIGN KEY (value_id) REFERENCES public.value(id);


--
-- TOC entry 4504 (class 2606 OID 20209)
-- Name: passenger_space_deck_entrances fko4fvtqi0mmjt6kqda1boo8ygj; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_deck_entrances
    ADD CONSTRAINT fko4fvtqi0mmjt6kqda1boo8ygj FOREIGN KEY (passenger_space_id) REFERENCES public.passenger_space(id);


--
-- TOC entry 4517 (class 2606 OID 20269)
-- Name: vehicle fkoddtmb1vbr9vwsu281lma71j; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT fkoddtmb1vbr9vwsu281lma71j FOREIGN KEY (transport_type_id) REFERENCES public.vehicle_type(id);


--
-- TOC entry 4494 (class 2606 OID 20154)
-- Name: deck_plan_decks fkoypbfb6g2bqy9x40tc8sufp0p; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_decks
    ADD CONSTRAINT fkoypbfb6g2bqy9x40tc8sufp0p FOREIGN KEY (decks_id) REFERENCES public.deck(id);


--
-- TOC entry 4496 (class 2606 OID 20164)
-- Name: deck_plan_key_values fkp2w8p403qkxvcja1ctebht92v; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_plan_key_values
    ADD CONSTRAINT fkp2w8p403qkxvcja1ctebht92v FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4519 (class 2606 OID 20284)
-- Name: vehicle_key_values fkpcikdho7j7r5lqxobt4s9uy64; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_key_values
    ADD CONSTRAINT fkpcikdho7j7r5lqxobt4s9uy64 FOREIGN KEY (vehicle_id) REFERENCES public.vehicle(id);


--
-- TOC entry 4506 (class 2606 OID 20219)
-- Name: passenger_space_key_values fkpwfueeq07663oayvwjto7xcg3; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_key_values
    ADD CONSTRAINT fkpwfueeq07663oayvwjto7xcg3 FOREIGN KEY (passenger_space_id) REFERENCES public.passenger_space(id);


--
-- TOC entry 4490 (class 2606 OID 20139)
-- Name: deck_key_values fkri4wr7dt56uvbtuah590w1qnr; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.deck_key_values
    ADD CONSTRAINT fkri4wr7dt56uvbtuah590w1qnr FOREIGN KEY (deck_id) REFERENCES public.deck(id);


--
-- TOC entry 4501 (class 2606 OID 20189)
-- Name: passenger_entrance_key_values fkrj560iwjbe4s0r5pgja1xpc92; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_entrance_key_values
    ADD CONSTRAINT fkrj560iwjbe4s0r5pgja1xpc92 FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4511 (class 2606 OID 20239)
-- Name: passenger_spot_key_values fksb8dp7sv2tk61e5y8jsn65nd7; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_spot_key_values
    ADD CONSTRAINT fksb8dp7sv2tk61e5y8jsn65nd7 FOREIGN KEY (key_values_id) REFERENCES public.value(id);


--
-- TOC entry 4508 (class 2606 OID 20229)
-- Name: passenger_space_passenger_spots fksc7nrgo767gtltsu2b3t0igvp; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.passenger_space_passenger_spots
    ADD CONSTRAINT fksc7nrgo767gtltsu2b3t0igvp FOREIGN KEY (passenger_space_id) REFERENCES public.passenger_space(id);


--
-- TOC entry 4527 (class 2606 OID 20319)
-- Name: vehicle_type fkshuxi09nma1f9yrqn2smvp8ev; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_type
    ADD CONSTRAINT fkshuxi09nma1f9yrqn2smvp8ev FOREIGN KEY (deck_plan_id) REFERENCES public.deck_plan(id);


--
-- TOC entry 4523 (class 2606 OID 20299)
-- Name: vehicle_equipment_profile_vehicle_equipment_profile_members fktiuwho3nqg8dr0cpt7cfwc4lv; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.vehicle_equipment_profile_vehicle_equipment_profile_members
    ADD CONSTRAINT fktiuwho3nqg8dr0cpt7cfwc4lv FOREIGN KEY (vehicle_equipment_profile_members_id) REFERENCES public.vehicle_equipment_profile_member(id);


--
-- TOC entry 4512 (class 2606 OID 20249)
-- Name: train fktmrr9becsook17lkhowtdfsmc; Type: FK CONSTRAINT; Schema: public; Owner: sobek
--

ALTER TABLE ONLY public.train
    ADD CONSTRAINT fktmrr9becsook17lkhowtdfsmc FOREIGN KEY (deck_plan_id) REFERENCES public.deck_plan(id);


--
-- TOC entry 4735 (class 0 OID 0)
-- Dependencies: 8
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: sobek
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2025-11-24 12:53:38

--
-- PostgreSQL database dump complete
--

