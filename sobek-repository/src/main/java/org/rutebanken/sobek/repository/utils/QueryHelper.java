package org.rutebanken.sobek.repository.utils;

public class QueryHelper {
    public static String objectValidCondition(String tableAlias, String parameterName) {
        return String.format("(%s.validBetween.fromDate IS NULL or %s.validBetween.fromDate  <= :%s) " +
            "AND (%s.validBetween.toDate IS NULL OR %s.validBetween.toDate >= :%s)", tableAlias, tableAlias, parameterName, tableAlias, tableAlias, parameterName);
    }
}
