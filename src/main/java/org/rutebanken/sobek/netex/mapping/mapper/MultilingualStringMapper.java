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

package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MultilingualStringMapper extends CustomMapper<MultilingualString, EmbeddableMultilingualString> {
    @Override
    public void mapAtoB(MultilingualString netexMLString, EmbeddableMultilingualString sobekMLString, MappingContext context) {
        if(netexMLString == null ||
                netexMLString.getContent() == null ||
                netexMLString.getContent().isEmpty()) {
            return;
        }

        sobekMLString.setLang(netexMLString.getLang());
        sobekMLString.setValue(netexMLString.getContent().get(0).toString());
    }

    @Override
    public void mapBtoA(EmbeddableMultilingualString sobekMLString, MultilingualString netexMLString, MappingContext context) {
        if(sobekMLString == null ||
                sobekMLString.getValue() == null ||
                sobekMLString.getValue().isEmpty()) {
            return;
        }

        netexMLString.withLang(sobekMLString.getLang());
        netexMLString.withContent(sobekMLString.getValue());
    }
}
