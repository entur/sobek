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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class GaplessIdGeneratorServiceTest extends SobekIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private NetexIdHelper netexIdHelper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Test
    public void verifyNetexIdAssignedToStop() {
        Vehicle vehicle = new Vehicle();
        vehicleRepository.save(vehicle);
        assertThat(vehicle.getNetexId()).isNotNull();
    }

    @Test
    public void explicitIdMustBeInsertedIntoHelperTable() {

        long wantedId = 11L;
        insertVehicleType(wantedId, new VehicleType());

        long actual = selectSingleInsertedId(VehicleType.class.getSimpleName(), wantedId);

        assertThat(actual).describedAs("Expecting to find the ID in the id_generator table").isEqualTo(wantedId);
    }

    private long selectSingleInsertedId(String tableName, long expectedId) {

        Query query = entityManager.createNativeQuery("SELECT id_value FROM id_generator WHERE table_name = '" + tableName + "' AND id_value = '" + expectedId + "'");

        List list = query.getResultList();
        assertThat(list).hasSize(1);
        return  (Long) list.getFirst();
    }

    private VehicleType insertVehicleType(long wantedId, VehicleType vehicleType) {
        String wantedNetexIdId = netexIdHelper.getNetexId("VehicleType", wantedId);
        vehicleType.setNetexId(wantedNetexIdId);
        vehicleTypeRepository.save(vehicleType);
        return vehicleType;
    }

    @Test
    public void generateIdAfterExplicitIDs() throws InterruptedException {

        // Use first 500 IDs
        for (long explicitId = 1; explicitId <= 30; explicitId++) {
            VehicleType vehicleType = new VehicleType();
            vehicleType.setNetexId(netexIdHelper.getNetexId(VehicleType.class.getSimpleName(), explicitId));
            vehicleTypeRepository.save(vehicleType);
            System.out.println("Saved vehicleType: " + vehicleType  .getNetexId());
        }

        VehicleType vehicleType = new VehicleType();
        vehicleTypeRepository.save(vehicleType);
        assertThat(netexIdHelper.extractIdPostfixNumeric(vehicleType.getNetexId())).isEqualTo(31);
    }

    @Test
    public void testIdGeneration() {

        IdGeneratorService idGeneratorService = new IdGeneratorService(entityManagerFactory);
        long actual = idGeneratorService.getNextIdForEntity(Vehicle.class);

        assertThat(actual).as("generated id is last id plus one").isEqualTo(1L);
    }

    /**
     * Was implemented under the supsicion that {@link IdGeneratorService} caused a bug.
     * But it was instead a matter of keeping the attached returned entity from save (in case the entity was merged)
     * See NRP-1171
     */
    @Test
    public void testUpdatingVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(new EmbeddableMultilingualString("test"));
        vehicle = vehicleRepository.save(vehicle);

        assertThat(vehicle.getNetexId()).isNotNull();
        String id = vehicle.getNetexId();

        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(new EmbeddableMultilingualString("vehicleTypeTest"));
        vehicleType = vehicleTypeRepository.save(vehicleType);

        //Add VehicleType, and save Vehicle
        vehicle.setTransportType(vehicleType);
        vehicle = vehicleRepository.save(vehicle);

        VehicleType vehicleType2 = new VehicleType();
        vehicleType2.setName(new EmbeddableMultilingualString("vehicleType2Test"));
        vehicleType2 = vehicleTypeRepository.save(vehicleType2);

        //Add another VehicleType and save Vehicle
        vehicle.setTransportType(vehicleType2);
        vehicle = vehicleRepository.save(vehicle);

        assertEquals(id, vehicle.getNetexId());
    }
}
