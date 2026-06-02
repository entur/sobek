package org.rutebanken.sobek.repository.utils;

public class QueryHelper {
    public static String objectValidCondition(String tableAlias, String parameterName) {
        return String.format("%s.validBetween.fromDate  <= :%s " +
            "AND (%s.validBetween.toDate IS NULL OR %s.validBetween.toDate >= :%s)", tableAlias, parameterName, tableAlias, tableAlias, parameterName);
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
