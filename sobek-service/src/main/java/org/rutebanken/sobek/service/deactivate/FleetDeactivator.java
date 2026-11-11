package org.rutebanken.sobek.service.deactivate;

import jakarta.validation.ValidationException;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FleetDeactivator extends GenericDeactivator<Fleet> {

    private final FleetVersionedSaverService fleetVersionedSaverService;
    private final FleetRepository fleetRepository;

    public FleetDeactivator(FleetVersionedSaverService fleetVersionedSaverService,
                            FleetRepository fleetRepository,
                            AuthorizationService authorizationService,
                            UsernameFetcher usernameFetcher,
                            VersionCreator versionCreator) {
        super(usernameFetcher, versionCreator, authorizationService, Fleet.class);
        this.fleetVersionedSaverService = fleetVersionedSaverService;
        this.fleetRepository = fleetRepository;
    }

    public Fleet deactivateFleet(String netexId, Long expectedVersion, Instant deactivateAt) throws ValidationException {
        return super.deactivateObject(netexId, expectedVersion, deactivateAt, "fleet", fleetRepository::findFirstByNetexIdOrderByVersionDesc, fleetVersionedSaverService::saveNewVersion, f -> null);
    }
}
