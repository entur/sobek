package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class DeckPlanRepositoryImpl implements DeckPlanRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(DeckPlanRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find deck plan's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return deck plan's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {

        Query query = entityManager.createNativeQuery("SELECT o.netex_id " +
                "FROM deck_plan o " +
                "INNER JOIN deck_plan_key_values okv " +
                "ON okv.deck_plan_id = o.id " +
                "INNER JOIN value_items v " +
                "ON okv.key_values_id = v.value_id " +
                "WHERE okv.key_values_key = :key " +
                "AND v.items IN ( :values ) " +
                "AND o.version = (SELECT MAX(oc.version) FROM deck_plan oc WHERE oc.netex_id = o.netex_id)");

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

    private static final String CURRENT_BASE_WHERE =
            "WHERE dp.validBetween.fromDate <= :now " +
            "AND (dp.validBetween.toDate IS NULL OR dp.validBetween.toDate >= :now)";

    @Override
    public List<DeckPlan> findAllCurrent() {
        return findCurrentPaged(null, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<DeckPlan> findCurrentPaged(List<String> netexIds, Pageable pageable) {
        Instant now = Instant.now();

        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND dp.netexId IN :netexIds");
        }


        String baseJpql = "SELECT DISTINCT dp FROM DeckPlan dp " + CURRENT_BASE_WHERE + filterSuffix;

        if (pageable.isUnpaged()) {
            TypedQuery<DeckPlan> query = entityManager.createQuery(baseJpql, DeckPlan.class);
            query.setParameter("now", now);
            if (netexIds != null && !netexIds.isEmpty()) {
                query.setParameter("netexIds", netexIds);
            }
            List<DeckPlan> results = query.getResultList();
            return new PageImpl<>(results);
        }

        String countJpql = "SELECT COUNT(DISTINCT dp) FROM DeckPlan dp " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("now", now);
        if (netexIds != null && !netexIds.isEmpty()) {
            countQuery.setParameter("netexIds", netexIds);
        }

        long total = countQuery.getSingleResult();

        TypedQuery<DeckPlan> query = entityManager.createQuery(baseJpql + " ORDER BY dp.id", DeckPlan.class);
        query.setParameter("now", now);
        if (netexIds != null && !netexIds.isEmpty()) {
            query.setParameter("netexIds", netexIds);
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }
}
