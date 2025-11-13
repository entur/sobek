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

package org.rutebanken.sobek.rest.dto;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.config.JerseyConfig.SERVICES_ADMIN_PATH;
import static org.rutebanken.sobek.config.JerseyConfig.SERVICES_VEHICLES_PATH;

public class DtoResourceIntegrationTest extends SobekIntegrationTest {

    private static final String PATH_MAPPING_VEHICLE = SERVICES_VEHICLES_PATH + "/mapping/vehicle";
    private static final String PATH_MAPPING_VEHICLE_TYPE = SERVICES_VEHICLES_PATH + "/mapping/vehicle_type";
    private static final String PATH_MAPPING_JBV_CODE = SERVICES_ADMIN_PATH + "/jbv_code_mapping";

    @Autowired
    VehicleVersionedSaverService saverService;

    @Before
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void testQuayIdMapping() {

        Vehicle vehicle = new Vehicle();

        String spOrigId = "TST:555";
        vehicle.getOriginalIds().add(spOrigId);

        VehicleType vt1 = new VehicleType();
        String originalId = "TST:123";
        String originalId2 = "TST:1234";
        vt1.getOriginalIds().add(originalId);
        vt1.getOriginalIds().add(originalId2);

        vehicle.setTransportType(vt1);

        vehicle = saverService.saveNewVersion(vehicle);
        Vehicle newVersion = versionCreator.createCopy(vehicle, Vehicle.class);
        String originalId3 = "TST:12345";
        newVersion.getTransportType().getOriginalIds().add(originalId3);

        saverService.saveNewVersion(vehicle);

        String responseWithoutStopPlaceType = getIdMapping(PATH_MAPPING_VEHICLE_TYPE);

        assertThat(responseWithoutStopPlaceType).contains(originalId + "," + vt1.getNetexId() + "\n");
        assertThat(responseWithoutStopPlaceType).contains(originalId2 + "," + vt1.getNetexId() + "\n");
        assertThat(responseWithoutStopPlaceType).contains(originalId3 + "," + vt1.getNetexId() + "\n");
        assertThat(responseWithoutStopPlaceType).doesNotContain(spOrigId);

        String responseWithStopPlaceType = getIdMapping(PATH_MAPPING_VEHICLE_TYPE);
//        assertThat(responseWithStopPlaceType).contains(originalId + "," + StopTypeEnumeration.AIRPORT.value() + "," + q1.getNetexId() + "\n");
    }

    @Test
    public void testStopPlaceIdMapping() {

        Vehicle vehicle = new Vehicle();
        String originalId = "TST:111";
        String originalId2 = "TST:123";
        vehicle.getOriginalIds().add(originalId);
        vehicle.getOriginalIds().add(originalId2);

        VehicleType vt1 = new VehicleType();
        String vtOrigId = "TST:222";
        String vtOrigId2 = "TST:333";

        vt1.getOriginalIds().add(vtOrigId);
        vt1.getOriginalIds().add(vtOrigId2);

        vehicle.setTransportType(vt1);

        vehicle = saverService.saveNewVersion(vehicle);

        String responseWithoutStopPlaceType = getIdMapping(PATH_MAPPING_VEHICLE);

        assertThat(responseWithoutStopPlaceType).contains(originalId + "," + vehicle.getNetexId() + "\n");
        assertThat(responseWithoutStopPlaceType).contains(originalId2 + "," + vehicle.getNetexId() + "\n");
        assertThat(responseWithoutStopPlaceType).doesNotContain(vtOrigId);
        assertThat(responseWithoutStopPlaceType).doesNotContain(vtOrigId2);

        String responseWithStopPlaceType = getIdMapping(PATH_MAPPING_VEHICLE);

        assertThat(responseWithStopPlaceType).contains(originalId + ",," + vehicle.getNetexId() + "\n");
        assertThat(responseWithStopPlaceType).contains(originalId2 + ",," + vehicle.getNetexId() + "\n");
    }


    private String getIdMapping(String url) {
        return given()
                .port(port)
                .get(url)
                .prettyPrint();
    }
}
