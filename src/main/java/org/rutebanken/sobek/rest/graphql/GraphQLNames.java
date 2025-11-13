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

import org.rutebanken.sobek.rest.graphql.scalars.DateScalar;


import static org.rutebanken.sobek.netex.mapping.mapper.NetexIdMapper.MERGED_ID_KEY;
import static org.rutebanken.sobek.netex.mapping.mapper.NetexIdMapper.ORIGINAL_ID_KEY;
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
    public static final String ENTITY_REF_REF_DESCRIPTION = "The NeTEx ID of the of the referenced entity. The reference must already exist";
    public static final String ENTITY_REF_VERSION = "version";
    public static final String ENTITY_REF_VERSION_DESCRIPTION = "The version of the referenced entity.";

    public static final String OUTPUT_TYPE_VALID_BETWEEN = "ValidBetween";
    public static final String INPUT_TYPE_VALID_BETWEEN = OUTPUT_TYPE_VALID_BETWEEN + INPUT_TYPE_POSTFIX;

    public static final String VALID_BETWEEN_FROM_DATE = "fromDate";
    public static final String VALID_BETWEEN_TO_DATE = "toDate";

    public static final String DATE_SCALAR_DESCRIPTION = "Date time using the format: " + DATE_TIME_PATTERN + ". Example: "+EXAMPLE_DATE_TIME;

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

    public static final String PERMISSIONS = "permissions";
    public static final String ENTITY_PERMISSIONS= "entityPermissions";
    public static final String OUTPUT_TYPE_ENTITY_PERMISSIONS= "EntityPermissions";
    public static final String USER_PERMISSIONS= "userPermissions";
    public static final String OUTPUT_TYPE_USER_PERMISSIONS= "UserPermissions";
    public static final String USER_CONTEXT= "userContext";
    public static final String LOCATION_PERMISSIONS= "locationPermissions";


    public static final String OUTPUT_TYPE_TAG = "Tag";
    public static final String INPUT_TYPE_TAG = "Tag" + INPUT_TYPE_POSTFIX;

    public static final String TAG = "tag";
    public static final String TAG_DESCRIPTION = "A tag for an entity like StopPlace";
    public static final String TAG_ID_REFERENCE = "idReference";
    public static final String TAG_ID_REFERENCE_DESCRIPTION = "A reference to a netex ID. For instance: NSR:StopPlace:1. Types supported: StopPlace";


    public static final String TAGS = "tags";
    public static final String TAGS_DESCRIPTION = "Fetches already used tags by name distinctively";
    public static final String TAGS_ARG_DESCRIPTION = "Only return StopPlaces reffered to by the tag names provided. Values should not start with #";
    public static final String TAG_COMMENT = "comment";
    public static final String TAG_NAME = "name";
    public static final String TAG_NAME_DESCRIPTION = "Tag name";
    public static final String TAG_COMMENT_DESCRIPTION = "A comment for this tag on this entity";
    public static final String TAG_REMOVED_DESCRIPTION = "When this tag was removed. If set, the tag is removed from entity it references in field '" + TAG_ID_REFERENCE + "'";
    public static final String TAG_REMOVED_BY_USER_DESCRIPTION = "Removed by username. Only set if tag has been removed";

    public static final String WITH_TAGS = "withTags";
    public static final String WITH_TAGS_ARG_DESCRIPTION = "If set to true, only stop places with valid tags are returned. If false, filter does not apply.";

    public static final String REMOVE_TAG = "removeTag";
    public static final String CREATE_TAG = "createTag";

    public static final String ID = "id";
    public static final String IDS = "ids";
    public static final String ID_ARG_DESCRIPTION = "IDs used to lookup StopPlace(s). When used - all other searchparameters are ignored.";

    public static final String SHORT_NAME = "shortName";
    public static final String DESCRIPTION = "description";

    public static final String AUDIBLE_SIGNALS_AVAILABLE = "audibleSignalsAvailable";
    public static final String VISUAL_SIGNS_AVAILABLE = "visualSignsAvailable";

    public static final String SANITARY_EQUIPMENT = "sanitaryEquipment";
    public static final String GENERAL_SIGN = "generalSign";

    // SanitaryEquipment
    public static final String NUMBER_OF_TOILETS = "numberOfToilets";
    public static final String GENDER = "gender";

    //GeneralSign
    public static final String NUMBER_OF_SPACES_WITH_RECHARGE_POINT = "numberOfSpacesWithRechargePoint";

    public static final String OUTPUT_TYPE_PRIVATE_CODE = "PrivateCode";
    public static final String PRIVATE_CODE = "privateCode";
    public static final String INPUT_TYPE_PRIVATE_CODE = OUTPUT_TYPE_PRIVATE_CODE + INPUT_TYPE_POSTFIX;

    public static final String CONTENT = "content";
    public static final String SIGN_CONTENT_TYPE = "signContentType";

    public static final String IMPORTED_ID = "importedId";
    public static final String KEY_VALUES = "keyValues";
    public static final String KEY = "key";
    public static final String KEY_ARG_DESCRIPTION = "Must be used together with parameter 'values', other search-parameters are ignored. Defines key to search for.";
    public static final String VALUES = "values";
    public static final String VALUES_ARG_DESCRIPTION = "Must be used together with parameter 'key', other search-parameters are ignored. Defines value to search for.";
    public static final String VERSION = "version";
    public static final String VERSION_COMMENT = "versionComment";
    public static final String MODIFICATION_ENUMERATION = "modificationEnumeration";
    public static final String VERSION_ARG_DESCRIPTION = "Find stop place from " + ID + " and " +  VERSION + ". Only used together with " + ID + " argument";

    public static final String CHANGED_BY = "changedBy";
    public static final String FROM_VERSION_COMMENT = "fromVersionComment";
    public static final String TO_VERSION_COMMENT = "toVersionComment";
    public static final String DRY_RUN = "dryRun";
    public static final String PUBLIC_CODE = "publicCode";
    public static final String WEIGHTING = "weighting";
    public static final String URL = "url";

    public static final String IMPORTED_ID_QUERY = "importedId";
    public static final String IMPORTED_ID_ARG_DESCRIPTION = "Searches for StopPlace by importedId.";
    public static final String COUNTY_REF = "countyReference";
    public static final String COUNTRY_REF = "countryReference";
    public static final String COUNTY_REF_ARG_DESCRIPTION = "Only return StopPlaces located in given counties.";
    public static final String COUNTRY_REF_ARG_DESCRIPTION = "Only return StopPlaces located in given countries.";

    public static final String MUNICIPALITY_REF = "municipalityReference";
    public static final String MUNICIPALITY_REF_ARG_DESCRIPTION = "Only return StopPlaces located in given municipalities.";

    public static final String QUERY = "query";
    public static final String QUERY_ARG_DESCRIPTION = "Searches for StopPlace by name, " + ID + ", " + ORIGINAL_ID_KEY + ", " + MERGED_ID_KEY + " or a single tag prefixed with #";
    public static final String PAGE = "page";
    public static final String PAGE_ARG_DESCRIPTION = "Pagenumber when using pagination - default is " + DEFAULT_PAGE_VALUE;

    public static final String SIZE = "size";
    public static final String SIZE_ARG_DESCRIPTION = "Number of hits per page when using pagination - default is " + DEFAULT_SIZE_VALUE;

    public static final String ALL_VERSIONS = "allVersions";
    public static final String ALL_VERSIONS_ARG_DESCRIPTION = "Fetch all versions for entitites in result. Should not be combined with argument versionValidity";

    public static final String VALID_BETWEEN = "validBetween";
    public static final String INCLUDE_EXPIRED = "includeExpired";

    public static final String SEARCH_WITH_CODE_SPACE = "code";
    public static final String SEARCH_WITH_CODE_SPACE_ARG_DESCRIPTION = "Filter results by data producer code space - i.e. code from original imported ID";

    public static final String NAME = "name";
    public static final String NAME_TYPE = "nameType";
    public static final String ALTERNATIVE_NAMES = "alternativeNames";
    public static final String VALUE = "value";
    public static final String LANG = "lang";

    public static final String TOTAL_CAPACITY = "totalCapacity";

    public static final String VERSION_VALIDITY_ARG = "versionValidity";
    public static final String VERSION_VALIDITY_ARG_DESCRIPTION = "Controls returned stop places based on time. " +
            "Only return stop places wich are valid currently, currently and in the future or just all versions. Default value: CURRENT";

    public static final String POINT_IN_TIME = "pointInTime";
    public static final String POINT_IN_TIME_ARG_DESCRIPTION = "Sets the point in time to use in search. Only StopPlaces " +
            "valid on the given timestamp will be returned. " +
            "If no value is provided, the search will fall back "+ VERSION_VALIDITY_ARG +"'s default value. Cannot be combined with " + VERSION_VALIDITY_ARG +
            " Date format: "+ DateScalar.DATE_TIME_PATTERN;

    public static final String VEHICLE_REGISTER = "VehicleRegister";
    
    public static final String VEHICLE_TYPES = "vehicleTypes";
    public static final String OUTPUT_TYPE_VEHICLE_TYPE = "VehicleType";
    public static final String VEHICLE_TYPE_DECK_PLAN = "deckPlan";

    public static final String VEHICLES = "vehicles";
    public static final String OUTPUT_TYPE_VEHICLE = "Vehicle";
    public static final String OUTPUT_TYPE_DECK_PLAN = "DeckPlan";
}
