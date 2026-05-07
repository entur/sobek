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

import graphql.language.BooleanValue;
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

import static graphql.Scalars.GraphQLBoolean;
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
    public final static boolean DEFAULT_USER_AUTHORIZED_VALUE = false;

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
    DataFetcher vehicleTypeFetcher;
    @Autowired
    private VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher;

    @Autowired
    DataFetcher deckPlanFetcher;

    @Autowired
    DataFetcher organisationFetcher;

    @Autowired
    DateScalar dateScalar;

    @Autowired
    private KeyValuesDataFetcher keyValuesDataFetcher;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private UserPermissionsFetcher userPermissionsFetcher;

    @PostConstruct
    public void init() {

        GraphQLObjectType userPermissionsObjectType = newObject()
                .name(OUTPUT_TYPE_USER_PERMISSIONS)
                .field(newFieldDefinition()
                        .name("isGuest")
                        .type(GraphQLBoolean)
                        .build())
                .field(newFieldDefinition()
                        .name("allowNewStopEverywhere")
                        .type(GraphQLBoolean)
                        .build())
                .field(newFieldDefinition()
                        .name("preferredName")
                        .type(GraphQLString)
                        .build())
                .build();

        GraphQLObjectType deckPlanObjectType = deckPlanObjectTypeCreator.create();
        GraphQLObjectType vehicleObjectType = vehicleObjectTypeCreator.create();
        GraphQLObjectType vehicleTypeObjectType = vehicleTypeObjectTypeCreator.create(deckPlanObjectType, vehicleObjectType, dateScalar.getGraphQLDateScalar());
        GraphQLObjectType organisationType = organisationObjectTypeCreator.create();

        GraphQLObjectType vehicleTypePageType = createPageType(OUTPUT_TYPE_VEHICLE_TYPE_PAGE, vehicleTypeObjectType);

        GraphQLInputObjectType vehicleTypeFilterInput = newInputObject()
                .name(INPUT_TYPE_VEHICLE_TYPE_FILTER)
                .field(newInputObjectField().name(IDS).type(new GraphQLList(new GraphQLNonNull(GraphQLString))).description("Batch lookup by NeTEx IDs"))
                .field(newInputObjectField().name(TRANSPORT_MODE).type(transportModeEnumType).description("Filter by transport mode"))
                .build();

        GraphQLInputObjectType organisationsFilterInput = newInputObject()
                .name(INPUT_TYPE_ORGANISATIONS_FILTER)
                .field(newInputObjectField().name(IDS).type(new GraphQLList(new GraphQLNonNull(GraphQLString))).description("Batch lookup by NeTEx IDs"))
                .field(newInputObjectField().name(ORGANISATION_TYPE).type(organisationTypeEnumType).description("Filter by organisation type"))
                .field(newInputObjectField().name(USER_AUTHORIZED).type(GraphQLBoolean).description(USER_AUTHORIZED_ARG_DESCRIPTION).defaultValueLiteral(BooleanValue.of(DEFAULT_USER_AUTHORIZED_VALUE)))
                .build();

        GraphQLObjectType deckPlanPageType = createPageType(OUTPUT_TYPE_DECK_PLAN_PAGE, deckPlanObjectType);
        GraphQLObjectType organisationPageType = createPageType(OUTPUT_TYPE_ORGANISATION_PAGE, organisationType);

        GraphQLObjectType vehicleRegistryQuery = newObject()
                .name(VEHICLE_REGISTER)
                .description("Query and search for data")
                .field(newFieldDefinition()
                        .name(USER_PERMISSIONS)
                        .description("User permissions")
                        .type(userPermissionsObjectType)
                        .build())
                .field(newFieldDefinition()
                        .name(VEHICLE_TYPES)
                        .type(vehicleTypePageType)
                        .description("Paged vehicle types with optional filtering")
                        .argument(GraphQLArgument.newArgument().name(FILTER).type(vehicleTypeFilterInput))
                        .arguments(createPageAndSizeArguments())
                )
                .field(newFieldDefinition()
                        .name(DECK_PLANS)
                        .type(deckPlanPageType)
                        .description("Paged deck plans")
                        .arguments(createPageAndSizeArguments())
                )
                .field(newFieldDefinition()
                        .name(ORGANISATIONS)
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

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_DECK_PLAN, ID, getNetexIdFetcher());

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, KEY_VALUES, keyValuesDataFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, VEHICLE_TYPES, vehicleTypeFetcher);
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, ID, getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, VEHICLE_TYPE_DECK_PLAN, vehicleTypeDeckPlanFetcher);
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, CHANGED_BY, getChangedByFetcher(authorizationService));

        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, USER_PERMISSIONS, userPermissionsFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, DECK_PLANS, deckPlanFetcher);
        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, ORGANISATIONS, organisationFetcher);

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
