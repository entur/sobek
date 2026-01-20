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
import org.rutebanken.sobek.repository.VehicleRepository;
import org.springframework.stereotype.Component;

@Component
public class VehicleVersionedSaverService {

    private final VehicleRepository vehicleRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;

    public VehicleVersionedSaverService(VehicleRepository vehicleRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService) {
        this.vehicleRepository = vehicleRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
    }

    public Vehicle saveNewVersion(Vehicle newVersion) {
        return defaultVersionedSaverService.saveNewVersion(newVersion, vehicleRepository);
    }
}
