CREATE TABLE code_value
(
    id         BIGINT NOT NULL,
    value_type VARCHAR(255) NOT NULL,
    label      VARCHAR(255) NOT NULL,
    value      VARCHAR(255) NOT NULL,
    CONSTRAINT pk_codevalue PRIMARY KEY (id)
);

CREATE SEQUENCE public.code_value_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.code_value_seq OWNER TO sobek;

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO1', 'Euro 1');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO2', 'Euro 2');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO3', 'Euro 3');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO4', 'Euro 4');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO5', 'Euro 5');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO6', 'Euro 6');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'EURO7', 'Euro 7');

