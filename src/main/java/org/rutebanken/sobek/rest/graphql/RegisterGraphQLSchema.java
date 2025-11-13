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
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import jakarta.annotation.PostConstruct;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.rutebanken.sobek.rest.graphql.fetchers.*;
import org.rutebanken.sobek.rest.graphql.resolvers.MutableTypeResolver;
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
import static org.rutebanken.sobek.rest.graphql.types.CustomGraphQLTypes.modificationEnumerationType;

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
    DataFetcher vehicleTypeFetcher;
    @Autowired
    private VehicleTypeDeckPlanFetcher vehicleTypeDeckPlanFetcher;


    @Autowired
    DateScalar dateScalar;

    @Autowired
    private KeyValuesDataFetcher keyValuesDataFetcher;

    @Autowired
    private UserPermissionsFetcher userPermissionsFetcher;


    @Autowired
    @PostConstruct
    public void init() {

        /**
         * Common field list for quays, stop places and addressable place.
         */
        List<GraphQLFieldDefinition> commonFieldsList = new ArrayList<>();

        commonFieldsList.add(
                newFieldDefinition()
                        .name(MODIFICATION_ENUMERATION)
                        .type(modificationEnumerationType)
                        .build()
        );

        GraphQLObjectType validBetweenObjectType = createValidBetweenObjectType();
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

        GraphQLObjectType entityPermissionObjectType = newObject()
                .name(OUTPUT_TYPE_ENTITY_PERMISSIONS)
                .field(newFieldDefinition()
                        .name("canEdit")
                        .type(GraphQLBoolean)
                        .build())
                .field(newFieldDefinition()
                        .name("canDelete")
                        .type(GraphQLBoolean)
                        .build())

                .build();

        GraphQLObjectType deckPlanObjectType = deckPlanObjectTypeCreator.create();
        GraphQLObjectType vehicleTypeObjectType = vehicleTypeObjectTypeCreator.create(deckPlanObjectType);

        GraphQLArgument allVersionsArgument = GraphQLArgument.newArgument()
                .name(ALL_VERSIONS)
                .type(GraphQLBoolean)
                .description(ALL_VERSIONS_ARG_DESCRIPTION)
                .build();


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
                        .type(new GraphQLList(vehicleTypeObjectType))
                        .description("Vehicle types")
                )
                .build();

        MutableTypeResolver vehicleTypeResolver = new MutableTypeResolver();

        vehicleRegisterSchema = GraphQLSchema.newSchema()
                .query(vehicleRegistryQuery)
//                .mutation(stopPlaceRegisterMutation)
                .codeRegistry(buildCodeRegistry(vehicleTypeResolver))
                .build();

    }


    public GraphQLCodeRegistry buildCodeRegistry(TypeResolver stopPlaceTypeResolver) {
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_DECK_PLAN, ID, getNetexIdFetcher());

        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, KEY_VALUES, keyValuesDataFetcher);

        registerDataFetcher(codeRegistryBuilder, VEHICLE_REGISTER, VEHICLE_TYPES, vehicleTypeFetcher);
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, ID, getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, OUTPUT_TYPE_VEHICLE_TYPE, VEHICLE_TYPE_DECK_PLAN, vehicleTypeDeckPlanFetcher);


        registerDataFetcher(codeRegistryBuilder,VEHICLE_REGISTER,USER_PERMISSIONS,userPermissionsFetcher);




        //mutation

        return codeRegistryBuilder.build();
    }

    private void mapNetexId(GraphQLCodeRegistry.Builder codeRegistryBuilder, String outputTypeShelterEquipment, String outputTypeSanitaryEquipment, String outputTypeCycleStorageEquipment, String outputTypeGeneralSignEquipment, String outputTypeTicketingEquipment, String outputTypeWaitingRoomEquipment) {
        registerDataFetcher(codeRegistryBuilder, outputTypeShelterEquipment,ID,getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, outputTypeSanitaryEquipment,ID,getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, outputTypeCycleStorageEquipment,ID,getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, outputTypeGeneralSignEquipment,ID,getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, outputTypeTicketingEquipment,ID,getNetexIdFetcher());
        registerDataFetcher(codeRegistryBuilder, outputTypeWaitingRoomEquipment,ID,getNetexIdFetcher());
    }

    private static DataFetcher<Object> getNetexIdFetcher() {
        return env -> {
            if (env.getSource() instanceof IdentifiedEntity identifiedEntity) {
                return identifiedEntity.getNetexId();
            }
            return null;
        };
    }

    private static DataFetcher<Object> getOriginalIdsFetcher(){
        return env -> {
            if(env.getSource() instanceof DataManagedObjectStructure dataManagedObjectStructure){
                return dataManagedObjectStructure.getOriginalIds();
            }
            return null;
        };
    }

    private static DataFetcher<Object> getChangedByFetcher(AuthorizationService authorizationService) {
        return env -> {
            if(env.getSource() instanceof DataManagedObjectStructure dataManagedObjectStructure && !authorizationService.isGuest()){
                return dataManagedObjectStructure.getChangedBy();
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

    private GraphQLObjectType createValidBetweenObjectType() {
        return newObject()
                .name(OUTPUT_TYPE_VALID_BETWEEN)
                .field(newFieldDefinition()
                        .name(VALID_BETWEEN_FROM_DATE)
                        .type(dateScalar.getGraphQLDateScalar())
                        .description(DATE_SCALAR_DESCRIPTION))
                .field(newFieldDefinition()
                        .name(VALID_BETWEEN_TO_DATE)
                        .type(dateScalar.getGraphQLDateScalar())
                        .description(DATE_SCALAR_DESCRIPTION))
                .build();
    }

    private GraphQLInputObjectType createValidBetweenInputObjectType() {
        return newInputObject()
                .name(INPUT_TYPE_VALID_BETWEEN)
                .field(newInputObjectField()
                        .name(VALID_BETWEEN_FROM_DATE)
                        .type(new GraphQLNonNull(dateScalar.getGraphQLDateScalar()))
                        .description("When the new version is valid from"))
                .field(newInputObjectField()
                        .name(VALID_BETWEEN_TO_DATE)
                        .type(dateScalar.getGraphQLDateScalar())
                        .description("When the version is no longer valid"))
                .build();
    }


    private List<GraphQLInputObjectField> createCommonInputFieldList(GraphQLInputObjectType embeddableMultiLingualStringInputObjectType) {

        List<GraphQLInputObjectField> commonInputFieldsList = new ArrayList<>();
        commonInputFieldsList.add(newInputObjectField().name(ID).type(GraphQLString).description("Ignore when creating new").build());
        commonInputFieldsList.add(newInputObjectField().name(NAME).type(embeddableMultiLingualStringInputObjectType).build());
        commonInputFieldsList.add(newInputObjectField().name(SHORT_NAME).type(embeddableMultiLingualStringInputObjectType).build());
        commonInputFieldsList.add(newInputObjectField().name(PUBLIC_CODE).type(GraphQLString).build());
        commonInputFieldsList.add(newInputObjectField().name(DESCRIPTION).type(embeddableMultiLingualStringInputObjectType).build());
        return commonInputFieldsList;
    }


}

