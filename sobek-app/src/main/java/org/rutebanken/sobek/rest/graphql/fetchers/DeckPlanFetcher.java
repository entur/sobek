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
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;
import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_PAGE_VALUE;
import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_SIZE_VALUE;

@Service("deckPlanFetcher")
@Transactional
class DeckPlanFetcher implements DataFetcher<Map<String, Object>> {

    @Autowired
    private DeckPlanRepository deckPlanRepository;

    @Override
    public Map<String, Object> get(DataFetchingEnvironment env) {
        int page = env.getArgumentOrDefault(PAGE, DEFAULT_PAGE_VALUE);
        int size = env.getArgumentOrDefault(SIZE, DEFAULT_SIZE_VALUE);

        var result = deckPlanRepository.findCurrentPaged(PageRequest.of(page, size));
        return PageResult.from(result, page, size);
    }
}
