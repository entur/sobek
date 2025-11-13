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

package org.rutebanken.sobek.repository.reference;

import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceResolverTest extends SobekIntegrationTest {

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Test
    public void testResolveVehicle() {

        Vehicle vehicle = vehicleRepository.save(new Vehicle());

        Vehicle actual = referenceResolver.resolve(new VersionOfObjectRefStructure(vehicle));

        assertThat(actual.getNetexId()).isEqualTo(vehicle.getNetexId());
        assertThat(actual.getVersion()).isEqualTo(vehicle.getVersion());
    }

    @Test
    public void testResolveVehicleType() {

        VehicleType vehicleType = vehicleTypeRepository.save(new VehicleType());

        VehicleType actual = referenceResolver.resolve(new VersionOfObjectRefStructure(vehicleType));

        assertThat(actual.getNetexId()).isEqualTo(vehicleType.getNetexId());
        assertThat(actual.getVersion()).isEqualTo(vehicleType.getVersion());
    }
}