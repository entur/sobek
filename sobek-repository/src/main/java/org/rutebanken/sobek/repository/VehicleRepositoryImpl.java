package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
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
import java.util.Set;

@Repository
public class VehicleRepositoryImpl implements VehicleRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(VehicleRepositoryImpl.class);

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VehicleRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    /**
     * Find vehicle's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return vehicle's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("vehicle", key, values);
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
    public Page<Vehicle> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name, Pageable pageable) {
        Instant now = Instant.now();

        // Build dynamic WHERE suffix for optional filters
        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND v.netexId IN :netexIds");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND vt.transportMode in :transportModes");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND ((v.name is not null and lower(v.name.value) LIKE :name ESCAPE '\\') or (v.transportType is not null and v.transportType.name is not null and lower(v.transportType.name.value) LIKE :name ESCAPE '\\'))");
        }
        filterSuffix.append(" AND v.dataOwnerRef = :dataOwnerRef and (v.transportType is null or v.transportType.dataOwnerRef = :dataOwnerRef)");

        String fetchJpql = "SELECT DISTINCT v FROM Vehicle v " +
                "LEFT JOIN FETCH v.transportType vt WHERE " +
                QueryHelper.objectValidCondition("v", "now")
                + " AND (v.transportType is null or ("
                    + QueryHelper.objectValidCondition("vt", "now") + ")) "
                + filterSuffix +
                " ORDER BY v.id";

        if (pageable.isUnpaged()) {
            TypedQuery<Vehicle> fetchQuery = entityManager.createQuery(fetchJpql, Vehicle.class);
            fetchQuery.setParameter("now", now);
            setFilterParams(fetchQuery, dataOwnerRef, netexIds, transportModes, name);
            return new PageImpl<>(fetchQuery.getResultList());
        }

        // Count — no Vehicle JOIN, no DISTINCT needed
        String countJpql = "SELECT COUNT(v) FROM Vehicle v " +
                "LEFT JOIN v.transportType vt " +
                "WHERE " + QueryHelper.objectValidCondition("v", "now") +
                " AND (v.transportType is null or ("
                    + QueryHelper.objectValidCondition("vt", "now") + ")) "
                + filterSuffix;

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("now", now);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, name);
        long total = countQuery.getSingleResult();

        // Fetch — in-memory pagination (HHH90003004) is acceptable for this small dataset
        TypedQuery<Vehicle> fetchQuery = entityManager.createQuery(fetchJpql, Vehicle.class);
        fetchQuery.setParameter("now", now);
        setFilterParams(fetchQuery, dataOwnerRef, netexIds, transportModes, name);
        fetchQuery.setFirstResult((int) pageable.getOffset());
        fetchQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(fetchQuery.getResultList(), pageable, total);
    }

    private void setFilterParams(Query query, String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name) {
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
    public boolean existsValidWithVehicleType(String vehicleTypeNetexId, Long vehicleTypeVersion) {
        Query query = entityManager.createNativeQuery("SELECT EXISTS (SELECT 1 FROM vehicle v join vehicle_type vt on v.transport_type_id=vt.id WHERE vt.netex_id = :vehicleTypeNetexId AND vt.version = :vehicleTypeVersion AND v.from_date <= now() AND (v.to_date IS NULL OR v.to_date >= now()))");
        query.setParameter("vehicleTypeNetexId", vehicleTypeNetexId);
        query.setParameter("vehicleTypeVersion", vehicleTypeVersion);
        Object result = query.getSingleResult();

        boolean exists = (result instanceof Boolean b) ? b : ((Number) result).intValue() == 1;

        logger.debug("Valid Vehicle with vehicle type {} exists: {}", vehicleTypeNetexId, exists);

        return exists;
    }


}
