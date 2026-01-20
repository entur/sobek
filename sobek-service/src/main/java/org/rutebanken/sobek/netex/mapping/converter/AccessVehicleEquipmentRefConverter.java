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

package org.rutebanken.sobek.netex.mapping.converter;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.rutebanken.netex.model.AccessVehicleEquipmentRefStructure;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccessVehicleEquipmentRefConverter extends BidirectionalConverter<AccessVehicleEquipmentRefStructure, AccessVehicleEquipment> {

    private static final Logger logger = LoggerFactory.getLogger(AccessVehicleEquipmentRefConverter.class);

    // TODO: a mapper or converter should ideally not use repositories
    private final ReferenceResolver resolver;

    @Autowired
    public AccessVehicleEquipmentRefConverter(ReferenceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public AccessVehicleEquipment convertTo(AccessVehicleEquipmentRefStructure accessVehicleEquipmentRefStructure, Type<AccessVehicleEquipment> type, MappingContext mappingContext) {
        AccessVehicleEquipment accessVehicleEquipment = resolver.resolve(new VersionOfObjectRefStructure(accessVehicleEquipmentRefStructure.getRef(), accessVehicleEquipmentRefStructure.getVersion()), AccessVehicleEquipment.class);
        if(accessVehicleEquipment != null) {
            return accessVehicleEquipment;
        }
        throw new NetexMappingException("Cannot find access vehicle equipment from ref: " +accessVehicleEquipmentRefStructure.getRef());
    }

    @Override
    public AccessVehicleEquipmentRefStructure convertFrom(AccessVehicleEquipment accessVehicleEquipment, Type<AccessVehicleEquipmentRefStructure> type, MappingContext mappingContext) {
        AccessVehicleEquipmentRefStructure accessVehicleEquipmentRefStructure = new AccessVehicleEquipmentRefStructure()
                .withCreated(LocalDateTime.now())
                .withRef(accessVehicleEquipment.getNetexId())
                .withVersion(String.valueOf(accessVehicleEquipment.getVersion()));

        logger.debug("Mapped access vehicle equipment ref structure: {}", accessVehicleEquipmentRefStructure);

        return accessVehicleEquipmentRefStructure;
    }
}
