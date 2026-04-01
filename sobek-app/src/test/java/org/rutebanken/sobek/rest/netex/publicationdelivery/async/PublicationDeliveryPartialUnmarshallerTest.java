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

package org.rutebanken.sobek.rest.netex.publicationdelivery.async;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.Vehicle;
import org.rutebanken.netex.model.VehicleType;
import org.rutebanken.sobek.SobekTestApplication;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.rest.netex.publicationdelivery.async.RunnableUnmarshaller.*;

@SpringBootTest(classes = SobekTestApplication.class)
public class PublicationDeliveryPartialUnmarshallerTest {

    @Autowired
    private PublicationDeliveryPartialUnmarshaller publicationDeliveryPartialUnmarshaller;

    public PublicationDeliveryPartialUnmarshallerTest() throws IOException, SAXException {
    }


    @Test
    public void partiallyPublicationDeliveryImport() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/vehicle-type-import-1.xml");

        UnmarshalResult unmarshalResult = publicationDeliveryPartialUnmarshaller.unmarshal(inputStream);

        assertThat(unmarshalResult).isNotNull();

        readAndVerifyVehicleTypes(unmarshalResult, 2);
    }

    @Test
    public void testPartialUnmarshallingPublicationDeliveryFromFile() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/vehicle-type-import.xml");

        UnmarshalResult unmarshalResult = publicationDeliveryPartialUnmarshaller.unmarshal(inputStream);
        assertThat(unmarshalResult).isNotNull();
        readAndVerifyVehicleTypes(unmarshalResult, 1);
    }

    private void readAndVerifyVehicles(UnmarshalResult unmarshalResult, int expectedCount) throws InterruptedException {
        AtomicInteger vehicles = new AtomicInteger();
        new EntityQueueProcessor<Vehicle>(unmarshalResult.getVehiclesQueue(), new AtomicBoolean(false), vehicle -> vehicles.incrementAndGet(), POISON_VEHICLE).run();
        assertThat(vehicles.get()).isEqualTo(expectedCount);
    }

    private void readAndVerifyVehicleTypes(UnmarshalResult unmarshalResult, int expectedCount) throws InterruptedException {
        AtomicInteger vehicleTypes = new AtomicInteger();
        new EntityQueueProcessor<VehicleType>(unmarshalResult.getVehicleTypesQueue(), new AtomicBoolean(false), vt -> vehicleTypes.incrementAndGet(), POISON_VEHICLE_TYPE).run();
        assertThat(vehicleTypes.get()).isEqualTo(expectedCount);
    }
}