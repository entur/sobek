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
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;

@Service("vehicleTypesPageFetcher")
@Transactional
class VehicleTypesPageFetcher implements DataFetcher<Map<String, Object>> {

    static final int DEFAULT_SIZE = 25;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(DataFetchingEnvironment env) {
        int page = env.getArgumentOrDefault(PAGE, 0);
        int size = env.getArgumentOrDefault(SIZE, DEFAULT_SIZE);

        List<String> ids = null;
        AllPublicTransportModesEnumeration mode = null;

        Map<String, Object> filter = env.getArgument(FILTER);
        if (filter != null) {
            ids = (List<String>) filter.get(IDS);
            Object modeArg = filter.get(TRANSPORT_MODE);
            if (modeArg instanceof AllPublicTransportModesEnumeration m) {
                mode = m;
            } else if (modeArg instanceof String s) {
                mode = AllPublicTransportModesEnumeration.fromValue(s);
            }
        }

        // Stub: in-memory filtering/paging over findAllCurrent() until Layer 2 adds repo support
        List<VehicleType> all = vehicleTypeRepository.findAllCurrent();

        final List<String> filterIds = ids;
        final AllPublicTransportModesEnumeration filterMode = mode;
        List<VehicleType> filtered = all.stream()
                .filter(vt -> filterIds == null || filterIds.isEmpty() || filterIds.contains(vt.getNetexId()))
                .filter(vt -> filterMode == null || filterMode.equals(vt.getTransportMode()))
                .toList();

        int total = filtered.size();
        int fromIdx = Math.min(page * size, total);
        int toIdx = Math.min(fromIdx + size, total);
        List<VehicleType> content = filtered.subList(fromIdx, toIdx);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(CONTENT, content);
        result.put(TOTAL_ELEMENTS, total);
        result.put(PAGE, page);
        result.put(SIZE, size);
        return result;
    }
}
