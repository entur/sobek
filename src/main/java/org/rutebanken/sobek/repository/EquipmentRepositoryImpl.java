package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class EquipmentRepositoryImpl implements EquipmentRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentRepositoryCustom.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find stop place's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return stop place's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {

        Query query = entityManager.createNativeQuery("SELECT vt.netex_id " +
                "FROM equipment e " +
                "INNER JOIN equipment_key_values ekv " +
                "ON ekv.equipment_id = e.id " +
                "INNER JOIN value_items v " +
                "ON ekv.key_values_id = v.value_id " +
                "WHERE ekv.key_values_key = :key " +
                "AND v.items IN ( :values ) " +
                "AND e.version = (SELECT MAX(pv.version) FROM equipment pv WHERE pv.netex_id = e.netex_id)");

        query.setParameter("key", key);
        query.setParameter("values", values);

        try {
            @SuppressWarnings("unchecked")
            List<String> results = query.getResultList();
            if (results.isEmpty()) {
                return null;
            } else {
                return results.getFirst();
            }
        } catch (NoResultException noResultException) {
            return null;
        }
    }
}
