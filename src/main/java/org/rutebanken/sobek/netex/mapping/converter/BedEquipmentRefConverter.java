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
import org.rutebanken.netex.model.BedEquipmentRefStructure;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.BedEquipment;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BedEquipmentRefConverter extends BidirectionalConverter<BedEquipmentRefStructure, BedEquipment> {

    private static final Logger logger = LoggerFactory.getLogger(BedEquipmentRefConverter.class);

    // TODO: a mapper or converter should ideally not use repositories
    private final ReferenceResolver resolver;

    @Autowired
    public BedEquipmentRefConverter(ReferenceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public BedEquipment convertTo(BedEquipmentRefStructure bedEquipmentRefStructure, Type<BedEquipment> type, MappingContext mappingContext) {
        BedEquipment bedEquipment = resolver.resolve(new VersionOfObjectRefStructure(bedEquipmentRefStructure.getRef(), bedEquipmentRefStructure.getVersion()), BedEquipment.class);
        if(bedEquipment != null) {
            return bedEquipment;
        }
        throw new NetexMappingException("Cannot find deck plan from ref: " +bedEquipmentRefStructure.getRef());
    }

    @Override
    public BedEquipmentRefStructure convertFrom(BedEquipment bedEquipment, Type<BedEquipmentRefStructure> type, MappingContext mappingContext) {
        BedEquipmentRefStructure bedEquipmentRefStructure = new BedEquipmentRefStructure()
                .withCreated(LocalDateTime.now())
                .withRef(bedEquipment.getNetexId())
                .withVersion(String.valueOf(bedEquipment.getVersion()));

        logger.debug("Mapped luggage spot equipment ref structure: {}", bedEquipmentRefStructure);

        return bedEquipmentRefStructure;
    }
}
