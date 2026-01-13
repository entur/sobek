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
import org.rutebanken.netex.model.SpotEquipmentRefStructure;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.SpotEquipment;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SpotEquipmentRefConverter extends BidirectionalConverter<SpotEquipmentRefStructure, SpotEquipment> {

    private static final Logger logger = LoggerFactory.getLogger(SpotEquipmentRefConverter.class);

    // TODO: a mapper or converter should ideally not use repositories
    private final ReferenceResolver resolver;

    @Autowired
    public SpotEquipmentRefConverter(ReferenceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public SpotEquipment convertTo(SpotEquipmentRefStructure spotEquipmentRefStructure, Type<SpotEquipment> type, MappingContext mappingContext) {
        SpotEquipment spotEquipment = resolver.resolve(new VersionOfObjectRefStructure(spotEquipmentRefStructure.getRef(), spotEquipmentRefStructure.getVersion()), SpotEquipment.class);
        if(spotEquipment != null) {
            return spotEquipment;
        }
        throw new NetexMappingException("Cannot find deck plan from ref: " +spotEquipmentRefStructure.getRef());
    }

    @Override
    public SpotEquipmentRefStructure convertFrom(SpotEquipment spotEquipment, Type<SpotEquipmentRefStructure> type, MappingContext mappingContext) {
        SpotEquipmentRefStructure spotEquipmentRefStructure = new SpotEquipmentRefStructure()
                .withCreated(LocalDateTime.now())
                .withRef(spotEquipment.getNetexId())
                .withVersion(String.valueOf(spotEquipment.getVersion()));

        logger.debug("Mapped luggage spot equipment ref structure: {}", spotEquipmentRefStructure);

        return spotEquipmentRefStructure;
    }
}
