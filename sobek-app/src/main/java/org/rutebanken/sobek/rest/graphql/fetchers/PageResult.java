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

import org.springframework.data.domain.Page;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;

/**
 * Converts a Spring Data {@link Page} to the Map structure that graphql-java's
 * PropertyDataFetcher resolves for the page wrapper types (VehicleTypePage, DeckPlanPage, etc.).
 */
final class PageResult {

    private PageResult() {}

    static Map<String, Object> from(Page<?> page, int pageNum, int size) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(CONTENT, page.getContent());
        m.put(TOTAL_ELEMENTS, Math.toIntExact(page.getTotalElements()));
        m.put(PAGE, pageNum);
        m.put(SIZE, size);
        return m;
    }
}
