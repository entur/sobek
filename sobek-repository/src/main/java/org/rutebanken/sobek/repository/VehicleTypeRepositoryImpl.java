package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.utils.QueryHelper;
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

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VehicleTypeRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    /**
     * Find stop place's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return stop place's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("vehicle_type", key, values);
    }

    /** VehicleType-only validity — no Vehicle join needed. */
    private static final String VT_CURRENT_WHERE =
            "WHERE vt.validBetween.fromDate <= :now " +
            "AND (vt.validBetween.toDate IS NULL OR vt.validBetween.toDate >= :now)";

    /** Extra condition for filtering out expired vehicles (requires alias v). */
    private static final String VEHICLE_CURRENT_COND =
            " AND (v IS NULL OR (v.validBetween.fromDate <= :now " +
            "AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)))";

    @Override
    public Page<VehicleType> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name, Pageable pageable) {
        Instant now = Instant.now();

        // Build dynamic WHERE suffix for optional filters
        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND vt.netexId IN :netexIds");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND vt.transportMode IN :transportModes");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND vt.name is not null and lower(vt.name.value) LIKE :name");
        }
        filterSuffix.append(" AND vt.dataOwnerRef = :dataOwnerRef");

        String fetchJpql = "SELECT DISTINCT vt FROM VehicleType vt " +
                "LEFT JOIN FETCH vt.vehicles v " +
                "LEFT JOIN FETCH vt.passengerCapacity " +
                VT_CURRENT_WHERE + VEHICLE_CURRENT_COND + filterSuffix +
                " ORDER BY vt.id";

        if (pageable.isUnpaged()) {
            TypedQuery<VehicleType> fetchQuery = entityManager.createQuery(fetchJpql, VehicleType.class);
            setFilterParams(fetchQuery, dataOwnerRef, netexIds, transportModes, name, now);
            return new PageImpl<>(fetchQuery.getResultList());
        }

        // Count — no Vehicle JOIN, no DISTINCT needed
        String countJpql = "SELECT COUNT(vt) FROM VehicleType vt " +
                VT_CURRENT_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, name, now);
        long total = countQuery.getSingleResult();

        // Fetch — in-memory pagination (HHH90003004) is acceptable for this small dataset
        TypedQuery<VehicleType> fetchQuery = entityManager.createQuery(fetchJpql, VehicleType.class);
        setFilterParams(fetchQuery, dataOwnerRef, netexIds, transportModes, name, now);
        fetchQuery.setFirstResult((int) pageable.getOffset());
        fetchQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(fetchQuery.getResultList(), pageable, total);
    }

    private void setFilterParams(Query query, String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name, Instant now) {
        query.setParameter("now", now);
        if (netexIds != null && !netexIds.isEmpty()) {
            query.setParameter("netexIds", netexIds);
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            query.setParameter("transportModes", transportModes);
        }
        if(name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }
        query.setParameter("dataOwnerRef", dataOwnerRef);
    }

    @Override
    public boolean existsValidWithDeckPlan(String deckPlanNetexId, Long deckPlanVersion) {
        Query query = entityManager.createNativeQuery("SELECT EXISTS (SELECT 1 FROM vehicle_type vt join deck_plan dp on vt.deck_plan_id=dp.id WHERE dp.netex_id = :deckPlanNetexId AND dp.version = :deckPlanVersion AND vt.from_date <= now() AND (vt.to_date IS NULL OR vt.to_date >= now()))");
        query.setParameter("deckPlanNetexId", deckPlanNetexId);
        query.setParameter("deckPlanVersion", deckPlanVersion);
Object result = query.getSingleResult();
boolean exists = (result instanceof Boolean b) ? b : ((Number) result).intValue() == 1;
logger.debug("Valid VehicleType with deck plan {} exists: {}", deckPlanNetexId, exists);
return exists;
    }


    @Override
    public void moveToDeckPlan(Long fromDeckPlanId, Long toDeckPlanId) {
        Query query = entityManager.createNativeQuery("UPDATE vehicle_type SET deck_plan_id = :toDeckPlanId WHERE deck_plan_id = :fromDeckPlanId " +
                " and (from_date is null or from_date <= now()) and (to_date is null or to_date >= now())");

        query.setParameter("toDeckPlanId", toDeckPlanId);
        query.setParameter("fromDeckPlanId", fromDeckPlanId);

        query.executeUpdate();
    }

    @Override
    public List<String> findCurrentNeTExIds() {
        String fetchJpql = "SELECT DISTINCT vt.netexId FROM VehicleType vt WHERE " +
                QueryHelper.objectValidCondition("vt", "now");
        TypedQuery<String> fetchQuery = entityManager.createQuery(fetchJpql, String.class);
        fetchQuery.setParameter("now", Instant.now());
        return fetchQuery.getResultList();
    }

}
