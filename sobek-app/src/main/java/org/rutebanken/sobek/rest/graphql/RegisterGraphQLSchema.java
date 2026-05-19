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

package org.rutebanken.sobek.rest.graphql;

import graphql.language.IntValue;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import jakarta.annotation.PostConstruct;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.rutebanken.sobek.rest.graphql.fetchers.*;
import org.rutebanken.sobek.rest.graphql.scalars.DateScalar;
import org.rutebanken.sobek.rest.graphql.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;
import static org.rutebanken.sobek.rest.graphql.types.CustomGraphQLTypes.organisationTypeEnumType;
import static org.rutebanken.sobek.rest.graphql.types.CustomGraphQLTypes.transportModeEnumType;

@Component
public class RegisterGraphQLSchema {

    public final static int DEFAULT_PAGE_VALUE = 0;
    public final static int DEFAULT_SIZE_VALUE = 20;

    public GraphQLSchema vehicleRegisterSchema;

    @Autowired
    private VehicleTypeObjectTypeCreator vehicleTypeObjectTypeCreator;
    @Autowired
    private DeckPlanObjectTypeCreator deckPlanObjectTypeCreator;
    @Autowired
    private VehicleObjectTypeCreator vehicleObjectTypeCreator;
    @Autowired
    private OrganisationObjectTypeCreator organisationObjectTypeCreator;
    @Autowired
    private PassengerCapacityObjectTypeCreator passengerCapacityObjectTypeCreator;

    @Autowired
    DataFetcher vehicleTypeFetcher;
    @Autowired
    private VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher;

    @Autowired
    DataFetcher deckPlanFetcher;

    @Autowired
    DataFetcher organisationFetcher;

    @Autowired
    DataFetcher vehicleFetcher;

    @Autowired
    DateScalar dateScalar;

    @Autowired
    private KeyValuesDataFetcher keyValuesDataFetcher;

    @Autowired
    private AuthorizationService authorizationService;

    @PostConstruct
    public void init() {

        // Create type for the deck plans query
        GraphQLObjectType deckPlanObjectType = deckPlanObjectTypeCreator.create();
        GraphQLObjectType deckPlanPageType = createPageType(OUTPUT_TYPE_DECK_PLAN_PAGE, deckPlanObjectType);
        GraphQLObjectType passengerCapacityObjectType = passengerCapacityObjectTypeCreator.create();

        // Create type for the vehicleTypes query, including vehicles (without vehicle type child) and deck plan in the structure
        GraphQLObjectType vehicleTypeObjectType = vehicleTypeObjectTypeCreator.create(OUTPUT_TYPE_VEHICLE_TYPE,
                deckPlanObjectType,
                vehicleObjectTypeCreator.create(OUTPUT_TYPE_VEHICLE_TYPE_VEHICLE,
                        null,
                        dateScalar.getGraphQLDateScalar()),
                passengerCapacityObjectType,
                dateScalar.getGraphQLDateScalar());
        GraphQLObjectType vehicleTypePageType = createPageType(OUTPUT_TYPE_VEHICLE_TYPE_PAGE, vehicleTypeObjectType);
        GraphQLInputObjectType vehicleTypeFilterInput = newInputObject()
                .name(INPUT_TYPE_VEHICLE_TYPE_FILTER)
                .field(newInputObjectField().name(FILTER_IDS).type(new GraphQLList(new GraphQLNonNull(GraphQLString))).description("Batch lookup by NeTEx IDs"))
                .field(newInputObjectField().name(FILTER_TRANSPORT_MODES).type(new GraphQLList(transportModeEnumType)).description("Filter by transport mode"))
                .build();

        // Create type for the organisations query
        GraphQLObjectType organisationType = organisationObjectTypeCreator.create();
        GraphQLObjectType organisationPageType = createPageType(OUTPUT_TYPE_ORGANISATION_PAGE, organisationType);
        GraphQLInputObjectType organisationsFilterInput = newInputObject()
                .name(INPUT_TYPE_ORGANISATIONS_FILTER)
                .field(newInputObjectField().name(FILTER_IDS).type(new GraphQLList(new GraphQLNonNull(GraphQLString))).description("Batch lookup by NeTEx IDs"))
                .field(newInputObjectField().name(FILTER_ORGANISATION_TYPE).type(organisationTypeEnumType).description("Filter by organisation type"))
                .build();

        // Create type for the vehicles query, including their vehicle type, but each vehicle type doesn't include it's structure
        GraphQLObjectType vehiclePageType = createPageType(OUTPUT_TYPE_VEHICLE_PAGE,
                vehicleObjectTypeCreator.create(OUTPUT_TYPE_VEHICLE,
                        vehicleTypeObjectTypeCreator.create(OUTPUT_TYPE_VEHICLE_VEHICLE_TYPE,
                                null,
                                null,
                                passengerCapacityObjectType,
                                dateScalar.getGraphQLDateScalar()),
                        dateScalar.getGraphQLDateScalar()));
        GraphQLInputObjectType vehicleFilterInput = newInputObject()
                .name(INPUT_TYPE_VEHICLE_FILTER)
                .field(newInputObjectField().name(FILTER_IDS).type(new GraphQLList(new GraphQLNonNull(GraphQLString))).description("Batch lookup by NeTEx IDs"))
                .field(newInputObjectField().name(FILTER_TRANSPORT_MODES).type(new GraphQLList(transportModeEnumType)).description("Filter by transport mode"))
                .build();


        GraphQLObjectType vehicleRegistryQuery = newObject()
                .name(VEHICLE_REGISTER)
                .description("Query and search for data")
                .field(newFieldDefinition()
                        .name(LIST_NAME_VEHICLES)
                        .type(vehiclePageType)
                        .description("Paged vehicles with optional filtering")
                        .argument(GraphQLArgument.newArgument().name(FILTER).type(vehicleFilterInput))
                        .arguments(createPageAndSizeArguments())
                )
                .field(newFieldDefinition()
                        .name(LIST_NAME_VEHICLE_TYPES)
                        .type(vehicleTypePageType)
                        .description("Paged vehicle types with optional filtering")
                        .argument(GraphQLArgument.newArgument().name(FILTER).type(vehicleTypeFilterInput))
                        .arguments(createPageAndSizeArguments())
                )
                .field(newFieldDefinition()
                        .name(LIST_NAME_DECK_PLANS)
                        .type(deckPlanPageType)
                        .description("Paged deck plans")
                        .arguments(createPageAndSizeArguments())
                )
                .field(newFieldDefinition()
                        .name(LIST_NAME_ORGANISATIONS)
                        .type(organisationPageType)
                        .description("Paged organisations")
                        .argument(GraphQLArgument.newArgument().name(FILTER).type(organisationsFilterInput))
                        .arguments(createPageAndSizeArguments())
                )
                .build();

        vehicleRegisterSchema = GraphQLSchema.newSchema()
                .query(vehicleRegistryQuery)
                .codeRegistry(buildCodeRegistry())
                .build();
    }

