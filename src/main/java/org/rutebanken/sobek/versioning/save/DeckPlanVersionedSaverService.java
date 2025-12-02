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


import lombok.extern.java.Log;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Log
@Component
public class DeckPlanVersionedSaverService {

    private final DeckPlanRepository deckPlanRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;

    public DeckPlanVersionedSaverService(DeckPlanRepository deckPlanRepository, VehicleTypeRepository vehicleTypeRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService) {
        this.deckPlanRepository = deckPlanRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
    }


    public DeckPlan saveNewVersion(DeckPlan newVersion) {
        var existingVersion = deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(newVersion.getNetexId());
        if (existingVersion != null) {
            log.log(Level.FINE, "Found existing entity from netexId {}", existingVersion.getNetexId());
        }

        var saved = defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, deckPlanRepository);
        if(existingVersion != null && !saved.getId().equals(existingVersion.getId())) {
            vehicleTypeRepository.moveToDeckPlan(existingVersion.getId(), saved.getId());
        }
        return saved;
    }
}