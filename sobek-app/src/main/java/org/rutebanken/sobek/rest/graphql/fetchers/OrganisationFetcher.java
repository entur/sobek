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
import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.rutebanken.sobek.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;
import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_PAGE_VALUE;
import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_SIZE_VALUE;

@Service("organisationFetcher")
@Transactional
class OrganisationFetcher implements DataFetcher<Map<String, Object>> {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(DataFetchingEnvironment env) {
        int page = env.getArgumentOrDefault(PAGE, DEFAULT_PAGE_VALUE);
        int size = env.getArgumentOrDefault(SIZE, DEFAULT_SIZE_VALUE);

        Map<String, Object> filter = env.getArgument(FILTER);
        List<String> ids = null;
        OrganisationTypeEnumeration type = null;
        if (filter != null) {
            ids = (List<String>) filter.get(IDS);
            Object orgArg = filter.get(ORGANISATION_TYPE);
            if (orgArg instanceof org.rutebanken.netex.model.OrganisationTypeEnumeration t) {
                type = t;
            } else if (orgArg instanceof String s) {
                type = OrganisationTypeEnumeration.fromValue(s);
            }
        }
        onlyAuthorized = (Boolean)filter.get(USER_AUTHORIZED);
        List<String> authorizedIds = null;
        if(onlyAuthorized != null && onlyAuthorized) {
            authorizedIds = authorizationService.getOrganisationRefsUserIsAuthorizedFor();
        }


        var result = organisationRepository.findCurrentFiltered(ids, type, authorizedIds, PageRequest.of(page, size));
        return PageResult.from(result, page, size);
    }
}
