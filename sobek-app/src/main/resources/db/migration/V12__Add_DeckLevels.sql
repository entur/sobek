CREATE TABLE deck_level
(
    id                BIGINT NOT NULL,
    version_comment   VARCHAR(255),
    changed_by        VARCHAR(255),
    created           TIMESTAMP WITHOUT TIME ZONE,
    changed           TIMESTAMP WITHOUT TIME ZONE,
    version           BIGINT NOT NULL,
    netex_id          VARCHAR(255),
    public_use        BOOLEAN,
    from_date         TIMESTAMP WITHOUT TIME ZONE,
    to_date           TIMESTAMP WITHOUT TIME ZONE,
    label_value       VARCHAR(255),
    label_lang        VARCHAR(5),
    name_value        VARCHAR(255),
    name_lang         VARCHAR(5),
    description_value VARCHAR(255),
    description_lang  VARCHAR(5),
    CONSTRAINT pk_decklevel PRIMARY KEY (id)
);

CREATE TABLE deck_level_key_values
(
    deck_level_id BIGINT NOT NULL,
    key_values_id BIGINT NOT NULL
);

CREATE TABLE deck_plan_deck_levels
(
    deck_plan_id   BIGINT NOT NULL,
    deck_levels_id BIGINT NOT NULL
);

ALTER TABLE deck
    ADD deck_level_id BIGINT;

ALTER TABLE deck_level_key_values
    ADD CONSTRAINT uc_deck_level_key_values_keyvalues UNIQUE (key_values_id);

ALTER TABLE deck_level_key_values
    ADD CONSTRAINT fk_declevkeyval_on_deck_level FOREIGN KEY (deck_level_id) REFERENCES deck_level (id);

ALTER TABLE deck_level_key_values
    ADD CONSTRAINT fk_declevkeyval_on_key_value FOREIGN KEY (key_values_id) REFERENCES key_value (id);

CREATE SEQUENCE public.deck_level_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS netex_deck_level_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE netex_deck_level_seq OWNER TO sobek;
