package org.rutebanken.sobek.repository.utils;

public class QueryHelper {
    public static String ObjectValid_Condition(String tableAlias, String parameterName) {
        return String.format("%s.validBetween.fromDate <= :%s " +
            "AND (%s.validBetween.toDate IS NULL OR %s.validBetween.toDate >= :%s)", tableAlias, parameterName, tableAlias, tableAlias, parameterName);
    }
}
