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

    @Override
    public List<VehicleType> findAllCurrent() {
        Instant now = Instant.now();
        TypedQuery<VehicleType> query = entityManager.createQuery(
            "SELECT DISTINCT vt FROM VehicleType vt " +
            "LEFT JOIN FETCH vt.vehicles v " +
            "WHERE vt.validBetween.fromDate <= :now " +
            "AND (vt.validBetween.toDate IS NULL OR vt.validBetween.toDate >= :now) " +
            "AND (v IS NULL OR (v.validBetween.fromDate <= :now AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)))", 
            VehicleType.class);
        query.setHint("hibernate.query.passDistinctThrough", false);
        query.setParameter("now", now);
        return query.getResultList();
    }

    @Override
    public Page<VehicleType> findCurrentFiltered(List<String> ids, AllPublicTransportModesEnumeration transportMode, Pageable pageable) {
        Instant now = Instant.now();
        StringBuilder jpql = new StringBuilder(
            "SELECT DISTINCT vt FROM VehicleType vt " +
            "LEFT JOIN FETCH vt.vehicles v " +
            "WHERE vt.validBetween.fromDate <= :now " +
            "AND (vt.validBetween.toDate IS NULL OR vt.validBetween.toDate >= :now) " +
            "AND (v IS NULL OR (v.validBetween.fromDate <= :now " +
            "AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)))");

        StringBuilder countJpql = new StringBuilder(
            "SELECT COUNT(DISTINCT vt) FROM VehicleType vt " +
            "WHERE vt.validBetween.fromDate <= :now " +
            "AND (vt.validBetween.toDate IS NULL OR vt.validBetween.toDate >= :now)");

        if (ids != null && !ids.isEmpty()) {
            jpql.append(" AND vt.netexId IN :ids");
            countJpql.append(" AND vt.netexId IN :ids");
        }
        if (transportMode != null) {
            jpql.append(" AND vt.transportMode = :transportMode");
            countJpql.append(" AND vt.transportMode = :transportMode");
        }

        TypedQuery<VehicleType> query = entityManager.createQuery(jpql.toString(), VehicleType.class);
        query.setHint("hibernate.query.passDistinctThrough", false);
        query.setParameter("now", now);

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        countQuery.setParameter("now", now);

        if (ids != null && !ids.isEmpty()) {
            query.setParameter("ids", ids);
            countQuery.setParameter("ids", ids);
        }
        if (transportMode != null) {
            query.setParameter("transportMode", transportMode);
            countQuery.setParameter("transportMode", transportMode);
        }

        long total = countQuery.getSingleResult();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
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