    private GraphQLCodeRegistry buildCodeRegistry() {
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_DECK_PLAN, PROPERTY_ID, getNetexIdFetcher());

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, PROPERTY_KEY_VALUES, keyValuesDataFetcher);
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, PROPERTY_ID, getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, OUTPUT_TYPE_VEHICLE_TYPE_DECK_PLAN, vehicleTypeDeckPlanFetcher);
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, PROPERTY_CHANGED_BY, getChangedByFetcher(authorizationService));
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE, PROPERTY_ID, getNetexIdFetcher());

        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, LIST_NAME_DECK_PLANS, deckPlanFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, LIST_NAME_ORGANISATIONS, organisationFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, LIST_NAME_VEHICLES, vehicleFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, LIST_NAME_VEHICLE_TYPES, vehicleTypeFetcher);

        return codeRegistryBuilder.build();
    }

    private static DataFetcher<Object> getNetexIdFetcher() {
        return env -> {
            if (env.getSource() instanceof IdentifiedEntity identifiedEntity) {
                return identifiedEntity.getNetexId();
            }
            return null;
        };
    }

    private static DataFetcher<Object> getChangedByFetcher(AuthorizationService authorizationService) {
        return env -> {
            if (env.getSource() instanceof DataManagedObjectStructure dmo && !authorizationService.isGuest()) {
                return dmo.getChangedBy();
            }
            return null;
        };
    }

    private void registerDataFetcher(GraphQLCodeRegistry.Builder codeRegistryBuilder, String parentType, String fieldName, DataFetcher<?> dataFetcher) {
        FieldCoordinates coordinates = FieldCoordinates.coordinates(parentType, fieldName);
        codeRegistryBuilder.dataFetcher(coordinates, dataFetcher);
    }

    private List<GraphQLArgument> createPageAndSizeArguments() {
        List<GraphQLArgument> arguments = new ArrayList<>();
        arguments.add(GraphQLArgument.newArgument()
                .name(PAGE)
                .type(GraphQLInt)
                .defaultValueLiteral(IntValue.of(DEFAULT_PAGE_VALUE))
                .description(PAGE_ARG_DESCRIPTION)
                .build());
        arguments.add(GraphQLArgument.newArgument()
                .name(SIZE)
                .type(GraphQLInt)
                .defaultValueLiteral(IntValue.of(DEFAULT_SIZE_VALUE))
                .description(SIZE_ARG_DESCRIPTION)
                .build());
        return arguments;
    }

    private static GraphQLObjectType createPageType(String name, GraphQLObjectType contentType) {
        return newObject()
                .name(name)
                .field(newFieldDefinition().name(CONTENT).type(new GraphQLList(contentType)))
                .field(newFieldDefinition().name(TOTAL_ELEMENTS).type(new GraphQLNonNull(GraphQLInt)))
                .field(newFieldDefinition().name(PAGE).type(new GraphQLNonNull(GraphQLInt)))
                .field(newFieldDefinition().name(SIZE).type(new GraphQLNonNull(GraphQLInt)))
                .build();
    }
}
