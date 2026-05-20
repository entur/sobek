package org.rutebanken.sobek.rest.graphql;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.rutebanken.sobek.rest.graphql.fetchers.*;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * GraphQL Query Controller for Sobek Vehicle Register
 */
@Controller
public class QueryController {

    private final VehicleFetcher vehicleFetcher;
    private final VehicleTypeFetcher vehicleTypeFetcher;
    private final DeckPlanFetcher deckPlanFetcher;
    private final OrganisationFetcher organisationFetcher;
    private final VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher;
    private final KeyValuesDataFetcher keyValuesDataFetcher;
    private final AuthorizationService authorizationService;

    public QueryController(
            VehicleFetcher vehicleFetcher,
            VehicleTypeFetcher vehicleTypeFetcher,
            DeckPlanFetcher deckPlanFetcher,
            OrganisationFetcher organisationFetcher,
            VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher,
            KeyValuesDataFetcher keyValuesDataFetcher,
            AuthorizationService authorizationService) {
        this.vehicleFetcher = vehicleFetcher;
        this.vehicleTypeFetcher = vehicleTypeFetcher;
        this.deckPlanFetcher = deckPlanFetcher;
        this.organisationFetcher = organisationFetcher;
        this.vehicleTypeDeckPlanFetcher = vehicleTypeDeckPlanFetcher;
        this.keyValuesDataFetcher = keyValuesDataFetcher;
        this.authorizationService = authorizationService;
    }

    // Query mappings
    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object vehicles(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size) throws Exception {
        graphql.schema.DataFetchingEnvironment env = createEnvironment("vehicles", filter, page, size);
        return vehicleFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object vehicleTypes(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size)  {
        graphql.schema.DataFetchingEnvironment env = createEnvironment("vehicleTypes", filter, page, size);
        return vehicleTypeFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object deckPlans(@Argument Integer page, @Argument Integer size) {
        graphql.schema.DataFetchingEnvironment env = createEnvironment("deckPlans", null, page, size);
        return deckPlanFetcher.get(env);
    }

    @QueryMapping
    @Transactional(readOnly = true)  // Single transaction for entire query
    public Object organisations(@Argument Map<String, Object> filter, @Argument Integer page, @Argument Integer size)  {
        graphql.schema.DataFetchingEnvironment env = createEnvironment("organisations", filter, page, size);
        return organisationFetcher.get(env);
    }

    // Field mappings for VehicleType
    @SchemaMapping(typeName = "VehicleType", field = "keyValues")
    public Object vehicleTypeKeyValues(Object source)  {
        graphql.schema.DataFetchingEnvironment env = createFieldEnvironment(source);
        return keyValuesDataFetcher.get(env);
    }

    @SchemaMapping(typeName = "VehicleType", field = "deckPlan")
    public Object vehicleTypeDeckPlan(Object source)  {
        graphql.schema.DataFetchingEnvironment env = createFieldEnvironment(source);
        return vehicleTypeDeckPlanFetcher.get(env);
    }

    @SchemaMapping(typeName = "VehicleType", field = "id")
    public String vehicleTypeId(Object source) {
        return getNetexId(source);
    }

    @SchemaMapping(typeName = "VehicleType", field = "changedBy")
    public String vehicleTypeChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for Vehicle
    @SchemaMapping(typeName = "Vehicle", field = "id")
    public String vehicleId(Object source) {
        return getNetexId(source);
    }

    @SchemaMapping(typeName = "Vehicle", field = "changedBy")
    public String vehicleChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for VehicleTypeVehicle
    @SchemaMapping(typeName = "VehicleTypeVehicle", field = "changedBy")
    public String vehicleTypeVehicleChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Field mappings for DeckPlan
    @SchemaMapping(typeName = "DeckPlan", field = "id")
    public String deckPlanId(Object source) {
        return getNetexId(source);
    }

    @SchemaMapping(typeName = "DeckPlan", field = "changedBy")
    public String deckPlanChangedBy(Object source) {
        return getChangedBy(source);
    }

    // Helper methods
    private String getNetexId(Object source) {
        if (source instanceof IdentifiedEntity identifiedEntity) {
            return identifiedEntity.getNetexId();
        }
        return null;
    }

    private String getChangedBy(Object source) {
        if (source instanceof DataManagedObjectStructure dmo && !authorizationService.isGuest()) {
            return dmo.getChangedBy();
        }
        return null;
    }

    private graphql.schema.DataFetchingEnvironment createEnvironment(
            String fieldName,
            Map<String, Object> filter,
            Integer page,
            Integer size) {
        Map<String, Object> arguments = new java.util.HashMap<>();
        if (filter != null) {
            arguments.put("filter", filter);
        }
        if (page != null) {
            arguments.put("page", page);
        }
        if (size != null) {
            arguments.put("size", size);
        }

        return graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .arguments(arguments)
                .build();
    }

    private graphql.schema.DataFetchingEnvironment createFieldEnvironment(Object source) {
        return graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .source(source)
                .build();
    }
}