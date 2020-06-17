CREATE OR REPLACE FUNCTION trigger_test() RETURNS trigger
     AS 'MODULE_PATHNAME','trigger_test'
LANGUAGE C STRICT;

CREATE TABLE tab_trg_c (i int , area float8, ts timestamp without time zone );

CREATE TRIGGER trg_areac
 BEFORE INSERT OR UPDATE ON tab_trg_c 
FOR EACH ROW EXECUTE PROCEDURE public.trigger_test();


CREATE TABLE tab_trg_plpgsql (i int , area float8, ts timestamp without time zone  );

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
 BEFORE INSERT OR UPDATE ON tab_trg_plpgsql 
FOR EACH ROW EXECUTE PROCEDURE public.trg_function_area();


---immutable
CREATE OR REPLACE FUNCTION trigger_test_immutable() RETURNS trigger
     AS 'MODULE_PATHNAME','trigger_test'
LANGUAGE C STRICT IMMUTABLE;

CREATE TABLE tab_trg_c_im (i int , area float8, ts timestamp without time zone  );

CREATE TRIGGER trg_areac_immutable
 BEFORE INSERT OR UPDATE ON tab_trg_c_im 
FOR EACH ROW EXECUTE PROCEDURE public.trigger_test_immutable();

---immutable
CREATE TABLE tab_trg_plpgsql_im (i int , area float8, ts timestamp without time zone  );

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
 BEFORE INSERT OR UPDATE ON tab_trg_plpgsql_im 
FOR EACH ROW EXECUTE PROCEDURE public.trg_function_area_immutable();




CREATE TABLE tab_gc (i int , area float8 GENERATED ALWAYS AS (i^2 ) STORED, ts timestamp without time zone  );

ALTER TABLE tab_trg_c SET (autovacuum_enabled = false);
ALTER TABLE tab_trg_plpgsql SET (autovacuum_enabled = false);
ALTER TABLE tab_trg_c_im SET (autovacuum_enabled = false);
ALTER TABLE tab_trg_plpgsql_im SET (autovacuum_enabled = false);
ALTER TABLE tab_gc SET (autovacuum_enabled = false);



select pg_stat_statements_reset();


