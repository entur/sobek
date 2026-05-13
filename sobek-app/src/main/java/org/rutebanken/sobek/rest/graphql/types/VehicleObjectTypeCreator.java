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
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;

@Component
public class VehicleObjectTypeCreator {

    public GraphQLObjectType create(String typeName, GraphQLObjectType vehicleTypeObjectType) {
        var builder = newObject()
                .name(typeName)
                .field(newFieldDefinition()
                        .name(ID)
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name("registrationNumber")
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name("operationalNumber")
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name(VERSION)
                        .type(GraphQLInt));
        if(vehicleTypeObjectType != null) {
                builder.field(newFieldDefinition()
                    .name(VEHICLE_VECHILE_TYPE)
                    .type(vehicleTypeObjectType));

        }
        return builder.build();
    }

}
