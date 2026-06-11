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

package org.rutebanken.sobek.service.deactivate;

import jakarta.xml.bind.ValidationException;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class VehicleDeactivator extends GenericDeactivator<Vehicle> {
    private final VehicleVersionedSaverService vehicleVersionedSaverService;
    private final VehicleRepository vehicleRepository;

    public VehicleDeactivator(VehicleVersionedSaverService vehicleVersionedSaverService,
                              VehicleRepository vehicleRepository,
                              AuthorizationService authorizationService,
                              UsernameFetcher usernameFetcher,
                              VersionCreator versionCreator) {
        super(usernameFetcher, versionCreator, authorizationService, Vehicle.class);
        this.vehicleVersionedSaverService = vehicleVersionedSaverService;
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle deactivateVehicle(String netexId, Long expectedVersion, Instant deactivateAt) throws ValidationException {
        return super.deactivateObject(netexId, expectedVersion, deactivateAt, "vehicle", vehicleRepository::findFirstByNetexIdOrderByVersionDesc, vehicleVersionedSaverService::saveNewVersion, v -> null);
    }
}
