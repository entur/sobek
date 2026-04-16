package org.rutebanken.sobek.repository;/*
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

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.SobekTestApplication;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleTypeMapper;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = SobekTestApplication.class)
public class ReferenceResolverTest  {

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private VehicleVersionedSaverService vehicleVersionedSaverService;

    @Autowired
    private VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;

    @Autowired
    private MappingContext context;

    @Autowired
    VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    VehicleMapper vehicleMapper;


    @Test
    public void testResolveVehicle() {

        org.rutebanken.netex.model.Vehicle input = new org.rutebanken.netex.model.Vehicle().withId("TST:Vehicle:1");
        Vehicle vehicle = vehicleVersionedSaverService.saveNewVersion(vehicleMapper.mapToSobek(input, context));

        Vehicle actual = referenceResolver.resolve(new VersionOfObjectRefStructure(vehicle));

        assertThat(actual.getNetexId()).isEqualTo(vehicle.getNetexId());
        assertThat(actual.getVersion()).isEqualTo(vehicle.getVersion());
    }

    @Test
    public void testResolveVehicleType() {

        org.rutebanken.netex.model.VehicleType input = new org.rutebanken.netex.model.VehicleType().withId("TST:VehicleType:1");

        VehicleType vehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleTypeMapper.mapToSobek(input, context));

        VehicleType actual = referenceResolver.resolve(new VersionOfObjectRefStructure(vehicleType));

        assertThat(actual.getNetexId()).isEqualTo(vehicleType.getNetexId());
        assertThat(actual.getVersion()).isEqualTo(vehicleType.getVersion());
    }
}