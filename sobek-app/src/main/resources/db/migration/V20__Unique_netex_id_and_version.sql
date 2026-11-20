-- A NeTEx id and a version identify one row: version N of an object exists once.
-- The rule is added to every table that has both a netex_id and a version column.
--
-- Rows that already break the rule must be dealt with by hand, as it cannot be known which of two rows is the real one.
-- The check below stops the migration and lists them. To list them again:
--   SELECT netex_id, version, count(*), array_agg(id) FROM <table> GROUP BY netex_id, version HAVING count(*) > 1;

DO
$$
DECLARE
    versioned_table text;
    duplicates      text;
    report          text := '';
BEGIN
    FOR versioned_table IN
        SELECT c.table_name
        FROM information_schema.columns c
                 JOIN information_schema.tables t
                      ON t.table_schema = c.table_schema AND t.table_name = c.table_name AND t.table_type = 'BASE TABLE'
        WHERE c.table_schema = current_schema()
          AND c.column_name = 'netex_id'
          AND EXISTS (SELECT 1
                      FROM information_schema.columns v
                      WHERE v.table_schema = c.table_schema
                        AND v.table_name = c.table_name
                        AND v.column_name = 'version')
        ORDER BY c.table_name
        LOOP
            EXECUTE format(
                    'SELECT string_agg(netex_id || '' version '' || version || '' (rows '' || ids || '')'', ''; '') FROM ('
                        || 'SELECT netex_id, version, string_agg(id::text, '', '' ORDER BY id) AS ids FROM %I '
                        || 'WHERE netex_id IS NOT NULL GROUP BY netex_id, version HAVING count(*) > 1) d',
                    versioned_table) INTO duplicates;
            IF duplicates IS NOT NULL THEN
                report := report || E'\n' || versioned_table || ': ' || duplicates;
            END IF;
        END LOOP;

    IF report <> '' THEN
        RAISE EXCEPTION 'Cannot add unique (netex_id, version): these objects exist more than once. Fix or remove the extra rows, then start again.%', report;
    END IF;

    FOR versioned_table IN
        SELECT c.table_name
        FROM information_schema.columns c
                 JOIN information_schema.tables t
                      ON t.table_schema = c.table_schema AND t.table_name = c.table_name AND t.table_type = 'BASE TABLE'
        WHERE c.table_schema = current_schema()
          AND c.column_name = 'netex_id'
          AND EXISTS (SELECT 1
                      FROM information_schema.columns v
                      WHERE v.table_schema = c.table_schema
                        AND v.table_name = c.table_name
                        AND v.column_name = 'version')
        ORDER BY c.table_name
        LOOP
            EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I UNIQUE (netex_id, version)',
                           versioned_table, 'uc_' || versioned_table || '_netex_id_version');
        END LOOP;
END
$$;
