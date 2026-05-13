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

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;

import java.lang.reflect.Method;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;


public class CustomGraphQLTypes {

    public static GraphQLEnumType transportModeEnumType = createCustomEnumType(TYPE_TRANSPORT_MODE, AllPublicTransportModesEnumeration.class);
    public static GraphQLEnumType organisationTypeEnumType = createCustomEnumType(TYPE_ORGANISATION_TYPE, OrganisationTypeEnumeration.class);


    public static GraphQLEnumType createCustomEnumType(String name, Class c) {

        Object[] enumConstants = c.getEnumConstants();

        GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum().name(name);
        for (Object enumObj : enumConstants) {
            boolean valueWasSetFromValueMethod = false;
            Method[] methods = enumObj.getClass().getMethods();
            for (Method method : methods) {

                if (method.getParameterCount() == 0 && "value".equals(method.getName())) {
                    try {
                        builder.value((String) method.invoke(enumObj), enumObj);
                        valueWasSetFromValueMethod = true;
                    } catch (Exception e) {
                        throw new ExceptionInInitializerError(e);
                    }
                }


            }
            if (!valueWasSetFromValueMethod) {
                builder.value(enumObj.toString());
            }
        }
        return builder.build();
    }

    public static GraphQLObjectType embeddableMultilingualStringObjectType = newObject()
            .name(OUTPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING)
            .field(newFieldDefinition()
                    .name(PROPERTY_VALUE)
                    .type(GraphQLString))
            .field(newFieldDefinition()
                    .name(PROPERTY_LANG)
                    .type(GraphQLString))
            .build();


    public static GraphQLInputObjectType embeddableMultiLingualStringInputObjectType = GraphQLInputObjectType.newInputObject()
            .name(INPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING)
            .field(newInputObjectField()
                    .name(PROPERTY_VALUE)
                    .type(GraphQLString))
            .field(newInputObjectField()
                    .name(PROPERTY_LANG)
                    .type(GraphQLString))
            .build();

    public static GraphQLObjectType keyValuesObjectType = newObject()
            .name(OUTPUT_TYPE_KEY_VALUES)
            .field(newFieldDefinition()
                    .name(PROPERTY_KEY)
                    .type(GraphQLString))
            .field(newFieldDefinition()
                    .name(PROPERTY_VALUES)
                    .type(new GraphQLList(GraphQLString)))
            .build();


    public static GraphQLInputObjectType keyValuesObjectInputType = GraphQLInputObjectType.newInputObject()
            .name(INPUT_TYPE_KEY_VALUES)
            .field(newInputObjectField()
                    .name(PROPERTY_KEY)
                    .type(GraphQLString))
            .field(newInputObjectField()
                    .name(PROPERTY_VALUES)
                    .type(new GraphQLList(GraphQLString)))
            .build();

    public static GraphQLFieldDefinition netexIdFieldDefinition = newFieldDefinition()
            .name(PROPERTY_ID)
            .type(GraphQLString)
            .build();


    public static GraphQLInputObjectType refInputObjectType = GraphQLInputObjectType.newInputObject()
            .name(INPUT_TYPE_ENTITY_REF)
            .description(ENTITY_REF_DESCRIPTION)
            .field(newInputObjectField()
                    .name(ENTITY_REF_REF)
                    .type(new GraphQLNonNull(GraphQLString))
                    .description(ENTITY_REF_REF_DESCRIPTION))
            .field(newInputObjectField()
                    .name(ENTITY_REF_VERSION)
                    .type(GraphQLString)
                    .description(ENTITY_REF_VERSION_DESCRIPTION))
            .build();

    /**
     * Versionless refInputObjectType
     */
    public static GraphQLInputObjectType versionLessRefInputObjectType = GraphQLInputObjectType.newInputObject()
            .name(INPUT_TYPE_VERSION_LESS_ENTITY_REF)
            .description(VERSION_LESS_ENTITY_REF_DESCRIPTION)
            .field(newInputObjectField()
                    .name(ENTITY_REF_REF)
                    .type(new GraphQLNonNull(GraphQLString))
                    .description(ENTITY_REF_REF_DESCRIPTION))
            .build();


}
