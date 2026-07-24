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

package org.rutebanken.sobek.netex.id;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class NetexIdHelperTest {


    private NetexIdHelper netexIdHelper = new NetexIdHelper(new ValidPrefixList("PRE", List.of("RUT", "AVI")));

    @Test
    public void extractIdPostfix() throws Exception {
        long last = netexIdHelper.extractIdPostfixNumeric("NOR:Vehicle:19215 ");
        assertThat(last).isEqualTo(19215L);
    }

    @Test
    public void vehicleIdIsNetexId() {
        assertThat(NetexIdHelper.isNetexId("RUT:Vehicle:313")).isTrue();
    }

    @Test
    public void vehicleTypeIdIsNetexId() {
        assertThat(NetexIdHelper.isNetexId("RUT:VehicleType:313")).isTrue();
    }

    @Test
    public void idWithStringPostfixIsNetexId() {
        assertThat(NetexIdHelper.isNetexId("AVI:Vehicle:OSL")).isTrue();
    }

    @Test
    public void idWithMoreThanThreeColonIsNotNetexId() {
        assertThat(NetexIdHelper.isNetexId("AVI:Vehicle:123:2")).isFalse();
    }

    @Test
    public void idWithLessThanThreeColonIsNotNetexId() {
        assertThat(NetexIdHelper.isNetexId("AVI:Vehicle321")).isFalse();
    }
}