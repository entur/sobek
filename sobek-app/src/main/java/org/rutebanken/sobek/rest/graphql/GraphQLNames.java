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

import static org.rutebanken.sobek.rest.graphql.scalars.DateScalar.*;
import static org.rutebanken.sobek.rest.graphql.scalars.DateTimeScalar.DATE_TIME_PATTERN;
import static org.rutebanken.sobek.rest.graphql.scalars.DateTimeScalar.EXAMPLE_DATE_TIME;


public class GraphQLNames {

    public final static int DEFAULT_PAGE_VALUE = 0;
    public final static int DEFAULT_SIZE_VALUE = 20;

    public static final String DATE_TIME_SCALAR_DESCRIPTION = "Date time using the format: " + DATE_TIME_PATTERN + ". Example: " + EXAMPLE_DATE_TIME;
    public static final String DATE_SCALAR_DESCRIPTION = "Date using the format: " + DATE_PATTERN + ". Example: " + EXAMPLE_DATE;

    public static final String CONTENT = "content";

    // Pagination fields
    public static final String TOTAL_ELEMENTS = "totalElements";
    public static final String PAGE = "page";
    public static final String SIZE = "size";

    // Filter fields
    public static final String FILTER = "filter";
    public static final String FILTER_TRANSPORT_MODES = "transportModes";
    public static final String FILTER_ORGANISATION_TYPE = "organisationType";
    public static final String FILTER_IDS = "netexIds";
    public static final String FILTER_NAME = "name";
}
