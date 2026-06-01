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

package org.rutebanken.sobek.rest.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.rest.graphql.helpers.FilterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.DEFAULT_PAGE_VALUE;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.DEFAULT_SIZE_VALUE;

@Service
public class VehicleTypeFetcher implements DataFetcher<Map<String, Object>> {

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(DataFetchingEnvironment env) {
        int page = env.getArgumentOrDefault(PAGE, DEFAULT_PAGE_VALUE);
        int size = env.getArgumentOrDefault(SIZE, DEFAULT_SIZE_VALUE);

        Map<String, Object> filter = env.getArgument(FILTER);
        List<String> netexIds = FilterHelper.getNetexIdsFromFilter(filter);
        String name = FilterHelper.getNameFromFilter(filter);
        List<AllPublicTransportModesEnumeration> modes = FilterHelper.getModesFromFilter(filter);

        var result = vehicleTypeRepository.findCurrentFiltered(netexIds, modes, name, PageRequest.of(page, size));
        return PageResult.from(result, page, size);
    }
}
