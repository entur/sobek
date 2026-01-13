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
import org.rutebanken.sobek.model.vehicle.SchematicMap;
import org.rutebanken.sobek.repository.SchematicMapRepository;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Log
@Component
public class SchematicMapVersionedSaverService {

    private final SchematicMapRepository schematicMapRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;

    public SchematicMapVersionedSaverService(SchematicMapRepository schematicMapRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService) {
        this.schematicMapRepository = schematicMapRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
    }


    public SchematicMap saveNewVersion(SchematicMap newVersion) {
        var existingVersion = schematicMapRepository.findFirstByNetexIdOrderByVersionDesc(newVersion.getNetexId());
        if (existingVersion != null) {
            log.log(Level.FINE, "Found existing entity from netexId {}", existingVersion.getNetexId());
        }

        return defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, schematicMapRepository);
    }
}