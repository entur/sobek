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
import org.rutebanken.netex.model.SeatEquipmentRefStructure;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.SeatEquipment;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SeatEquipmentRefConverter extends BidirectionalConverter<SeatEquipmentRefStructure, SeatEquipment> {

    private static final Logger logger = LoggerFactory.getLogger(SeatEquipmentRefConverter.class);

    // TODO: a mapper or converter should ideally not use repositories
    private final ReferenceResolver resolver;

    @Autowired
    public SeatEquipmentRefConverter(ReferenceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public SeatEquipment convertTo(SeatEquipmentRefStructure seatEquipmentRefStructure, Type<SeatEquipment> type, MappingContext mappingContext) {
        SeatEquipment seatEquipment = resolver.resolve(new VersionOfObjectRefStructure(seatEquipmentRefStructure.getRef(), seatEquipmentRefStructure.getVersion()), SeatEquipment.class);
        if(seatEquipment != null) {
            return seatEquipment;
        }
        throw new NetexMappingException("Cannot find deck plan from ref: " +seatEquipmentRefStructure.getRef());
    }

    @Override
    public SeatEquipmentRefStructure convertFrom(SeatEquipment seatEquipment, Type<SeatEquipmentRefStructure> type, MappingContext mappingContext) {
        SeatEquipmentRefStructure seatEquipmentRefStructure = new SeatEquipmentRefStructure()
                .withCreated(LocalDateTime.now())
                .withRef(seatEquipment.getNetexId())
                .withVersion(String.valueOf(seatEquipment.getVersion()));

        logger.debug("Mapped luggage spot equipment ref structure: {}", seatEquipmentRefStructure);

        return seatEquipmentRefStructure;
    }
}
