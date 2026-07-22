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


import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VehicleVersionedSaverService {

    private final VehicleRepository vehicleRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;
    private final OrganisationRegistry organisationRegistry;

    public VehicleVersionedSaverService(VehicleRepository vehicleRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService, OrganisationRegistry organisationRegistry) {
        this.vehicleRepository = vehicleRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
        this.organisationRegistry = organisationRegistry;
    }

    public Vehicle saveNewVersion(Vehicle newVersion) {
        organisationRegistry.validateOrganisationRef(newVersion.getDataOwnerRef());
        return defaultVersionedSaverService.saveNewVersion(newVersion, vehicleRepository);
    }

    public Vehicle saveNewVersion(Vehicle existingVersion, Vehicle newVersion, Instant defaultValidFrom) {
        organisationRegistry.validateOrganisationRef(newVersion.getDataOwnerRef());
        return defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, defaultValidFrom, vehicleRepository);
    }

}
