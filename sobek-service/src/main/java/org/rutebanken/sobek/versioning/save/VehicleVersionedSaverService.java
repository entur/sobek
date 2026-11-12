/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.versioning.save;


import org.rutebanken.sobek.error.VehicleTypeKindViolationException;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VehicleVersionedSaverService {

    private final VehicleRepository vehicleRepository;
    private final FleetRepository fleetRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;

    public VehicleVersionedSaverService(VehicleRepository vehicleRepository, FleetRepository fleetRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService) {
        this.vehicleRepository = vehicleRepository;
        this.fleetRepository = fleetRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
    }

    public Vehicle saveNewVersion(Vehicle newVersion) {
        Vehicle existingVersion = newVersion.getNetexId() == null ? null
                : vehicleRepository.findFirstByNetexIdOrderByVersionDesc(newVersion.getNetexId());
        return saveNewVersion(existingVersion, newVersion, Instant.now());
    }

    public Vehicle saveNewVersion(Vehicle existingVersion, Vehicle newVersion, Instant defaultValidFrom) {
        verifyTransportTypeIsPhysical(newVersion);
        Vehicle saved = defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, defaultValidFrom, vehicleRepository);
        if (existingVersion != null && !saved.getId().equals(existingVersion.getId())) {
            // The fleets that have this vehicle as a member follow it to the new version
            fleetRepository.moveMember(existingVersion.getId(), saved.getId());
        }
        return saved;
    }

    /**
     * A Vehicle is a real-world object, so it can only refer to a physical vehicle type.
     */
    private void verifyTransportTypeIsPhysical(Vehicle vehicle) {
        VehicleType transportType = vehicle.getTransportType();
        if (transportType != null && transportType.getVehicleTypeKind() == VehicleTypeKindEnumeration.CONTRACTUAL) {
            throw new VehicleTypeKindViolationException("Vehicle " + vehicle.getNetexId() + " refers to the contractual vehicle type "
                    + transportType.getNetexId() + ". Vehicles can only refer to physical vehicle types.");
        }
    }

}
