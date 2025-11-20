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

import org.junit.Ignore;
import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.netex.id.RandomizedTestNetexIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class VersionCreatorTest extends SobekIntegrationTest {

    @Autowired
    private VersionCreator versionCreator;

    @Autowired
    private RandomizedTestNetexIdGenerator randomizedTestNetexIdGenerator;

    @Autowired
    private org.rutebanken.sobek.repository.VehicleRepository vehicleRepository;

    @Test
    @Ignore("Versioning in Sobek needs a review")
    public void versionCommentShouldNotBeCopied() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVersion(1L);
        vehicle.setVersionComment("stopp flyttet 100 meter nordover");
        vehicle = vehicleRepository.save(vehicle);

        Vehicle newVersion = versionCreator.createCopy(vehicle, Vehicle.class);
        assertThat(newVersion.getVersionComment()).isNull();
    }


    @Test
    @Ignore("Versioning in Sobek needs a review")
    public void changedByShouldNotBeCopied() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Vehicle.setChangedBy("testuser");
        Vehicle = vehicleRepository.save(Vehicle);

        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);
        assertThat(newVersion.getChangedBy()).isNull();
    }

    @Test
    @Ignore("Versioning in Sobek needs a review")
    public void validbetweenShouldNotBeCopied() {
        Vehicle Vehicle = new Vehicle();

        Vehicle.setValidBetween(new ValidBetween(Instant.EPOCH, Instant.now()));

        Vehicle = vehicleRepository.save(Vehicle);

        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);
        assertThat(newVersion.getValidBetween()).isNull();
    }


    @Test
    @Ignore("Versioning in Sobek needs a review")
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

    @Ignore
    @Test
    public void deepCopiedObjectShouldHaveOriginalId() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Vehicle.getOriginalIds().add("original-id");
        Vehicle = vehicleRepository.save(Vehicle);

        Vehicle newVersion = versionCreator.createCopy(Vehicle, Vehicle.class);
        assertThat(newVersion.getOriginalIds()).hasSize(1);
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