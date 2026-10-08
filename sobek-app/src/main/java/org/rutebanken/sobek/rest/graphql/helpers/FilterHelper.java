package org.rutebanken.sobek.rest.graphql.helpers;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.*;

public class FilterHelper {

    public static List<String> getNetexIdsFromFilter(Map<String, Object> filter) {
        if(filter == null) { return null; }
        Object filterIds = filter.get(FILTER_IDS);
        if (filterIds instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> castedFilterIds = (List<String>) filterIds;
            return castedFilterIds;
        }
        return null;
    }

    public static List<AllPublicTransportModesEnumeration> getModesFromFilter(Map<String, Object> filter) {
        if(filter == null) { return null; }
        Object modesObj = filter.get(FILTER_TRANSPORT_MODES);
        if (modesObj instanceof List) {
            List<?> modesList = (List<?>) modesObj;
            return modesList.stream()
                    .filter(obj -> obj != null)
                    .map(obj -> {
                        if (obj instanceof AllPublicTransportModesEnumeration) {
                            return (AllPublicTransportModesEnumeration) obj;
                        } else if (obj instanceof String) {
                            try {
                                return AllPublicTransportModesEnumeration.valueOf(((String) obj).toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Skip invalid enum value and return null
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(obj -> obj != null)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public static String getNameFromFilter(Map<String, Object> filter) {
        if(filter == null) { return null; }
        return (String)filter.get(FILTER_NAME);
    }

    public static String getValueTypeFromFilter(Map<String, Object> filter) {
        if(filter == null) { return null; }
        return (String)filter.get(FILTER_VALUE_TYPE);
    }

    public static String getDataOwnerRefFromFilter(Map<String, Object> filter) {
        if (filter == null || filter.get(FILTER_DATA_OWNER_REF) == null) {
            throw new IllegalArgumentException("Missing required filter field '" + FILTER_DATA_OWNER_REF + "'");
        }
        return (String) filter.get(FILTER_DATA_OWNER_REF);
    }

    public static List<String> getAuthorizedNetexIdsFilter(Map<String, Object> filter, AuthorizationService authorizationService) {
        if(filter == null) { return null; }
        Boolean onlyAuthorized = (Boolean)filter.get(FILTER_ONLY_USER_AUTHORIZED);
        if(onlyAuthorized != null && onlyAuthorized) {
            return authorizationService.getOrganisationRefsUserIsAuthorizedFor();
        }
        return null;
    }
}
