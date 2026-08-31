package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
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
public class DeckPlanRepositoryImpl implements DeckPlanRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(DeckPlanRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    public DeckPlanRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    /**
     * Find deck plan's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return deck plan's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("deck_plan", key, values);
    }

    private static final String CURRENT_BASE_WHERE =
            "WHERE dp.validBetween.fromDate <= :now " +
            "AND (dp.validBetween.toDate IS NULL OR dp.validBetween.toDate >= :now)";

    @Override
    public Page<DeckPlan> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, String name, Pageable pageable) {
        Instant now = Instant.now();

        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND dp.netexId IN :netexIds");
        }
        if (transportModes != null && !transportModes.isEmpty()) {
            filterSuffix.append(" AND exists(from VehicleType vt where vt.deckPlan=dp and vt.transportMode IN :transportModes)");
        }
        if(name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND (dp.name is not null and lower(dp.name.value) LIKE :name ESCAPE '\\' or exists(from VehicleType vt where vt.deckPlan = dp and vt.name is not null and lower(vt.name.value) LIKE :name ESCAPE '\\'))");
        }
        filterSuffix.append(" AND dp.dataOwnerRef = :dataOwnerRef");


        String baseJpql = "SELECT DISTINCT dp FROM DeckPlan dp " + CURRENT_BASE_WHERE + filterSuffix;

        if (pageable.isUnpaged()) {
            TypedQuery<DeckPlan> query = entityManager.createQuery(baseJpql, DeckPlan.class);
            setFilterParams(query, dataOwnerRef, netexIds, transportModes, name, now);
            List<DeckPlan> results = query.getResultList();
            return new PageImpl<>(results);
        }

        String countJpql = "SELECT COUNT(DISTINCT dp) FROM DeckPlan dp " + CURRENT_BASE_WHERE + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setFilterParams(countQuery, dataOwnerRef, netexIds, transportModes, name, now);

        long total = countQuery.getSingleResult();

        TypedQuery<DeckPlan> query = entityManager.createQuery(baseJpql + " ORDER BY dp.id", DeckPlan.class);
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
        query.setParameter("dataOwnerRef", dataOwnerRef);
    }

    @Override
    public List<String> findCurrentNeTExIds() {
        String fetchJpql = "SELECT DISTINCT d.netexId FROM DeckPlan d WHERE " +
                QueryHelper.objectValidCondition("d", "now");
        TypedQuery<String> fetchQuery = entityManager.createQuery(fetchJpql, String.class);
        fetchQuery.setParameter("now", Instant.now());
        return fetchQuery.getResultList();
    }

}
