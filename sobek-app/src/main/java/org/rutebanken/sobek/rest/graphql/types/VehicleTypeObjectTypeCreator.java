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

package org.rutebanken.sobek.rest.graphql.types;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;
import static org.rutebanken.sobek.rest.graphql.types.CustomGraphQLTypes.*;

@Component
public class VehicleTypeObjectTypeCreator {

    public GraphQLObjectType create(String typeName, GraphQLObjectType deckPlanObjectType, GraphQLObjectType vehicleObjectType, GraphQLObjectType passengerCapacityObjectType, GraphQLScalarType dateScalar) {
        var builder = newObject()
                .name(typeName)
                .field(newFieldDefinition()
                        .name(PROPERTY_ID)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(PROPERTY_NAME)
                        .type(embeddableMultilingualStringObjectType))
                .field(newFieldDefinition()
                        .name(PROPERTY_SHORT_NAME)
                        .type(embeddableMultilingualStringObjectType))
                .field(newFieldDefinition()
                        .name(PROPERTY_DESCRIPTION)
                        .type(embeddableMultilingualStringObjectType))
                .field(newFieldDefinition()
                        .name(PROPERTY_TRANSPORT_MODE)
                        .type(transportModeEnumType))
                .field(newFieldDefinition()
                        .name(PROPERTY_LENGTH)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_WIDTH)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_HEIGHT)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_WEIGHT)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_VERSION)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_CREATED)
                        .type(dateScalar)
                        .description(DATE_SCALAR_DESCRIPTION))
                .field(newFieldDefinition()
                        .name(PROPERTY_CHANGED)
                        .type(dateScalar)
                        .description(DATE_SCALAR_DESCRIPTION))
                .field(newFieldDefinition()
                        .name(PROPERTY_CHANGED_BY)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(PROPERTY_PRIVATE_CODES)
                        .type(new GraphQLList(GraphQLString)))
                .field(newFieldDefinition()
                        .name(PROPERTY_EURO_CLASS)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(PROPERTY_SELF_PROPELLED)
                        .type(GraphQLBoolean))
                .field(newFieldDefinition()
                        .name(PROPERTY_LOW_FLOOR)
                        .type(GraphQLBoolean))
                .field(newFieldDefinition()
                        .name(PROPERTY_FORM_DRAG_COEFFICIENT)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_ROLL_RESISTANCE_COEFFICIENT)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_MAXIMUM_ENGINE_EFFECT_KW)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_MAXIMUM_VELOCITY)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_MAXIMUM_RANGE)
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(PROPERTY_HYBRID_CATEGORY)
                        .type(hybridCategoryEnumType))
                .field(newFieldDefinition()
                        .name(PROPERTY_PROPULSION_TYPES)
                        .type(new GraphQLList(propulsionTypeEnumType)))
                .field(newFieldDefinition()
                        .name(PROPERTY_FUEL_TYPES)
                        .type(new GraphQLList(fuelTypeEnumType)))
                .field(newFieldDefinition()
                        .name(PROPERTY_PASSENGER_CAPACITY)
                        .type(passengerCapacityObjectType));
                // TODO #85: includedIn is @Transient — needs Flyway migration to add
                // self-referencing FK (parent VehicleType), then persist via JPA @ManyToOne,
                // update MapStruct mappers, and wire a custom DataFetcher here.
                // NeTEx: IncludedIn is a child-to-parent ref for VehicleType hierarchies.
        if(deckPlanObjectType != null) {
            builder.field(newFieldDefinition()
                    .name(OUTPUT_TYPE_VEHICLE_TYPE_DECK_PLAN)
                    .type(deckPlanObjectType));
        }
        if(vehicleObjectType != null) {
                builder.field(newFieldDefinition()
                    .name(PROPERTY_VEHICLES)
                    .type(new GraphQLList(vehicleObjectType)));
        }
        return builder.build();
    }

}
