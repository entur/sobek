package org.rutebanken.sobek.rest.graphql.helpers;

import org.rutebanken.netex.model.OrganisationTypeEnumeration;
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

    public static OrganisationTypeEnumeration getOrganisationTypeFromFilter(Map<String, Object> filter) {
        if(filter == null) { return null; }
        Object orgArg = filter.get(FILTER_ORGANISATION_TYPE);
        if (orgArg instanceof org.rutebanken.netex.model.OrganisationTypeEnumeration t) {
            return t;
        } else if (orgArg instanceof String s) {
            return OrganisationTypeEnumeration.valueOf(s.toUpperCase());
        }
        return null;
    }
}
