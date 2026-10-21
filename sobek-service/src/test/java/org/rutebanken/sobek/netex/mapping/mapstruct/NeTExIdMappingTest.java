package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.entur.netex2.model.KeyListStructure;
import org.entur.netex2.model.KeyValueStructure;
import org.entur.netex2.model.Vehicle;
import org.rutebanken.sobek.model.ResourceFrame;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.rutebanken.sobek.model.CustomKeyValueTypes.ORIGINAL_ID_KEY;

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
        org.entur.netex2.model.Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("AKT:Vehicle:1");
        var vehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        mapper.mapNetexToSobek(netexVehicle, vehicle);

        assertNotNull(vehicle.getNetexId());
        assertEquals("AKT:Vehicle:1", vehicle.getNetexId());
        assertNull(vehicle.getId());
        assertInstanceOf(org.rutebanken.sobek.model.vehicle.Vehicle.class, vehicle);
    }

    @Test
    public void mapResourceFrameIdToNetex() {
        ResourceFrame resourceFrame = new ResourceFrame();
        resourceFrame.setNetexId("NSR:ResourceFrame:123123");

        org.entur.netex2.model.ResourceFrame netexResourceFrame = new org.entur.netex2.model.ResourceFrame();
        mapper.mapSobekToNetex(resourceFrame, netexResourceFrame);

        assertThat(netexResourceFrame.getId()).isNotEmpty();
        assertThat(netexResourceFrame.getId()).isEqualToIgnoringCase("NSR:ResourceFrame:123123");
    }

    @Test
    public void accessibilityAssesmentIdToNetex() {
        DeckPlan sobek = new DeckPlan();
        sobek.setNetexId("NMR:DeckPlan:123124");

        org.entur.netex2.model.DeckPlan netex = new org.entur.netex2.model.DeckPlan();
        mapper.mapSobekToNetex(sobek, netex);

        assertThat(netex.getId()).isNotEmpty();
        assertThat(netex.getId()).isEqualToIgnoringCase("NMR:DeckPlan:123124");
    }

    // TODO: Is this still needed?
//    @Test
//    public void copyKeyValuesAvoidEmptyOriginalId() throws Exception {
//
//        String originalId = "RUT:Vehicle:1,,RUT:Vehicle:2";
//
//        org.entur.netex2.model.DataManagedObjectStructure netexEntity = new org.entur.netex2.model.Vehicle()
//                .withKeyList(new KeyListStructure()
//                        .withKeyValue(new KeyValueStructure()
//                                .withKey(ORIGINAL_ID_KEY)
//                                .withValue(originalId)));
//
//        org.rutebanken.sobek.model.vehicle.Vehicle vehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
//
//        mapper.mapNetexToSobek(netexEntity, vehicle);
//
//        assertThat(vehicle.getOriginalIds()).hasSize(2);
//    }

    @Test
    public void copyKeyValuesForVehicleTypeEmptyPostfixRemove() {

        String originalId = "RUT:VehicleType:";

        org.entur.netex2.model.DataManagedObjectStructure netexEntity = new org.entur.netex2.model.VehicleType()
                .withKeyList(new KeyListStructure()
                        .withKeyValue(new KeyValueStructure()
                                .withKey(ORIGINAL_ID_KEY)
                                .withValue(originalId)));

        org.rutebanken.sobek.model.vehicle.VehicleType vehicleType = new org.rutebanken.sobek.model.vehicle.VehicleType();

        mapper.mapNetexToSobek(netexEntity, vehicleType);

        assertThat(vehicleType.getKeyValues().stream().filter(kv -> kv.getKey().equals(ORIGINAL_ID_KEY)).findFirst().isEmpty()).isTrue();
    }
}
