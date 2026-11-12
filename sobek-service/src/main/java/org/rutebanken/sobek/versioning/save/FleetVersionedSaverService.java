package org.rutebanken.sobek.versioning.save;

import jakarta.validation.ValidationException;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.repository.FleetRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class FleetVersionedSaverService {

    private final FleetRepository fleetRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;
    private final OrganisationRegistry organisationRegistry;

    public FleetVersionedSaverService(FleetRepository fleetRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService,
                                      OrganisationRegistry organisationRegistry) {
        this.fleetRepository = fleetRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
        this.organisationRegistry = organisationRegistry;
    }

    public Fleet saveNewVersion(Fleet newVersion) {
        verifyMembersAndTransportTypes(newVersion);
        return defaultVersionedSaverService.saveNewVersion(newVersion, fleetRepository);
    }

    public Fleet saveNewVersion(Fleet existingVersion, Fleet newVersion, Instant defaultValidFrom) {
        verifyMembersAndTransportTypes(newVersion);
        return defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, defaultValidFrom, fleetRepository);
    }

    /**
     * A fleet is made of its owner's vehicles and vehicle types: they must exist, belong to the same data owner as the
     * fleet, and be physical. A Vehicle is a real-world object, so a fleet of vehicles cannot refer to a contractual
     * vehicle type, and the export of the physical kind would have a reference to something that is not in it.
     */
    private void verifyMembersAndTransportTypes(Fleet fleet) {
        String fleetId = fleet.getNetexId() == null ? "(new)" : fleet.getNetexId();

        // The organisation of the fleet can be of any type, but it must be in the organisation registry. If it is not,
        // the registry says so with a ValidationException
        if (fleet.getTransportOrganisationRef() != null) {
            organisationRegistry.validateOrganisationRef(fleet.getTransportOrganisationRef());
        }

        for (Vehicle member : fleet.getMembers()) {
            if (member == null || member.getId() == null) {
                throw new ValidationException("Fleet " + fleetId + " has a member that is not an existing vehicle.");
            }
            if (!Objects.equals(member.getDataOwnerRef(), fleet.getDataOwnerRef())) {
                throw new ValidationException("Fleet " + fleetId + " is owned by " + fleet.getDataOwnerRef() + " but has the member "
                        + member.getNetexId() + ", which is owned by " + member.getDataOwnerRef() + ". A fleet can only have members of its own data owner.");
            }
            if (isDeactivated(member)) {
                throw new ValidationException("Fleet " + fleetId + " has the member " + member.getNetexId() + ", which is deactivated.");
            }
        }

        for (VehicleType transportType : fleet.getTransportTypes()) {
            if (transportType == null || transportType.getId() == null) {
                throw new ValidationException("Fleet " + fleetId + " has a transport type that is not an existing vehicle type.");
            }
            if (!Objects.equals(transportType.getDataOwnerRef(), fleet.getDataOwnerRef())) {
                throw new ValidationException("Fleet " + fleetId + " is owned by " + fleet.getDataOwnerRef() + " but has the transport type "
                        + transportType.getNetexId() + ", which is owned by " + transportType.getDataOwnerRef() + ". A fleet can only have transport types of its own data owner.");
            }
            if (transportType.getVehicleTypeKind() == VehicleTypeKindEnumeration.CONTRACTUAL) {
                throw new ValidationException("Fleet " + fleetId + " has the contractual vehicle type " + transportType.getNetexId()
                        + " as a transport type. A fleet can only have physical vehicle types.");
            }
            if (isDeactivated(transportType)) {
                throw new ValidationException("Fleet " + fleetId + " has the transport type " + transportType.getNetexId() + ", which is deactivated.");
            }
        }
    }

    private static boolean isDeactivated(org.rutebanken.sobek.model.EntityInVersionStructure entity) {
        return entity.getValidBetween() != null
                && entity.getValidBetween().getToDate() != null
                && !entity.getValidBetween().getToDate().isAfter(Instant.now());
    }
}
