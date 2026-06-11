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

package org.rutebanken.sobek.versioning;

import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates new version of already existing objects, by mapping with Orika and ignore primary key "id".
 */
@Service
public class VersionCreator {

    private static final Logger logger = LoggerFactory.getLogger(VersionCreator.class);

    private MappingContext mappingContext;

    @Autowired
    public VersionCreator(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Create next version of entity (copy), before changes are made.
     * Does not increment version or valid between:  Will be done by saver service
     *
     * @param entityInVersionStructure
     * @param type extends {@link EntityInVersionStructure}
     * @return a deep copied stop place with incremented version and valid between set.
     */
    public <T extends EntityInVersionStructure> T createCopy(T entityInVersionStructure, Class<T> type) {
        logger.debug("Create new version for entity: {}", entityInVersionStructure);

        T copy = null;
        if (entityInVersionStructure instanceof Vehicle) {
            // Copy vehicle, but keep the same transport type reference
            Vehicle original = (Vehicle) entityInVersionStructure;
            Vehicle vehicleCopy = mappingContext.getVersionCopyMapper().copy(original);
            if(vehicleCopy.getTransportType() != null) {
                vehicleCopy.getTransportType().setId(original.getTransportType().getId());
            }
            copy = type.cast(vehicleCopy);
        } else if (entityInVersionStructure instanceof VehicleType) {
            // Copy vehicle type, but keep the same deck plan reference
            VehicleType original = (VehicleType) entityInVersionStructure;
            VehicleType vehicleTypeCopy = mappingContext.getVersionCopyMapper().copy(original);
            if(vehicleTypeCopy.getDeckPlan() != null) {
                vehicleTypeCopy.getDeckPlan().setId(original.getDeckPlan().getId());
            }
            copy = type.cast(vehicleTypeCopy);
        } else if (entityInVersionStructure instanceof DeckPlan) {
            // For deck plan, we can just copy the entity as they don't have references to Vehicle or VehicleType
            copy = type.cast(mappingContext.getVersionCopyMapper().copy((DeckPlan) entityInVersionStructure));
        }

        logger.debug("Created copy of entity: {}", copy);

        return copy;
    }
}
