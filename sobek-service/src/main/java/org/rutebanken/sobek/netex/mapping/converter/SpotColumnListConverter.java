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
import org.rutebanken.netex.model.SpotColumns_RelStructure;
import org.rutebanken.sobek.model.vehicle.SpotColumn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpotColumnListConverter extends BidirectionalConverter<List<SpotColumn>, SpotColumns_RelStructure> {


    @Override
    public SpotColumns_RelStructure convertTo(List<SpotColumn> spotColumns, Type<SpotColumns_RelStructure> type, MappingContext mappingContext) {

        if(spotColumns == null || spotColumns.isEmpty()) {
            return null;
        }

        return new SpotColumns_RelStructure()
                .withSpotColumn(spotColumns.stream()
                        .map(ds -> mapperFacade.map(ds, org.rutebanken.netex.model.SpotColumn.class))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<SpotColumn> convertFrom(SpotColumns_RelStructure spotColumnsRelStructure, Type<List<SpotColumn>> type, MappingContext mappingContext) {
        return null;
    }
}

