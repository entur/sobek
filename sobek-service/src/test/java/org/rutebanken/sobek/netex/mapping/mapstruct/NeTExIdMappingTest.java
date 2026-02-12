package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NeTExIdMappingTest {

    @Autowired
    EntityStructureMapper mapper;

    @Test
    public void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    @Test
    public void testIdSobekToNetex() {
        org.rutebanken.sobek.model.vehicle.Vehicle vehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();

        vehicle.setNetexId("AKT:Vehicle:1");

        var netexVehicle = new Vehicle();
        mapper.mapSobekToNetex(vehicle, netexVehicle);

        assertNotNull(netexVehicle.getId());
        assertEquals("AKT:Vehicle:1", netexVehicle.getId());
        assertInstanceOf(Vehicle.class, netexVehicle);
    }

    @Test
    public void testIdNetexToSobek() {
        org.rutebanken.netex.model.Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("AKT:Vehicle:1");
        var vehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        mapper.mapNetexToSobek(netexVehicle, vehicle);

        assertNotNull(vehicle.getNetexId());
        assertEquals("AKT:Vehicle:1", vehicle.getNetexId());
        assertNull(vehicle.getId());
        assertInstanceOf(org.rutebanken.sobek.model.vehicle.Vehicle.class, vehicle);
    }
}
