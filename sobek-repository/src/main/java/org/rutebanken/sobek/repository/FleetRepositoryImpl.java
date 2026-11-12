package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.repository.utils.QueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public class FleetRepositoryImpl implements FleetRepositoryCustom {

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public FleetRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("fleet", key, values);
    }

    @Override
    public Page<Fleet> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, String name, Pageable pageable) {
        Instant now = Instant.now();

        StringBuilder filterSuffix = new StringBuilder();
        if (netexIds != null && !netexIds.isEmpty()) {
            filterSuffix.append(" AND f.netexId IN :netexIds");
        }
        if (name != null && !name.isEmpty()) {
            name = "%" + QueryHelper.escapeForLike(name.toLowerCase()) + "%";
            filterSuffix.append(" AND (f.name is not null and lower(f.name.value) LIKE :name ESCAPE '\\')");
        }
        if (dataOwnerRef != null) {
            filterSuffix.append(" AND f.dataOwnerRef = :dataOwnerRef");
        }

        String fetchJpql = "SELECT f FROM Fleet f WHERE " + QueryHelper.objectValidCondition("f", "now")
                + filterSuffix + " ORDER BY f.id";

        if (pageable.isUnpaged()) {
            TypedQuery<Fleet> fetchQuery = entityManager.createQuery(fetchJpql, Fleet.class);
            fetchQuery.setParameter("now", now);
            setFilterParams(fetchQuery, dataOwnerRef, netexIds, name);
            return new PageImpl<>(withKeyValues(fetchQuery.getResultList()));
        }

        String countJpql = "SELECT COUNT(f) FROM Fleet f WHERE " + QueryHelper.objectValidCondition("f", "now") + filterSuffix;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("now", now);
        setFilterParams(countQuery, dataOwnerRef, netexIds, name);
        long total = countQuery.getSingleResult();

        TypedQuery<Fleet> fetchQuery = entityManager.createQuery(fetchJpql, Fleet.class);
        fetchQuery.setParameter("now", now);
        setFilterParams(fetchQuery, dataOwnerRef, netexIds, name);
        fetchQuery.setFirstResult((int) pageable.getOffset());
        fetchQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(withKeyValues(fetchQuery.getResultList()), pageable, total);
    }

    /**
     * The key values are a lazy collection, and GraphQL reads them after the transaction has ended. They are loaded here
     * for all the fleets at once, as the members and transport types are (those are eager).
     */
    private List<Fleet> withKeyValues(List<Fleet> fleets) {
        if (!fleets.isEmpty()) {
            entityManager.createQuery("SELECT DISTINCT f FROM Fleet f LEFT JOIN FETCH f.keyValues WHERE f IN :fleets", Fleet.class)
                    .setParameter("fleets", fleets)
                    .getResultList();
        }
        return fleets;
    }

    private void setFilterParams(Query query, String dataOwnerRef, List<String> netexIds, String name) {
        if (netexIds != null && !netexIds.isEmpty()) {
            query.setParameter("netexIds", netexIds);
        }
        if (name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }
        if (dataOwnerRef != null) {
            query.setParameter("dataOwnerRef", dataOwnerRef);
        }
    }

    @Override
    public List<String> findCurrentNeTExIds() {
        String jpql = "SELECT DISTINCT f.netexId FROM Fleet f WHERE " + QueryHelper.objectValidCondition("f", "now");
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        query.setParameter("now", Instant.now());
        return query.getResultList();
    }

    @Override
    public List<Fleet> findCurrentWithMember(String vehicleNetexId) {
        return findCurrentWith("members", vehicleNetexId);
    }

    @Override
    public List<Fleet> findCurrentWithTransportType(String vehicleTypeNetexId) {
        return findCurrentWith("transportTypes", vehicleTypeNetexId);
    }

    private List<Fleet> findCurrentWith(String collection, String netexId) {
        TypedQuery<Fleet> query = entityManager.createQuery(
                "SELECT DISTINCT f FROM Fleet f JOIN f." + collection + " x WHERE x.netexId = :netexId AND "
                        + QueryHelper.objectValidCondition("f", "now") + " ORDER BY f.netexId", Fleet.class);
        query.setParameter("netexId", netexId);
        query.setParameter("now", Instant.now());
        return withKeyValues(query.getResultList());
    }

    @Override
    public void moveMember(Long fromVehicleId, Long toVehicleId) {
        moveReference("fleet_members", "members_id", fromVehicleId, toVehicleId);
    }

    @Override
    public void moveTransportType(Long fromVehicleTypeId, Long toVehicleTypeId) {
        moveReference("fleet_transport_types", "transport_types_id", fromVehicleTypeId, toVehicleTypeId);
    }

    /**
     * The fleets that have not expired follow to the new version: the ones in use now, and the ones that start later. A
     * fleet that has expired is history and keeps the version it had. The old row may be in a fleet that already has
     * the new row, so those are left as they are.
     */
    private void moveReference(String joinTable, String column, Long fromId, Long toId) {
        Query query = entityManager.createNativeQuery(
                "UPDATE " + joinTable + " jt SET " + column + " = :toId " +
                        "WHERE jt." + column + " = :fromId " +
                        "AND NOT EXISTS (SELECT 1 FROM " + joinTable + " other WHERE other.fleet_id = jt.fleet_id AND other." + column + " = :toId) " +
                        "AND EXISTS (SELECT 1 FROM fleet f WHERE f.id = jt.fleet_id " + NOT_EXPIRED + ")");
        query.setParameter("toId", toId);
        query.setParameter("fromId", fromId);
        query.executeUpdate();
    }

    @Override
    public boolean existsUnexpiredWithMember(String vehicleNetexId) {
        return exists("SELECT EXISTS (SELECT 1 FROM fleet f JOIN fleet_members fm ON fm.fleet_id = f.id " +
                "JOIN vehicle v ON v.id = fm.members_id WHERE v.netex_id = :netexId " + NOT_EXPIRED + ")", vehicleNetexId);
    }

    @Override
    public boolean existsUnexpiredWithTransportType(String vehicleTypeNetexId) {
        return exists("SELECT EXISTS (SELECT 1 FROM fleet f JOIN fleet_transport_types ft ON ft.fleet_id = f.id " +
                "JOIN vehicle_type vt ON vt.id = ft.transport_types_id WHERE vt.netex_id = :netexId " + NOT_EXPIRED + ")", vehicleTypeNetexId);
    }

    /** A fleet that is in use now or starts later: it has no end, or its end has not passed. */
    private static final String NOT_EXPIRED = "AND (f.to_date IS NULL OR f.to_date >= now())";

    private boolean exists(String sql, String netexId) {
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("netexId", netexId);
        Object result = query.getSingleResult();
        return (result instanceof Boolean b) ? b : ((Number) result).intValue() == 1;
    }
}
