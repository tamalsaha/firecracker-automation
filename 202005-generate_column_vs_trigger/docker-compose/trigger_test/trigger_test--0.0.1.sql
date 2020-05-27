CREATE OR REPLACE FUNCTION trigger_test() RETURNS trigger
     AS 'MODULE_PATHNAME','trigger_test'
LANGUAGE C STRICT;

CREATE TABLE square_trgc (i int, area float8 );

CREATE TRIGGER trg_areac
 BEFORE INSERT OR UPDATE ON square_trgc 
FOR EACH ROW EXECUTE PROCEDURE public.trigger_test();


CREATE TABLE square_trgplpgsql (i int, area float8 );

CREATE OR REPLACE FUNCTION trg_function_area ()
    RETURNS TRIGGER
    AS $$
BEGIN
    IF tg_op = 'INSERT' THEN
        NEW.area = NEW.i ^ 2;
    END IF;
    IF tg_op = 'UPDATE' THEN
        NEW.area = NEW.i ^ 2;
    END IF;
    RETURN new;
END;
$$
LANGUAGE plpgsql ;


CREATE TRIGGER trg_areaplpgsql
 BEFORE INSERT OR UPDATE ON square_trgplpgsql 
FOR EACH ROW EXECUTE PROCEDURE public.trg_function_area();


---immutable
CREATE OR REPLACE FUNCTION trigger_test_immutable() RETURNS trigger
     AS 'MODULE_PATHNAME','trigger_test'
LANGUAGE C STRICT IMMUTABLE;

CREATE TABLE square_trgc_immutable (i int, area float8 );

CREATE TRIGGER trg_areac_immutable
 BEFORE INSERT OR UPDATE ON square_trgc_immutable 
FOR EACH ROW EXECUTE PROCEDURE public.trigger_test_immutable();


CREATE TABLE square_trgplpgsql_immutable (i int, area float8 );

CREATE OR REPLACE FUNCTION trg_function_area_immutable ()
    RETURNS TRIGGER
    AS $$
BEGIN
    IF tg_op = 'INSERT' THEN
        NEW.area = NEW.i ^ 2;
    END IF;
    IF tg_op = 'UPDATE' THEN
        NEW.area = NEW.i ^ 2;
    END IF;
    RETURN new;
END;
$$
LANGUAGE plpgsql IMMUTABLE ;


CREATE TRIGGER trg_areaplpgsql_immutable
 BEFORE INSERT OR UPDATE ON square_trgplpgsql_immutable 
FOR EACH ROW EXECUTE PROCEDURE public.trg_function_area_immutable();




CREATE TABLE square_gc (i int, area float8 GENERATED ALWAYS AS (i^2 ) STORED );

ALTER TABLE square_trgc SET (autovacuum_enabled = false);
ALTER TABLE square_trgplpgsql SET (autovacuum_enabled = false);
ALTER TABLE square_trgc_immutable SET (autovacuum_enabled = false);
ALTER TABLE square_trgplpgsql_immutable SET (autovacuum_enabled = false);
ALTER TABLE square_gc SET (autovacuum_enabled = false);

select pg_stat_statements_reset();


