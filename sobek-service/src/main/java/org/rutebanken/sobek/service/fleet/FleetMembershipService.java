package org.rutebanken.sobek.service.fleet;

import jakarta.validation.ValidationException;
import org.rutebanken.sobek.graphql.converter.FleetNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleTypeNeTExIdConverter;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adds vehicles and vehicle types (transport types) to a fleet and removes them from it, leaving the fleet otherwise
 * unchanged. Each call makes one new version of the fleet, whatever it changes, and also when nothing in it changes.
 * <p>
 * Both are about the end result: what is added is in the fleet afterwards whether it was before, and what is removed is
 * not in the fleet afterwards whether it was before. A vehicle or vehicle type that does not exist at all is an error.
 * The rules of a fleet (same data owner, physical vehicle types) are those of the fleet saver, and apply to the whole call:
 * if one thing is refused, nothing is changed.
 */
@Service
@Transactional
public class FleetMembershipService {

    private final FleetRepository fleetRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final FleetNeTExIdConverter fleetIdConverter;
    private final VehicleNeTExIdConverter vehicleIdConverter;
    private final VehicleTypeNeTExIdConverter vehicleTypeIdConverter;
    private final VersionCreator versionCreator;
    private final FleetVersionedSaverService fleetVersionedSaverService;

    public FleetMembershipService(FleetRepository fleetRepository, VehicleRepository vehicleRepository,
                                  VehicleTypeRepository vehicleTypeRepository, FleetNeTExIdConverter fleetIdConverter,
                                  VehicleNeTExIdConverter vehicleIdConverter, VehicleTypeNeTExIdConverter vehicleTypeIdConverter,
                                  VersionCreator versionCreator, FleetVersionedSaverService fleetVersionedSaverService) {
        this.fleetRepository = fleetRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.fleetIdConverter = fleetIdConverter;
        this.vehicleIdConverter = vehicleIdConverter;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.versionCreator = versionCreator;
        this.fleetVersionedSaverService = fleetVersionedSaverService;
    }

    public Fleet add(String fleetNetexId, Collection<String> vehicleNetexIds, Collection<String> transportTypeNetexIds) {
        requireSomethingToChange(vehicleNetexIds, transportTypeNetexIds);
        Fleet existing = findFleet(fleetNetexId);
        Collection<Vehicle> vehicles = findVehicles(vehicleNetexIds);
        Collection<VehicleType> transportTypes = findTransportTypes(transportTypeNetexIds);

        Fleet newVersion = newVersionOf(existing);
        Set<String> memberIds = new HashSet<>();
        newVersion.getMembers().forEach(member -> memberIds.add(member.getNetexId()));
        for (Vehicle vehicle : vehicles) {
            if (memberIds.add(vehicle.getNetexId())) {
                newVersion.getMembers().add(vehicle);
            }
        }
        Set<String> transportTypeIds = new HashSet<>();
        newVersion.getTransportTypes().forEach(transportType -> transportTypeIds.add(transportType.getNetexId()));
        for (VehicleType transportType : transportTypes) {
            if (transportTypeIds.add(transportType.getNetexId())) {
                newVersion.getTransportTypes().add(transportType);
            }
        }
        return fleetVersionedSaverService.saveNewVersion(existing, newVersion, Instant.now());
    }

    public Fleet remove(String fleetNetexId, Collection<String> vehicleNetexIds, Collection<String> transportTypeNetexIds) {
        requireSomethingToChange(vehicleNetexIds, transportTypeNetexIds);
        Fleet existing = findFleet(fleetNetexId);
        Set<String> vehiclesToRemove = new HashSet<>();
        findVehicles(vehicleNetexIds).forEach(vehicle -> vehiclesToRemove.add(vehicle.getNetexId()));
        Set<String> transportTypesToRemove = new HashSet<>();
        findTransportTypes(transportTypeNetexIds).forEach(transportType -> transportTypesToRemove.add(transportType.getNetexId()));

        Fleet newVersion = newVersionOf(existing);
        newVersion.getMembers().removeIf(member -> vehiclesToRemove.contains(member.getNetexId()));
        newVersion.getTransportTypes().removeIf(transportType -> transportTypesToRemove.contains(transportType.getNetexId()));
        return fleetVersionedSaverService.saveNewVersion(existing, newVersion, Instant.now());
    }

    private static void requireSomethingToChange(Collection<String> vehicleNetexIds, Collection<String> transportTypeNetexIds) {
        if ((vehicleNetexIds == null || vehicleNetexIds.isEmpty()) && (transportTypeNetexIds == null || transportTypeNetexIds.isEmpty())) {
            throw new ValidationException("At least one vehicle or vehicle type must be given.");
        }
    }

    /**
     * A copy of the latest version that is valid from now on, as any new version is. The end of the validity is kept, so
     * that a deactivation that is planned still is. A fleet that has not started yet keeps its start.
     */
    private Fleet newVersionOf(Fleet existing) {
        Fleet newVersion = versionCreator.createCopy(existing, Fleet.class);
        Instant from = existing.getValidBetween() == null ? null : existing.getValidBetween().getFromDate();
        Instant to = existing.getValidBetween() == null ? null : existing.getValidBetween().getToDate();
        newVersion.setValidBetween(new ValidBetween(from != null && from.isAfter(Instant.now()) ? from : null, to));
        return newVersion;
    }

    private Fleet findFleet(String fleetNetexId) {
        Fleet asGiven = new Fleet();
        asGiven.setNetexId(fleetNetexId);
        Fleet fleet = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleetIdConverter.convertIncomingId(asGiven).getNetexId());
        if (fleet == null) {
            throw new ValidationException("Cannot find fleet " + fleetNetexId + ". No changes executed.");
        }
        if (fleet.getValidBetween() != null && fleet.getValidBetween().getToDate() != null
                && !fleet.getValidBetween().getToDate().isAfter(Instant.now())) {
            throw new ValidationException("The fleet " + fleetNetexId + " is deactivated, and cannot be changed.");
        }
        return fleet;
    }

    /** The vehicles in their latest versions. All of them must exist. */
    private Collection<Vehicle> findVehicles(Collection<String> vehicleNetexIds) {
        Set<Vehicle> vehicles = new LinkedHashSet<>();
        if (vehicleNetexIds == null) {
            return vehicles;
        }
        for (String netexId : vehicleNetexIds) {
            Vehicle asGiven = new Vehicle();
            asGiven.setNetexId(netexId);
            Vehicle vehicle = vehicleRepository.findFirstByNetexIdOrderByVersionDesc(vehicleIdConverter.convertIncomingId(asGiven).getNetexId());
            if (vehicle == null) {
                throw new ValidationException("Cannot find vehicle " + netexId + ". No changes executed.");
            }
            vehicles.add(vehicle);
        }
        return vehicles;
    }

    /** The vehicle types in their latest versions. All of them must exist. */
    private Collection<VehicleType> findTransportTypes(Collection<String> vehicleTypeNetexIds) {
        Set<VehicleType> vehicleTypes = new LinkedHashSet<>();
        if (vehicleTypeNetexIds == null) {
            return vehicleTypes;
        }
        for (String netexId : vehicleTypeNetexIds) {
            VehicleType asGiven = new VehicleType();
            asGiven.setNetexId(netexId);
            VehicleType vehicleType = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(vehicleTypeIdConverter.convertIncomingId(asGiven).getNetexId());
            if (vehicleType == null) {
                throw new ValidationException("Cannot find vehicle type " + netexId + ". No changes executed.");
            }
            vehicleTypes.add(vehicleType);
        }
        return vehicleTypes;
    }
}
