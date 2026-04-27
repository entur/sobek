package org.rutebanken.sobek.rest.netex;

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.SobekTestApplication;
import org.rutebanken.sobek.netex.util.PublicationDeliveryUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
public class VehicleTypeValidationTest {
    @Autowired
    private PublicationDeliveryUnmarshaller unmarshaller;

    @Test
    void importVehicleTypeBasic_returnsImportedVehicleTypeWithAssignedId() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/vehicle-type-import-basic.xml")) {
            unmarshaller.unmarshal(in);
        }
    }
}
