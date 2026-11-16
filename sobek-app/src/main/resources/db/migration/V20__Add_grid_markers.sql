-- A grid marker puts a label on a cell of the grid of a deck (a row and a column of the deck), for a deck space. A deck
-- space can have several. They are carried in the Extensions of the deck space in NeTEx.
CREATE TABLE grid_marker
(
    id             BIGINT                      NOT NULL,
    netex_id       VARCHAR(255),
    changed        TIMESTAMP(6) WITH TIME ZONE,
    created        TIMESTAMP(6) WITH TIME ZONE,
    from_date      TIMESTAMP(6) WITH TIME ZONE,
    to_date        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT                      NOT NULL,
    label_lang     VARCHAR(5),
    label_value    VARCHAR(255),
    spot_row_id    BIGINT,
    spot_column_id BIGINT,
    CONSTRAINT pk_grid_marker PRIMARY KEY (id)
);

CREATE SEQUENCE grid_marker_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE grid_marker
    ADD CONSTRAINT fk_gridmarker_on_spot_row FOREIGN KEY (spot_row_id) REFERENCES spot_row (id);

ALTER TABLE grid_marker
    ADD CONSTRAINT fk_gridmarker_on_spot_column FOREIGN KEY (spot_column_id) REFERENCES spot_column (id);

CREATE TABLE deck_space_grid_markers
(
    deck_space_id   BIGINT NOT NULL,
    grid_markers_id BIGINT NOT NULL
);

ALTER TABLE deck_space_grid_markers
    ADD CONSTRAINT uc_deck_space_grid_markers_gridmarkers UNIQUE (grid_markers_id);

ALTER TABLE deck_space_grid_markers
    ADD CONSTRAINT fk_decspagrimar_on_deck_space FOREIGN KEY (deck_space_id) REFERENCES deck_space (id);

ALTER TABLE deck_space_grid_markers
    ADD CONSTRAINT fk_decspagrimar_on_grid_marker FOREIGN KEY (grid_markers_id) REFERENCES grid_marker (id);
