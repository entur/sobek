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
import javassist.NotFoundException;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.ALL_VERSIONS;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.ID;

@Service("deckPlanFetcher")
@Transactional
class DeckPlanFetcher implements DataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(DeckPlanFetcher.class);

    @Autowired
    private DeckPlanRepository deckPlanRepository;


    @Override
    public Object get(DataFetchingEnvironment environment) throws NotFoundException {
        String netexId = environment.getArgument(ID);
        boolean allVersions = Boolean.TRUE.equals(environment.getArgument(ALL_VERSIONS));

        if (netexId != null) {

            logger.debug("Returning deck plan from netexId: {}", netexId);
            if (allVersions) {
                return deckPlanRepository.findByNetexId(netexId);
            } else {
                var deckPlan = deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(netexId);
                if(deckPlan == null) {
                    throw new NotFoundException("No DeckPlan found with id " + netexId);
                } else {
                    return List.of(deckPlan);
                }
            }
        }
        return deckPlanRepository.findAllCurrent();

    }
}
