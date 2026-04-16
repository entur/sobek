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

    public static final String OUTPUT_TYPE_VALID_BETWEEN = "ValidBetween";
    public static final String INPUT_TYPE_VALID_BETWEEN = OUTPUT_TYPE_VALID_BETWEEN + INPUT_TYPE_POSTFIX;

    public static final String VALID_BETWEEN_FROM_DATE = "fromDate";
    public static final String VALID_BETWEEN_TO_DATE = "toDate";

    public static final String DATE_SCALAR_DESCRIPTION = "Date time using the format: " + DATE_TIME_PATTERN + ". Example: " + EXAMPLE_DATE_TIME;

    public static final String OUTPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING = "EmbeddableMultilingualString";
    public static final String INPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING = OUTPUT_TYPE_EMBEDDABLE_MULTILINGUAL_STRING + INPUT_TYPE_POSTFIX;

    public static final String OUTPUT_TYPE_ALTERNATIVE_NAME = "AlternativeName";
    public static final String INPUT_TYPE_ALTERNATIVE_NAME = OUTPUT_TYPE_ALTERNATIVE_NAME + INPUT_TYPE_POSTFIX;

    public static final String OUTPUT_TYPE_SANITARY_EQUIPMENT = "SanitaryEquipment";
    public static final String INPUT_TYPE_SANITARY_EQUIPMENT = OUTPUT_TYPE_SANITARY_EQUIPMENT + INPUT_TYPE_POSTFIX;

    public static final String OUTPUT_TYPE_GENERAL_SIGN_EQUIPMENT = "GeneralSign";
    public static final String INPUT_TYPE_GENERAL_SIGN_EQUIPMENT = OUTPUT_TYPE_GENERAL_SIGN_EQUIPMENT + INPUT_TYPE_POSTFIX;

    public static final String OUTPUT_TYPE_KEY_VALUES = "KeyValues";
    public static final String INPUT_TYPE_KEY_VALUES = OUTPUT_TYPE_KEY_VALUES + INPUT_TYPE_POSTFIX;

    public static final String USER_PERMISSIONS = "userPermissions";
    public static final String OUTPUT_TYPE_USER_PERMISSIONS = "UserPermissions";

    public static final String ID = "id";
    public static final String IDS = "ids";

    public static final String SHORT_NAME = "shortName";
    public static final String DESCRIPTION = "description";

    public static final String AUDIBLE_SIGNALS_AVAILABLE = "audibleSignalsAvailable";
    public static final String VISUAL_SIGNS_AVAILABLE = "visualSignsAvailable";

    public static final String SANITARY_EQUIPMENT = "sanitaryEquipment";
    public static final String GENERAL_SIGN = "generalSign";

    public static final String NUMBER_OF_TOILETS = "numberOfToilets";
    public static final String GENDER = "gender";

    public static final String NUMBER_OF_SPACES_WITH_RECHARGE_POINT = "numberOfSpacesWithRechargePoint";

    public static final String OUTPUT_TYPE_PRIVATE_CODE = "PrivateCode";
    public static final String PRIVATE_CODE = "privateCode";
    public static final String INPUT_TYPE_PRIVATE_CODE = OUTPUT_TYPE_PRIVATE_CODE + INPUT_TYPE_POSTFIX;

    public static final String CONTENT = "content";
    public static final String SIGN_CONTENT_TYPE = "signContentType";

    public static final String KEY_VALUES = "keyValues";
    public static final String KEY = "key";
    public static final String VALUES = "values";
    public static final String VERSION = "version";
    public static final String VERSION_COMMENT = "versionComment";
    public static final String CHANGED_BY = "changedBy";
    public static final String PUBLIC_CODE = "publicCode";

    public static final String NAME = "name";
    public static final String NAME_TYPE = "nameType";
    public static final String ALTERNATIVE_NAMES = "alternativeNames";
    public static final String VALUE = "value";
    public static final String LANG = "lang";

    public static final String PAGE = "page";
    public static final String PAGE_ARG_DESCRIPTION = "Page number when using pagination - default is " + DEFAULT_PAGE_VALUE;

    public static final String SIZE = "size";
    public static final String SIZE_ARG_DESCRIPTION = "Number of hits per page when using pagination - default is " + DEFAULT_SIZE_VALUE;

    public static final String ALL_VERSIONS = "allVersions";
    public static final String ALL_VERSIONS_ARG_DESCRIPTION = "Fetch all versions for entities in result";

    public static final String VEHICLE_REGISTER = "VehicleRegister";

    public static final String TRANSPORT_MODE = "transportMode";
    public static final String CREATED = "created";
    public static final String CHANGED = "changed";
    public static final String FILTER = "filter";

    public static final String OUTPUT_TYPE_VEHICLE_TYPE_PAGE = "VehicleTypePage";
    public static final String OUTPUT_TYPE_DECK_PLAN_PAGE = "DeckPlanPage";
    public static final String TOTAL_ELEMENTS = "totalElements";
    public static final String INPUT_TYPE_VEHICLE_TYPE_FILTER = "VehicleTypeFilter";

    public static final String VEHICLE_TYPES = "vehicleTypes";
    public static final String OUTPUT_TYPE_VEHICLE_TYPE = "VehicleType";
    public static final String VEHICLE_TYPE_DECK_PLAN = "deckPlan";

    public static final String DECK_PLANS = "deckPlans";
    public static final String OUTPUT_TYPE_DECK_PLAN = "DeckPlan";

    public static final String LENGTH = "length";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    public static final String VEHICLES = "vehicles";
    public static final String OUTPUT_TYPE_VEHICLE = "Vehicle";
}
