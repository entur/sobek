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
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 1', 'Euro1');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 2', 'Euro2');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 3', 'Euro3');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 4', 'Euro4');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 5', 'Euro5');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 6', 'Euro6');

insert into code_value (id, value_type, label, value)
values (nextval('code_value_seq'), 'EMISSION_STANDARD', 'Euro 7', 'Euro7');

