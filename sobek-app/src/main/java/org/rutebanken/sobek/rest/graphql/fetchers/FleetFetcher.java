package org.rutebanken.sobek.rest.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.rest.graphql.helpers.FilterHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.DEFAULT_PAGE_VALUE;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.DEFAULT_SIZE_VALUE;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.FILTER;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.PAGE;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.SIZE;

@Service
public class FleetFetcher implements DataFetcher<Map<String, Object>> {

    private final FleetRepository fleetRepository;
    private final AuthorizationService authorizationService;

    public FleetFetcher(FleetRepository fleetRepository, AuthorizationService authorizationService) {
        this.fleetRepository = fleetRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public Map<String, Object> get(DataFetchingEnvironment env) {
        int page = env.getArgumentOrDefault(PAGE, DEFAULT_PAGE_VALUE);
        int size = env.getArgumentOrDefault(SIZE, DEFAULT_SIZE_VALUE);

        Map<String, Object> filter = env.getArgument(FILTER);
        List<String> netexIds = FilterHelper.getNetexIdsFromFilter(filter);
        String name = FilterHelper.getNameFromFilter(filter);
        String dataOwnerRef = FilterHelper.getDataOwnerRefFromFilter(filter);

        authorizationService.verifyCanViewDataOwner(dataOwnerRef);

        var result = fleetRepository.findCurrentFiltered(dataOwnerRef, netexIds, name, PageRequest.of(page, size));
        return PageResult.from(result, page, size);
    }
}
