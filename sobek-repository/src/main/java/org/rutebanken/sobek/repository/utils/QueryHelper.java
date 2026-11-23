package org.rutebanken.sobek.repository.utils;

public class QueryHelper {
    public static String objectValidCondition(String tableAlias, String parameterName) {
        return String.format("%s.validBetween.fromDate  <= :%s " +
            "AND (%s.validBetween.toDate IS NULL OR %s.validBetween.toDate >= :%s)", tableAlias, parameterName, tableAlias, tableAlias, parameterName);
    }

    /**
     * True for a fleet (alias f) that is shared with the organisation in the parameter :dataOwnerRef: with everyone, or with
     * that organisation by name. Does not look at who owns the fleet, or at whether it is in use.
     * <p>
     * The conditions below are constants, so that no query is built from anything but literals; the values are bound as parameters.
     */
    public static final String FLEET_SHARED_WITH_ORGANISATION =
            "(f.sharedWithAll = true OR :dataOwnerRef MEMBER OF f.sharedWith)";

    private static final String SHARED_FLEET_IN_USE =
            "sf.validBetween.fromDate <= :now AND (sf.validBetween.toDate IS NULL OR sf.validBetween.toDate >= :now) " +
            "AND (sf.sharedWithAll = true OR :dataOwnerRef MEMBER OF sf.sharedWith)";

    /** True for a vehicle (alias v) that is a member of a fleet in use now that is shared with :dataOwnerRef. Takes :now too. */
    public static final String VEHICLE_IN_SHARED_FLEET =
            "EXISTS (SELECT 1 FROM Fleet sf JOIN sf.members sm WHERE sm.id = v.id AND " + SHARED_FLEET_IN_USE + ")";

    /** True for a vehicle type (alias vt) that is a transport type of a fleet in use now that is shared with :dataOwnerRef. Takes :now too. */
    public static final String VEHICLE_TYPE_IN_SHARED_FLEET =
            "EXISTS (SELECT 1 FROM Fleet sf JOIN sf.transportTypes sm WHERE sm.id = vt.id AND " + SHARED_FLEET_IN_USE + ")";

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
