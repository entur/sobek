package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class VehicleModelRepositoryImpl implements VehicleModelRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;
    public VehicleModelRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }
    /**
     * Find vehicle model's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return vehicle model's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("vehicle_model", key, values);
    }

    private static final String CURRENT_BASE_WHERE =
        "WHERE vm.validBetween.fromDate <= :now " +
            "AND (vm.validBetween.toDate IS NULL OR vm.validBetween.toDate >= :now)";

    @Override
    public Page<VehicleModel> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name, Pageable pageable) {
        Instant now = Instant.now();

        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND vm.netexId IN :netexIds");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND vm.transportType.transportMode IN :transportModes");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND (vm.name is not null and lower(vm.name.value) LIKE :name ESCAPE '\\' or vm.transportType.name is not null and lower(vm.transportType.value) LIKE :name ESCAPE '\\'))");
        }
        if(dataOwnerRef != null) {
            filterSuffix.append(" AND vm.transportType.dataOwnerRef = :dataOwnerRef");
        }


        String baseJpql = "SELECT DISTINCT vm FROM VehicleModel vm " + CURRENT_BASE_WHERE + filterSuffix;

        if (pageable.isUnpaged()) {
            TypedQuery<VehicleModel> query = entityManager.createQuery(baseJpql, VehicleModel.class);
            setFilterParams(query, dataOwnerRef, netexIds, transportModes, name, now);
            List<VehicleModel> results = query.getResultList();
            return new PageImpl<>(results);
        }

        String countJpql = "SELECT COUNT(DISTINCT vm) FROM VehicleModel vm " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, name, now);

        long total = countQuery.getSingleResult();

        TypedQuery<VehicleModel> query = entityManager.createQuery(baseJpql + " ORDER BY vm.id", VehicleModel.class);
        setFilterParams(query, dataOwnerRef, netexIds, transportModes, name, now);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
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
        if(dataOwnerRef != null) {
            query.setParameter("dataOwnerRef", dataOwnerRef);
        }
    }

}
