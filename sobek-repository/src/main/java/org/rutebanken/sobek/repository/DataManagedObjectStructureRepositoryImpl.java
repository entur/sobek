package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class DataManagedObjectStructureRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public String findFirstByKeyValues(String tableName, String key, Set<String> values) {
        Query query = entityManager.createNativeQuery("SELECT o.netex_id " +
                "FROM " + tableName + " o " +
                "INNER JOIN " + tableName + "_key_values okv " +
                "ON okv." + tableName + "_id = o.id " +
                "INNER JOIN key_value kv " +
                "ON okv.key_values_id = kv.id " +
                "WHERE kv.key = :key " +
                "AND kv.value IN ( :values ) " +
                "AND o.version = (SELECT MAX(oc.version) FROM " + tableName + " oc WHERE oc.netex_id = o.netex_id)");

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
