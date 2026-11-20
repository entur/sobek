package org.rutebanken.sobek.rest.netex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A NeTEx id and a version identify one row, in every table that has both columns.
 */
@SpringBootTest
class UniqueNetexIdAndVersionIT {

    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeTestRows() {
        jdbcTemplate.update("DELETE FROM deck_plan WHERE netex_id LIKE 'NMR:DeckPlan:unique-test-%'");
    }

    @Test
    void everyVersionedTable_hasAUniqueConstraintOnNetexIdAndVersion() {
        List<String> versionedTables = jdbcTemplate.queryForList(
                """
                SELECT table_name FROM information_schema.columns
                WHERE table_schema = current_schema() AND column_name = 'netex_id'
                  AND table_name IN (SELECT table_name FROM information_schema.columns
                                     WHERE table_schema = current_schema() AND column_name = 'version')
                  AND table_name IN (SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE')
                """,
                String.class);
        assertThat(versionedTables).contains("deck_plan", "vehicle_type", "deck", "spot_row");
        for (String table : versionedTables) {
            List<String> columns = jdbcTemplate.queryForList(
                    """
                    SELECT string_agg(a.attname, ',' ORDER BY a.attname)
                    FROM pg_index i
                    JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY (i.indkey)
                    WHERE i.indrelid = ?::regclass AND i.indisunique AND NOT i.indisprimary
                    GROUP BY i.indexrelid
                    """,
                    String.class,
                    table);
            assertThat(columns).as("unique indexes on %s", table).contains("netex_id,version");
        }
    }

    @Test
    void secondRowWithTheSameNetexIdAndVersion_isRejected() {
        String netexId = "NMR:DeckPlan:unique-test-" + System.nanoTime();
        insertDeckPlan(1_000_000_001L + System.nanoTime() % 1_000_000, netexId, 1);

        assertThatThrownBy(() -> insertDeckPlan(2_000_000_001L + System.nanoTime() % 1_000_000, netexId, 1))
                .isInstanceOf(DataIntegrityViolationException.class);

        // The next version of the same object is fine
        insertDeckPlan(3_000_000_001L + System.nanoTime() % 1_000_000, netexId, 2);
    }

    private void insertDeckPlan(long id, String netexId, long version) {
        jdbcTemplate.update("INSERT INTO deck_plan (id, netex_id, version) VALUES (?, ?, ?)", id, netexId, version);
    }
}
