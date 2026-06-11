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
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class DeckPlanDeactivator extends GenericDeactivator<DeckPlan> {
    private final DeckPlanVersionedSaverService deckPlanVersionedSaverService;
    private final DeckPlanRepository deckPlanRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public DeckPlanDeactivator(DeckPlanVersionedSaverService deckPlanVersionedSaverService,
                               DeckPlanRepository deckPlanRepository,
                               VehicleTypeRepository vehicleTypeRepository,
                               UsernameFetcher usernameFetcher,
                               VersionCreator versionCreator,
                               AuthorizationService authorizationService) {
        super(usernameFetcher, versionCreator, authorizationService, DeckPlan.class);
        this.deckPlanVersionedSaverService = deckPlanVersionedSaverService;
        this.deckPlanRepository = deckPlanRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    public DeckPlan deactivateDeckPlan(String netexId, Long expectedVersion, Instant suggestedTimeOfDeactivation) throws ValidationException {
        return super.deactivateObject(netexId,
                expectedVersion,
                suggestedTimeOfDeactivation,
                "deck plan",
                deckPlanRepository::findFirstByNetexIdOrderByVersionDesc,
                deckPlanVersionedSaverService::saveNewVersion,
                deckPlan -> {
                    if(vehicleTypeRepository.existsValidWithDeckPlan(netexId, expectedVersion)) {
                        return "Cannot deactivate deck plan " + netexId + " because it is still in use.";
                    }
                    return null;
                });
    }
}
