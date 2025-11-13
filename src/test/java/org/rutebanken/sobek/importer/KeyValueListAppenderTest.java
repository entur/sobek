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

package org.rutebanken.sobek.importer;

import org.junit.Test;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.Value;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValueListAppenderTest {
    
    @Test
    public void twoValuesMustNotBeAddedTwice() {
        Vehicle newVehicle = new Vehicle();
        newVehicle.getKeyValues().put("key", new Value("value"));

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.getKeyValues().put("key", new Value("value"));

        boolean changed = new KeyValueListAppender().appendToOriginalId("key", newVehicle, existingVehicle);
        assertThat(changed).isFalse();
        assertThat(existingVehicle.getKeyValues().get("key").getItems()).hasSize(1);
    }


    @Test
    public void addNewValues() {
        Vehicle newVehicle = new Vehicle();
        newVehicle.getKeyValues().put("key", new Value("newValue"));

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.getKeyValues().put("key", new Value("oldValue"));

        boolean changed = new KeyValueListAppender().appendToOriginalId("key", newVehicle, existingVehicle);
        assertThat(changed).isTrue();
        assertThat(existingVehicle.getKeyValues().get("key").getItems()).hasSize(2);
    }

    @Test
    public void keepExistingValues() {
        Vehicle newVehicle = new Vehicle();

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.getKeyValues().put("key", new Value("oldValue"));

        boolean changed = new KeyValueListAppender().appendToOriginalId("key", newVehicle, existingVehicle);
        assertThat(changed).isFalse();
        assertThat(existingVehicle.getKeyValues().get("key").getItems()).hasSize(1);
    }
}