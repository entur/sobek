package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            filterSuffix.append(" AND vt.name is not null and lower(vt.name.value) LIKE :name ESCAPE '\\'");
        }
        filterSuffix.append(" AND vt.dataOwnerRef = :dataOwnerRef");

        if (pageable.isUnpaged()) {
            List<VehicleType> vehicleTypes = fetchVehicleTypesWithValidVehicles(dataOwnerRef, netexIds, transportModes, name, now, filterSuffix.toString(), null);
            return new PageImpl<>(vehicleTypes);
        }

        // Count — no Vehicle JOIN, no DISTINCT needed
        String countJpql = "SELECT COUNT(vt) FROM VehicleType vt " +
                VT_CURRENT_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, name, now);
        long total = countQuery.getSingleResult();

        // Fetch with pagination
        List<VehicleType> vehicleTypes = fetchVehicleTypesWithValidVehicles(dataOwnerRef, netexIds, transportModes, name, now, filterSuffix.toString(), pageable);

        return new PageImpl<>(vehicleTypes, pageable, total);
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

    /**
     * Fetches VehicleTypes with their currently valid Vehicles using a two-step approach:
     * 1. First query fetches the VehicleType IDs that meet the criteria
     * 2. Second query fetches the VehicleTypes with their valid Vehicles in separate batches
     */
    private List<VehicleType> fetchVehicleTypesWithValidVehicles(
        String dataOwnerRef,
        List<String> netexIds,
        List<AllPublicTransportModesEnumeration> transportModes,
        String name,
        Instant now,
        String filterSuffix,
        Pageable pageable) {

        // Step 1: Get IDs of VehicleTypes that meet criteria
        String idsJpql = "SELECT vt.id FROM VehicleType vt " +
            VT_CURRENT_WHERE + filterSuffix +
            " ORDER BY vt.id";

        TypedQuery<Long> idsQuery = entityManager.createQuery(idsJpql, Long.class);
        setFilterParams(idsQuery, dataOwnerRef, netexIds, transportModes, name, now);

        if (pageable != null) {
            idsQuery.setFirstResult((int) pageable.getOffset());
            idsQuery.setMaxResults(pageable.getPageSize());
        }

        List<Long> vehicleTypeIds = idsQuery.getResultList();

        if (vehicleTypeIds.isEmpty()) {
            return List.of();
        }

        // Step 2: Fetch VehicleTypes with PassengerCapacity (no vehicles yet)
        String fetchVtJpql = "SELECT DISTINCT vt FROM VehicleType vt " +
            "LEFT JOIN FETCH vt.passengerCapacity " +
            "WHERE vt.id IN :ids " +
            "ORDER BY vt.id";

        TypedQuery<VehicleType> fetchVtQuery = entityManager.createQuery(fetchVtJpql, VehicleType.class);
        fetchVtQuery.setParameter("ids", vehicleTypeIds);
        List<VehicleType> vehicleTypes = fetchVtQuery.getResultList();

        // Step 3: Fetch only the currently valid Vehicles for these VehicleTypes
        String fetchVehiclesJpql = "SELECT v FROM Vehicle v " +
            "WHERE v.transportType.id IN :ids " +
            "AND v.validBetween.fromDate <= :now " +
            "AND (v.validBetween.toDate IS NULL OR v.validBetween.toDate >= :now)";

        TypedQuery<Vehicle> fetchVehiclesQuery = entityManager.createQuery(fetchVehiclesJpql, Vehicle.class);
        fetchVehiclesQuery.setParameter("ids", vehicleTypeIds);
        fetchVehiclesQuery.setParameter("now", now);
        List<Vehicle> vehicles = fetchVehiclesQuery.getResultList();

        // Group vehicles by VehicleType ID
        Map<Long, List<Vehicle>> vehiclesByTypeId = vehicles.stream()
            .collect(Collectors.groupingBy(v -> v.getTransportType().getId()));

        // Set the valid vehicles collection for each VehicleType
        vehicleTypes.forEach(vt ->
            vt.setVehicles(vehiclesByTypeId.getOrDefault(vt.getId(), new java.util.ArrayList<>()))
        );

        return vehicleTypes;
    }
}
