package org.rutebanken.sobek.repository;

import org.rutebanken.sobek.model.vehicle.Fleet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FleetRepositoryCustom extends DataManagedObjectStructureRepository<Fleet> {

    /**
     * Current fleets, optionally limited to a data owner, a list of NeTEx ids and a name (a part of the name, in any case).
     */
    Page<Fleet> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, String name, Pageable pageable);

    List<String> findCurrentNeTExIds();

    /**
     * The fleets in use now that have the vehicle (by NeTEx id, in any of its versions) as a member.
     */
    List<Fleet> findCurrentWithMember(String vehicleNetexId);

    /**
     * The fleets in use now that have the vehicle type (by NeTEx id, in any of its versions) as a transport type.
     */
    List<Fleet> findCurrentWithTransportType(String vehicleTypeNetexId);

    /**
     * Makes the fleets that have the vehicle row as a member (and have not expired) have the new row instead. Called
     * when a vehicle gets a new version, so that the members of a fleet stay the current versions of its vehicles.
     */
    void moveMember(Long fromVehicleId, Long toVehicleId);

    /**
     * Makes the fleets that have the vehicle type row as a transport type have the new row instead. Called when a
     * vehicle type gets a new version.
     */
    void moveTransportType(Long fromVehicleTypeId, Long toVehicleTypeId);

    /**
     * True if a fleet that has not expired (it is in use now, or starts later) has the vehicle (by NeTEx id) as a member.
     */
    boolean existsUnexpiredWithMember(String vehicleNetexId);

    /**
     * True if a fleet that has not expired has the vehicle type (by NeTEx id) as a transport type.
     */
    boolean existsUnexpiredWithTransportType(String vehicleTypeNetexId);
}
