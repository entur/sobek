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

    public GraphQLObjectType create(GraphQLObjectType deckPlanObjectType, GraphQLObjectType vehicleObjectType, GraphQLScalarType dateScalar) {
        return newObject()
                .name(OUTPUT_TYPE_VEHICLE_TYPE)
                .field(newFieldDefinition()
                        .name(ID)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(NAME)
                        .type(embeddableMultilingualStringObjectType))
                .field(newFieldDefinition()
                        .name(SHORT_NAME)
                        .type(embeddableMultilingualStringObjectType))
                .field(newFieldDefinition()
                        .name(TRANSPORT_MODE)
                        .type(transportModeEnumType))
                .field(newFieldDefinition()
                        .name("length")
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name("width")
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name("height")
                        .type(GraphQLFloat))
                .field(newFieldDefinition()
                        .name(VERSION)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(CREATED)
                        .type(dateScalar)
                        .description(DATE_SCALAR_DESCRIPTION))
                .field(newFieldDefinition()
                        .name(CHANGED)
                        .type(dateScalar)
                        .description(DATE_SCALAR_DESCRIPTION))
                .field(newFieldDefinition()
                        .name(CHANGED_BY)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(VERSION_COMMENT)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(VEHICLE_TYPE_DECK_PLAN)
                        .type(deckPlanObjectType))
                .field(newFieldDefinition()
                        .name(VEHICLES)
                        .type(new GraphQLList(vehicleObjectType)))
                .build();
    }

}
