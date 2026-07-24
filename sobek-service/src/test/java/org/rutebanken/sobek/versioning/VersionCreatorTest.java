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

package org.rutebanken.sobek.versioning;

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.model.CustomKeyValueTypes.ORIGINAL_ID_KEY;

@SpringBootTest
public class VersionCreatorTest {

    @Autowired
    private VersionCreator versionCreator;

    @Autowired
    private org.rutebanken.sobek.repository.VehicleRepository vehicleRepository;

    @Test
    public void unsavedNewVersionShouldNotHavePrimaryKey() throws NoSuchFieldException, IllegalAccessException {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);

        // Save first version
        Vehicle = vehicleRepository.save(Vehicle);
        vehicleRepository.flush();

        // Create new version
        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);

        Object actualVehicleId = getIdValue(newVersion);
        assertThat(actualVehicleId).isNull();
    }

    @Test
    public void deepCopiedObjectShouldHaveOriginalId() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Vehicle.addKeyValue(ORIGINAL_ID_KEY, "original-id");
        Vehicle = vehicleRepository.save(Vehicle);

        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);
        assertThat(newVersion.getKeyValues().stream().anyMatch(kv -> kv.getKey().equals(ORIGINAL_ID_KEY) && kv.getValue().equals("original-id"))).isTrue();
    }

    private Object getIdValue(IdentifiedEntity entity) throws NoSuchFieldException, IllegalAccessException {
        Field field = IdentifiedEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        return field.get(entity);
    }

    @Test
    public void createNewVersionOfStopWithChangeInstance() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Vehicle.setChanged(Instant.now());
        Vehicle = vehicleRepository.save(Vehicle);
        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);
        assertThat(newVersion.getChanged()).isNotNull();
    }


}