package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class VehicleRepositoryImpl implements VehicleRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(VehicleRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find vehicle's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return vehicle's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {

        Query query = entityManager.createNativeQuery("SELECT o.netex_id " +
                "FROM vehicle o " +
                "INNER JOIN vehicle_key_values okv " +
                "ON okv.vehicle_id = o.id " +
                "INNER JOIN value_items v " +
                "ON okv.key_values_id = v.value_id " +
                "WHERE okv.key_values_key = :key " +
                "AND v.items IN ( :values ) " +
                "AND o.version = (SELECT MAX(oc.version) FROM vehicle oc WHERE oc.netex_id = o.netex_id)");

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
    public void moveToTransportType(Long fromTransportTypeId, Long toTransportTypeId) {
        Query query = entityManager.createNativeQuery("UPDATE vehicle SET transport_type_id = :toTransportTypeId WHERE transport_type_id = :fromTransportTypeId " +
                " and (from_date is null or from_date <= now()) and (to_date is null or to_date >= now())");

        query.setParameter("toTransportTypeId", toTransportTypeId);
        query.setParameter("fromTransportTypeId", fromTransportTypeId);

        query.executeUpdate();
    }

    @Override
    public void healthCheck() {
        logger.info("Health check for VehicleRepositoryImpl");
        Query query = entityManager.createNativeQuery("SELECT 1");
        query.getResultList();
    }


    @Override
    public PageImpl<Vehicle> findCurrentFiltered(List<String> ids, List<AllPublicTransportModesEnumeration> transportModes, Pageable pageable) {
        Instant now = Instant.now();

        // Build dynamic WHERE suffix for optional filters
        StringBuilder filterSuffix = new StringBuilder();
        if (ids != null && !ids.isEmpty()) {
            filterSuffix.append(" AND v.netexId IN :ids");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND vt.transportMode in :transportModes");
        }

        String fetchJpql = "SELECT DISTINCT v FROM Vehicle v " +
                "LEFT JOIN FETCH v.transportType vt WHERE " +
                QueryHelper.ObjectValid_Condition("v", "now") + " AND " + QueryHelper.ObjectValid_Condition("vt", "now") + filterSuffix +
                " ORDER BY v.id";

        if (pageable.isUnpaged()) {
            TypedQuery<Vehicle> fetchQuery = entityManager.createQuery(fetchJpql, Vehicle.class);
            fetchQuery.setParameter("now", now);
            setFilterParams(fetchQuery, ids, transportModes);
            return new PageImpl<>(fetchQuery.getResultList());
        }

        // Count — no Vehicle JOIN, no DISTINCT needed
        String countJpql = "SELECT COUNT(v) FROM Vehicle v " +
                "LEFT JOIN v.transportType vt " +
                "WHERE " + QueryHelper.ObjectValid_Condition("v", "now") +
                " AND " + QueryHelper.ObjectValid_Condition("vt", "now") + filterSuffix;
        if(transportModes != null && !transportModes.isEmpty()) {
            countJpql += " AND vt.transportMode in :transportModes";
        }

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("now", now);
        setFilterParams(countQuery, ids, transportModes);
        long total = countQuery.getSingleResult();

        // Fetch — in-memory pagination (HHH90003004) is acceptable for this small dataset
        TypedQuery<Vehicle> fetchQuery = entityManager.createQuery(fetchJpql, Vehicle.class);
        fetchQuery.setParameter("now", now);
        setFilterParams(fetchQuery, ids, transportModes);
        fetchQuery.setFirstResult((int) pageable.getOffset());
        fetchQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(fetchQuery.getResultList(), pageable, total);
    }

    private void setFilterParams(Query query, List<String> ids, List<AllPublicTransportModesEnumeration> transportModes) {
        if (ids != null && !ids.isEmpty()) {
            query.setParameter("ids", ids);
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            query.setParameter("transportModes", transportModes);
        }
    }


}
