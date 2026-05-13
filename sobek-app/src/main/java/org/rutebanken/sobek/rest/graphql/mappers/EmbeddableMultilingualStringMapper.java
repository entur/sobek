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

package org.rutebanken.sobek.rest.graphql.mappers;


import org.rutebanken.sobek.model.EmbeddableMultilingualString;

import java.util.Map;

import static org.rutebanken.sobek.rest.graphql.GraphQLNames.PROPERTY_LANG;
import static org.rutebanken.sobek.rest.graphql.GraphQLNames.PROPERTY_VALUE;

public class EmbeddableMultilingualStringMapper {

    public static EmbeddableMultilingualString getEmbeddableString(Map map) {
        if (map != null) {
            return new EmbeddableMultilingualString((String) map.get(PROPERTY_VALUE), (String) map.get(PROPERTY_LANG));
        }
        return null;
    }

}
