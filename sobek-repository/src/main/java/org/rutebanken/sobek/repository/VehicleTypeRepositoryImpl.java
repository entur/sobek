package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
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

    @Override
    public Page<VehicleType> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, VehicleTypeKindEnumeration vehicleTypeKind, Class<? extends VehicleType> subtype, String name, Pageable pageable) {
        Instant now = Instant.now();

        // Build dynamic WHERE suffix for optional filters
        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND vt.netexId IN :netexIds");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND vt.transportMode IN :transportModes");
        }
        if (vehicleTypeKind != null) {
            filterSuffix.append(" AND vt.vehicleTypeKind = :vehicleTypeKind");
        }
        if (subtype != null) {
            // TYPE() is the exact class, so VehicleType gives the plain ones and not the trains
            filterSuffix.append(" AND TYPE(vt) = :subtype");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND vt.name is not null and lower(vt.name.value) LIKE :name ESCAPE '\\'");
        }
        if(dataOwnerRef != null) {
            // The vehicle types of the organisation, and the ones that are transport types of a fleet that another organisation shares with it
            filterSuffix.append(" AND (vt.dataOwnerRef = :dataOwnerRef OR " + QueryHelper.VEHICLE_TYPE_IN_SHARED_FLEET + ")");
        }

        if (pageable.isUnpaged()) {
            List<VehicleType> vehicleTypes = fetchVehicleTypes(dataOwnerRef, netexIds, transportModes, vehicleTypeKind, subtype, name, now, filterSuffix.toString(), null);
            return new PageImpl<>(vehicleTypes);
        }

        // Count — no Vehicle JOIN, no DISTINCT needed
        String countJpql = "SELECT COUNT(vt) FROM VehicleType vt " +
                VT_CURRENT_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, vehicleTypeKind, subtype, name, now);
        long total = countQuery.getSingleResult();

        // Fetch with pagination
        List<VehicleType> vehicleTypes = fetchVehicleTypes(dataOwnerRef, netexIds, transportModes, vehicleTypeKind, subtype, name, now, filterSuffix.toString(), pageable);

        return new PageImpl<>(vehicleTypes, pageable, total);
    }

    private void setFilterParams(Query query, String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, VehicleTypeKindEnumeration vehicleTypeKind, Class<? extends VehicleType> subtype, String name, Instant now) {
        query.setParameter("now", now);
        if (netexIds != null && !netexIds.isEmpty()) {
            query.setParameter("netexIds", netexIds);
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            query.setParameter("transportModes", transportModes);
        }
        if (vehicleTypeKind != null) {
            query.setParameter("vehicleTypeKind", vehicleTypeKind);
        }
        if (subtype != null) {
            query.setParameter("subtype", subtype);
        }
        if(name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }
        if(dataOwnerRef != null) {
            query.setParameter("dataOwnerRef", dataOwnerRef);
        }
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
    public boolean existsValidCompoundTrainWithTrain(String trainNetexId, Long trainVersion) {
        Query query = entityManager.createNativeQuery("SELECT EXISTS (SELECT 1 FROM train_in_compound_train tic " +
                "JOIN vehicle_type train ON tic.train_id = train.id " +
                "JOIN vehicle_type compound ON tic.owner_compound_train_id = compound.id " +
                "WHERE train.netex_id = :trainNetexId AND train.version = :trainVersion " +
                "AND (compound.from_date IS NULL OR compound.from_date <= :now) AND (compound.to_date IS NULL OR compound.to_date >= :now))");
        query.setParameter("now", Instant.now());
        query.setParameter("trainNetexId", trainNetexId);
        query.setParameter("trainVersion", trainVersion);
        Object result = query.getSingleResult();
        return (result instanceof Boolean b) ? b : ((Number) result).intValue() == 1;
    }

    @Override
    public boolean existsValidTrainWithTrainElement(String trainElementNetexId, Long trainElementVersion) {
        Query query = entityManager.createNativeQuery("SELECT EXISTS (SELECT 1 FROM train_component tc " +
                "JOIN vehicle_type element ON tc.train_element_id = element.id " +
                "JOIN vehicle_type train ON tc.owner_train_id = train.id " +
                "WHERE element.netex_id = :elementNetexId AND element.version = :elementVersion " +
                "AND (train.from_date IS NULL OR train.from_date <= :now) AND (train.to_date IS NULL OR train.to_date >= :now))");
        query.setParameter("now", Instant.now());
        query.setParameter("elementNetexId", trainElementNetexId);
        query.setParameter("elementVersion", trainElementVersion);
        Object result = query.getSingleResult();
        return (result instanceof Boolean b) ? b : ((Number) result).intValue() == 1;
    }

    @Override
    public void moveComponentReferences(Long fromVehicleTypeId, Long toVehicleTypeId) {
        // Only components of owners that are valid now are moved, so that the history keeps pointing at what was current then
        String currentOwner = " IN (SELECT id FROM vehicle_type WHERE (from_date is null or from_date <= :now) and (to_date is null or to_date >= :now))";
        // The ids are those of vehicle_type rows, so a row is either a train (held by compound trains) or a train element
        // (held by trains): only one of the two updates changes anything.
        Instant now = Instant.now();
        Query compoundTrains = entityManager.createNativeQuery(
                "UPDATE train_in_compound_train SET train_id = :to WHERE train_id = :from AND owner_compound_train_id" + currentOwner);
        compoundTrains.setParameter("to", toVehicleTypeId);
        compoundTrains.setParameter("from", fromVehicleTypeId);
        compoundTrains.setParameter("now", now);
        compoundTrains.executeUpdate();

        Query trains = entityManager.createNativeQuery(
                "UPDATE train_component SET train_element_id = :to WHERE train_element_id = :from AND owner_train_id" + currentOwner);
        trains.setParameter("to", toVehicleTypeId);
        trains.setParameter("from", fromVehicleTypeId);
        trains.setParameter("now", now);
        trains.executeUpdate();
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

    @Override
    public List<Vehicle> findCurrentVehiclesVisibleTo(Long vehicleTypeId, Collection<String> organisationRefs) {
        Instant now = Instant.now();
        List<Vehicle> vehicles = entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.transportType.id = :typeId AND " + QueryHelper.objectValidCondition("v", "now")
                                + " ORDER BY v.id", Vehicle.class)
                .setParameter("typeId", vehicleTypeId)
                .setParameter("now", now)
                .getResultList();
        return organisationRefs == null ? vehicles : onlyVisibleTo(organisationRefs, vehicles, now);
    }

    /**
     * The vehicles of the organisations, and the ones of other organisations that are in a fleet in use now that is shared
     * with them or with everyone. A vehicle type that is shared through a fleet does not show the owner's other vehicles.
     */
    private List<Vehicle> onlyVisibleTo(Collection<String> organisationRefs, List<Vehicle> vehicles, Instant now) {
        if (organisationRefs.isEmpty()) {
            return List.of();
        }
        List<Long> foreignIds = vehicles.stream()
                .filter(v -> !organisationRefs.contains(v.getDataOwnerRef()))
                .map(Vehicle::getId)
                .toList();
        if (foreignIds.isEmpty()) {
            return vehicles;
        }
        List<Long> sharedIds = entityManager.createQuery(
                        "SELECT DISTINCT m.id FROM Fleet f JOIN f.members m LEFT JOIN f.sharedWith s WHERE m.id IN :ids AND "
                                + QueryHelper.objectValidCondition("f", "now") + " AND (f.sharedWithAll = true OR s IN :organisationRefs)", Long.class)
                .setParameter("ids", foreignIds)
                .setParameter("now", now)
                .setParameter("organisationRefs", organisationRefs)
                .getResultList();
        return vehicles.stream()
                .filter(v -> organisationRefs.contains(v.getDataOwnerRef()) || sharedIds.contains(v.getId()))
                .toList();
    }

    /**
     * Fetches VehicleTypes using a two-step approach:
     * 1. First query fetches the VehicleType IDs that meet the criteria
     * 2. Second query fetches the VehicleTypes with their passenger capacity
     */
    private List<VehicleType> fetchVehicleTypes(
        String dataOwnerRef,
        List<String> netexIds,
        List<AllPublicTransportModesEnumeration> transportModes,
        VehicleTypeKindEnumeration vehicleTypeKind,
        Class<? extends VehicleType> subtype,
        String name,
        Instant now,
        String filterSuffix,
        Pageable pageable) {

        // Step 1: Get IDs of VehicleTypes that meet criteria
        String idsJpql = "SELECT vt.id FROM VehicleType vt " +
            VT_CURRENT_WHERE + filterSuffix +
            " ORDER BY vt.id";

        TypedQuery<Long> idsQuery = entityManager.createQuery(idsJpql, Long.class);
        setFilterParams(idsQuery, dataOwnerRef, netexIds, transportModes, vehicleTypeKind, subtype, name, now);

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

        // The vehicles are not set here: they are given by VehicleType.vehicles in GraphQL, which limits them to the ones that
        // the user may see (see findCurrentVehiclesVisibleTo), however the vehicle type was reached.
        return vehicleTypes;
    }
}
