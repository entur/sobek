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
import org.rutebanken.netex.model.DeckSpaceCapacities_RelStructure;
import org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeckSpaceCapacityListConverter extends BidirectionalConverter<List<DeckSpaceCapacity>, DeckSpaceCapacities_RelStructure> {

    @Override
    public DeckSpaceCapacities_RelStructure convertTo(List<DeckSpaceCapacity> sobekList, Type<DeckSpaceCapacities_RelStructure> type, MappingContext mappingContext) {

        if(sobekList == null || sobekList.isEmpty()) {
            return null;
        }

        return new DeckSpaceCapacities_RelStructure()
                .withDeckSpaceCapacity(sobekList.stream()
                        .map(ds -> mapperFacade.map(ds, org.rutebanken.netex.model.DeckSpaceCapacity.class))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<DeckSpaceCapacity> convertFrom(DeckSpaceCapacities_RelStructure sobekListRelStructure, Type<List<DeckSpaceCapacity>> type, MappingContext mappingContext) {
        return null;
    }
}

