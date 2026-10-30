package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class EquipmentRepositoryImpl implements EquipmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    private static final String CURRENT_BASE_WHERE =
        "WHERE eq.validBetween.fromDate <= :now " +
            "AND (eq.validBetween.toDate IS NULL OR eq.validBetween.toDate >= :now)";



    public EquipmentRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
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
        return dataManagedObjectStructureRepository.findFirstByKeyValues("equipment", key, values);
    }

    @Override
    public Page<Equipment> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, String name, Pageable pageable) {
        Instant now = Instant.now();

        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND eq.netexId IN :netexIds");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND (eq.name is not null and lower(eq.name.value) LIKE :name ESCAPE '\\'");
        }
        // TODO: Add data owner ref filter - see https://github.com/entur/sobek/issues/226
//        if(dataOwnerRef != null) {
//            filterSuffix.append(" AND eq.dataOwnerRef = :dataOwnerRef");
//        }


        String baseJpql = "SELECT DISTINCT eq FROM Equipment eq " + CURRENT_BASE_WHERE + filterSuffix;

        if (pageable.isUnpaged()) {
            TypedQuery<Equipment> query = entityManager.createQuery(baseJpql, Equipment.class);
            setFilterParams(query, dataOwnerRef, netexIds, name, now);
            List<Equipment> results = query.getResultList();
            return new PageImpl<>(results);
        }

        String countJpql = "SELECT COUNT(DISTINCT eq) FROM Equipment eq " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, name, now);

        long total = countQuery.getSingleResult();

        TypedQuery<Equipment> query = entityManager.createQuery(baseJpql + " ORDER BY eq.id", Equipment.class);
        setFilterParams(query, dataOwnerRef, netexIds, name, now);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    private void setFilterParams(Query query, String dataOwnerRef, List<String> netexIds, String name, Instant now) {
        query.setParameter("now", now);
        if (netexIds != null && !netexIds.isEmpty()) {
            query.setParameter("netexIds", netexIds);
        }
        if(name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }
//        if(dataOwnerRef != null) {
//            query.setParameter("dataOwnerRef", dataOwnerRef);
//        }
    }


}
