package org.rutebanken.sobek.repository.utils;

public class QueryHelper {
    public static String objectValidCondition(String tableAlias, String parameterName) {
        return String.format("%s.validBetween.fromDate  <= :%s " +
            "AND (%s.validBetween.toDate IS NULL OR %s.validBetween.toDate >= :%s)", tableAlias, parameterName, tableAlias, tableAlias, parameterName);
    }

    /**
     * True for a fleet (the alias) that is shared with the organisation in the parameter: with everyone, or with that
     * organisation by name. Does not look at who owns the fleet, or at whether it is in use.
     */
    public static String sharedWithCondition(String fleetAlias, String organisationParameter) {
        return String.format("(%s.sharedWithAll = true OR :%s MEMBER OF %s.sharedWith)", fleetAlias, organisationParameter, fleetAlias);
    }

    /**
     * True for a row (the alias, a vehicle or a vehicle type) that is in a fleet in use now that is shared with the
     * organisation in the parameter. The collection is the one of the fleet that holds the row: members or transportTypes.
     */
    public static String inSharedFleetCondition(String rowAlias, String fleetCollection, String organisationParameter, String nowParameter) {
        return String.format("EXISTS (SELECT 1 FROM Fleet sf JOIN sf.%s sm WHERE sm.id = %s.id AND %s AND %s)",
                fleetCollection, rowAlias, objectValidCondition("sf", nowParameter), sharedWithCondition("sf", organisationParameter));
    }

    /**
     * Escapes special LIKE wildcard characters to treat them as literals
     */
    public static String escapeForLike(String value) {
        return value
                .replace("\\", "\\\\")  // Escape the escape character first
                .replace("%", "\\%")     // Escape percent wildcard
                .replace("_", "\\_");    // Escape underscore wildcard
    }


}
