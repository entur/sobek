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

import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_PAGE_VALUE;
import static org.rutebanken.sobek.rest.graphql.RegisterGraphQLSchema.DEFAULT_SIZE_VALUE;
import static org.rutebanken.sobek.rest.graphql.scalars.DateScalar.DATE_TIME_PATTERN;
import static org.rutebanken.sobek.rest.graphql.scalars.DateScalar.EXAMPLE_DATE_TIME;

public class GraphQLNames {

    private static final String INPUT_TYPE_POSTFIX = "Input";

    public static final String OUTPUT_TYPE_ENTITY_REF = "EntityRef";
    public static final String INPUT_TYPE_ENTITY_REF = OUTPUT_TYPE_ENTITY_REF + INPUT_TYPE_POSTFIX;
    public static final String ENTITY_REF_DESCRIPTION = "A reference to an entity with version";

    public static final String OUTPUT_TYPE_VERSION_LESS_ENTITY_REF = "VersionLessEntityRef";
    public static final String INPUT_TYPE_VERSION_LESS_ENTITY_REF = OUTPUT_TYPE_VERSION_LESS_ENTITY_REF + INPUT_TYPE_POSTFIX;
    public static final String VERSION_LESS_ENTITY_REF_DESCRIPTION = "A reference to an entity without version";

    public static final String ENTITY_REF_REF = "ref";
    public static final String ENTITY_REF_REF_DESCRIPTION = "The NeTEx ID of the referenced entity. The reference must already exist";
    public static final String ENTITY_REF_VERSION = "version";
    public static final String ENTITY_REF_VERSION_DESCRIPTION = "The version of the referenced entity.";

    public static final String DATE_SCALAR_DESCRIPTION = "Date time using the format: " + DATE_TIME_PATTERN + ". Example: " + EXAMPLE_DATE_TIME;

    public static final String OUTPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING = "EmbeddableMultilingualString";
    public static final String INPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING = OUTPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING + INPUT_TYPE_POSTFIX;

    public static final String OUTPUT_TYPE_KEY_VALUES = "KeyValues";
    public static final String INPUT_TYPE_KEY_VALUES = OUTPUT_TYPE_KEY_VALUES + INPUT_TYPE_POSTFIX;

    public static final String CONTENT = "content";

    // Pagination fields
    public static final String TOTAL_ELEMENTS = "totalElements";
    public static final String PAGE = "page";
    public static final String PAGE_ARG_DESCRIPTION = "Page number when using pagination - default is " + DEFAULT_PAGE_VALUE;
    public static final String SIZE = "size";
    public static final String SIZE_ARG_DESCRIPTION = "Number of hits per page when using pagination - default is " + DEFAULT_SIZE_VALUE;

    public static final String VEHICLE_REGISTER = "VehicleRegister";

    // Filter fields
    public static final String FILTER = "filter";
    public static final String FILTER_TRANSPORT_MODES = "transportModes";
    public static final String FILTER_ORGANISATION_TYPE = "organisationType";
    public static final String FILTER_IDS = "ids";

    // Property names
    public static final String PROPERTY_TRANSPORT_MODE = "transportMode";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_CHANGED = "changed";
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_WIDTH = "width";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_TRANSPORT_TYPE = "transportType";
    public static final String PROPERTY_VEHICLES = "vehicles";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_CHANGED_BY = "changedBy";
    public static final String PROPERTY_SHORT_NAME = "shortName";
    public static final String PROPERTY_DESCRIPTION = "description";

    public static final String PROPERTY_KEY_VALUES = "keyValues";
    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_VALUES = "values";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_VERSION_COMMENT = "versionComment";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_LANG = "lang";

    public static final String PROPERTY_VALID_BETWEEN_FROM_DATE = "fromDate";
    public static final String PROPERTY_VALID_BETWEEN_TO_DATE = "toDate";
    public static final String PROPERTY_ID = "id";

    // Root lists in GraphQL lists
    public static final String OUTPUT_TYPE_VEHICLE_TYPE_PAGE = "VehicleTypePage";
    public static final String OUTPUT_TYPE_VEHICLE_PAGE = "VehiclePage";
    public static final String OUTPUT_TYPE_DECK_PLAN_PAGE = "DeckPlanPage";
    public static final String OUTPUT_TYPE_ORGANISATION_PAGE = "OrganisationPage";

    public static final String INPUT_TYPE_VEHICLE_TYPE_FILTER = "VehicleTypeFilter";
    public static final String INPUT_TYPE_VEHICLE_FILTER = "VehicleFilter";
    public static final String INPUT_TYPE_ORGANISATIONS_FILTER = "OrganisationsFilter";

    public static final String LIST_NAME_VEHICLE_TYPES = "vehicleTypes";
    public static final String OUTPUT_TYPE_VEHICLE_TYPE = "VehicleType";
    public static final String OUTPUT_TYPE_VEHICLE_TYPE_VEHICLE = "VehicleTypeVehicle";
    public static final String OUTPUT_TYPE_VEHICLE_TYPE_DECK_PLAN = "deckPlan";

    public static final String LIST_NAME_DECK_PLANS = "deckPlans";
    public static final String OUTPUT_TYPE_DECK_PLAN = "DeckPlan";

    public static final String OUTPUT_TYPE_VEHICLE_VEHICLE_TYPE = "VehicleVehicleType";

    public static final String LIST_NAME_VEHICLES = "vehicles";
    public static final String OUTPUT_TYPE_VEHICLE = "Vehicle";

    public static final String LIST_NAME_ORGANISATIONS = "organisations";
    public static final String OUTPUT_TYPE_ORGANISATION = "Organisation";

    public static final String TYPE_TRANSPORT_MODE = "transportMode";
    public static final String TYPE_ORGANISATION_TYPE = "organisationType";
}
