package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
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
public class VehicleTypeRepositoryImpl implements VehicleTypeRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTypeRepositoryCustom.class);

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
                "FROM vehicle_type vt " +
                "INNER JOIN vehicle_type_key_values vtkv " +
                "ON vtkv.vehicle_type_id = vt.id " +
                "INNER JOIN value_items v " +
                "ON vtkv.key_values_id = v.value_id " +
                "WHERE vtkv.key_values_key = :key " +
                "AND v.items IN ( :values ) " +
                "AND vt.version = (SELECT MAX(pv.version) FROM vehicle_type pv WHERE pv.netex_id = vt.netex_id)");

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
            "WHERE vt.validBetween.fromDate <= :now " +
            "AND (vt.validBetween.toDate IS NULL OR vt.validBetween.toDate >= :now) " +
            "AND (v IS NULL OR (v.validBetween.fromDate <= :now " +
            "AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)))";

    @Override
    public List<VehicleType> findAllCurrent() {
        return findCurrentFiltered(null, null, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<VehicleType> findCurrentFiltered(List<String> ids, AllPublicTransportModesEnumeration transportMode, Pageable pageable) {
        Instant now = Instant.now();

        // Build dynamic WHERE suffix for optional filters
        StringBuilder filterSuffix = new StringBuilder();
        if (ids != null && !ids.isEmpty()) {
            filterSuffix.append(" AND vt.netexId IN :ids");
        }
        if (transportMode != null) {
            filterSuffix.append(" AND vt.transportMode = :transportMode");
        }

        // Phase 1: count
        String countJpql = "SELECT COUNT(DISTINCT vt) FROM VehicleType vt " +
                "LEFT JOIN vt.vehicles v " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("now", now);
        setFilterParams(countQuery, ids, transportMode);
        long total = countQuery.getSingleResult();

        if (pageable.isUnpaged()) {
            // No pagination — fetch all with JOIN FETCH
            String fetchJpql = "SELECT DISTINCT vt FROM VehicleType vt " +
                    "LEFT JOIN FETCH vt.vehicles v " + CURRENT_BASE_WHERE + filterSuffix;
            TypedQuery<VehicleType> fetchQuery = entityManager.createQuery(fetchJpql, VehicleType.class);
            fetchQuery.setParameter("now", now);
            setFilterParams(fetchQuery, ids, transportMode);
            return new PageImpl<>(fetchQuery.getResultList());
        }

        // Phase 2: fetch paged IDs (no JOIN FETCH — safe for LIMIT/OFFSET)
        String idJpql = "SELECT DISTINCT vt.id FROM VehicleType vt " +
                "LEFT JOIN vt.vehicles v " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> idQuery = entityManager.createQuery(idJpql, Long.class);
        idQuery.setParameter("now", now);
        setFilterParams(idQuery, ids, transportMode);
        idQuery.setFirstResult((int) pageable.getOffset());
        idQuery.setMaxResults(pageable.getPageSize());
        List<Long> pagedIds = idQuery.getResultList();

        if (pagedIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        // Phase 3: batch-fetch full entities with vehicles for the paged IDs
        TypedQuery<VehicleType> fetchQuery = entityManager.createQuery(
                "SELECT DISTINCT vt FROM VehicleType vt " +
                "LEFT JOIN FETCH vt.vehicles v " +
                "WHERE vt.id IN :pagedIds " +
                "AND (v IS NULL OR (v.validBetween.fromDate <= :now " +
                "AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)))",
                VehicleType.class);
        fetchQuery.setParameter("pagedIds", pagedIds);
        fetchQuery.setParameter("now", now);

        return new PageImpl<>(fetchQuery.getResultList(), pageable, total);
    }

    private void setFilterParams(Query query, List<String> ids, AllPublicTransportModesEnumeration transportMode) {
        if (ids != null && !ids.isEmpty()) {
            query.setParameter("ids", ids);
        }
        if (transportMode != null) {
            query.setParameter("transportMode", transportMode);
        }
    }

    @Override
    public void moveToDeckPlan(Long fromDeckPlanId, Long toDeckPlanId) {
        Query query = entityManager.createNativeQuery("UPDATE vehicle_type SET deck_plan_id = :toDeckPlanId WHERE deck_plan_id = :fromDeckPlanId " +
                " and (from_date is null or from_date <= now()) and (to_date is null or to_date >= now())");

        query.setParameter("toDeckPlanId", toDeckPlanId);
        query.setParameter("fromDeckPlanId", fromDeckPlanId);

        query.executeUpdate();
    }

}
