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

package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class KeyListStructureMapperTest {

    @Autowired
    private KeyListStructureMapper mapper;

    @Test
    public void mapKeyVal() {
        KeyListStructure keyListStructure = new KeyListStructure()
                .withKeyValue(new KeyValueStructure()
                        .withKey("myKey")
                        .withValue("myValue"));
        var keyValues = mapper.mapToSobek(keyListStructure, mock(MappingContext.class));
        assertThat(keyValues.stream().anyMatch(kv -> kv.getKey().equals("myKey") && kv.getValue().equals("myValue"))).isTrue();
    }

    @Test
    public void mapEmpty() {
        KeyListStructure keyListStructure = new KeyListStructure();
        var keyValues = mapper.mapToSobek(keyListStructure, mock(MappingContext.class));
        assertThat(keyValues).isEmpty();
    }
}