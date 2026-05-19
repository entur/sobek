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

import graphql.schema.GraphQLObjectType;
import org.springframework.stereotype.Component;

import static graphql.Scalars.GraphQLInt;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;

@Component
public class PassengerCapacityObjectTypeCreator {

    public GraphQLObjectType create() {
        return newObject()
                .name(OUTPUT_TYPE_PASSENGER_CAPACITY)
                .field(newFieldDefinition()
                        .name(PROPERTY_SEATING_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_STANDING_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_TOTAL_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_SPECIAL_PLACE_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_PUSHCHAIR_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_WHEELCHAIR_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_PRAM_PLACE_CAPACITY)
                        .type(GraphQLInt))
                .field(newFieldDefinition()
                        .name(PROPERTY_BICYCLE_RACK_CAPACITY)
                        .type(GraphQLInt))
                .build();
    }
}
