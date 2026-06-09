package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.HybridCategoryEnumeration;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VehicleTypeMapperTest {

    @Autowired
    private VehicleTypeMapper mapper;

    @Autowired
    MappingContext context;

    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    @Test
    void testMapToSobek() {
        // Given
        VehicleType netexVehicleType = new VehicleType();
        netexVehicleType.setId("VT:Bus:1");
        netexVehicleType.setVersion("1");
        netexVehicleType.setLowFloor(true);
        netexVehicleType.setLength(new BigDecimal("12.5"));
        netexVehicleType.setWidth(new BigDecimal("2.5"));
        netexVehicleType.setHeight(new BigDecimal("3.2"));
        netexVehicleType.setWeight(new BigDecimal("12000"));
        netexVehicleType.setMaximumVelocity(new BigDecimal("100"));
        netexVehicleType.setEuroClass("Euro6");
        netexVehicleType.setTransportMode(AllPublicTransportModesEnumeration.BUS);
        netexVehicleType.withKeyList(new KeyListStructure().withKeyValue(
                new KeyValueStructure().withKey("FormDragCoefficient").withValue("0.4"),
                new KeyValueStructure().withKey("MaximumEngineEffectKW").withValue("500"),
                new KeyValueStructure().withKey("HybridCategory").withValue("chargeable")
        ));

        // Passenger capacity
        PassengerCapacityStructure capacity = new PassengerCapacityStructure();
        capacity.setSeatingCapacity(BigInteger.valueOf(50));
        capacity.setStandingCapacity(BigInteger.valueOf(30));
        capacity.setTotalCapacity(BigInteger.valueOf(80));
        netexVehicleType.setPassengerCapacity(capacity);

        // Fuel types
        netexVehicleType.withFuelTypes(Arrays.asList(
                FuelTypeEnumeration.DIESEL,
                FuelTypeEnumeration.BATTERY
        ));

        // Propulsion types
        netexVehicleType.withPropulsionTypes(Arrays.asList(
                PropulsionTypeEnumeration.COMBUSTION,
                PropulsionTypeEnumeration.ELECTRIC
        ));

        // Deck plan reference
        DeckPlanRefStructure deckPlanRef = new DeckPlanRefStructure();
        deckPlanRef.setRef("DP:1");
        netexVehicleType.setDeckPlanRef(deckPlanRef);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekVehicleType =
                mapper.mapToSobek(netexVehicleType, context);

        // Then
        assertNotNull(sobekVehicleType);
        assertEquals(1, sobekVehicleType.getVersion());
        assertTrue(sobekVehicleType.getLowFloor());
        assertEquals(new BigDecimal("12.5"), sobekVehicleType.getLength());
        assertEquals(new BigDecimal("2.5"), sobekVehicleType.getWidth());
        assertEquals(new BigDecimal("3.2"), sobekVehicleType.getHeight());
        assertEquals(new BigDecimal("12000"), sobekVehicleType.getWeight());
        assertEquals(new BigDecimal("100"), sobekVehicleType.getMaximumVelocity());
        assertEquals("Euro6", sobekVehicleType.getEuroClass());
        assertEquals(org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration.BUS, sobekVehicleType.getTransportMode());

        // Check passenger capacity
        assertNotNull(sobekVehicleType.getPassengerCapacity());
        assertEquals(BigInteger.valueOf(50), sobekVehicleType.getPassengerCapacity().getSeatingCapacity());
        assertEquals(BigInteger.valueOf(30), sobekVehicleType.getPassengerCapacity().getStandingCapacity());

        // Check fuel types
        assertNotNull(sobekVehicleType.getFuelTypes());
        assertEquals(2, sobekVehicleType.getFuelTypes().size());
        assertTrue(sobekVehicleType.getFuelTypes().contains(org.rutebanken.sobek.model.vehicle.FuelTypeEnumeration.DIESEL));
        assertTrue(sobekVehicleType.getFuelTypes().contains(org.rutebanken.sobek.model.vehicle.FuelTypeEnumeration.BATTERY));

        // Check propulsion types
        assertNotNull(sobekVehicleType.getPropulsionTypes());
        assertEquals(2, sobekVehicleType.getPropulsionTypes().size());

        assertEquals(BigDecimal.valueOf(0.4), sobekVehicleType.getFormDragCoefficient());
        assertNull(sobekVehicleType.getRollResistanceCoefficient());
        assertEquals(BigDecimal.valueOf(500), sobekVehicleType.getMaximumEngineEffectKW());
        assertEquals(HybridCategoryEnumeration.CHARGEABLE, sobekVehicleType.getHybridCategory());

        // Check deck plan ref is stored
        //assertNotNull(sobekVehicleType.getDeckPlanRef());
        //assertEquals("DP:1", sobekVehicleType.getDeckPlanRef().getRef());
    }

    @Test
    void testMapToNetex() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekVehicleType =
                new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekVehicleType.setVersion(1);
        sobekVehicleType.setLowFloor(true);
        sobekVehicleType.setLength(new BigDecimal("12.5"));
        sobekVehicleType.setWidth(new BigDecimal("2.5"));
        sobekVehicleType.setWeight(new BigDecimal("12000"));
        sobekVehicleType.setMaximumVelocity(new BigDecimal("100"));
        sobekVehicleType.setEuroClass("Euro6");
        sobekVehicleType.setFormDragCoefficient(BigDecimal.valueOf(0.4));

        // Set deck plan ref in transient field
        DeckPlanRefStructure deckPlanRef = new DeckPlanRefStructure();
        deckPlanRef.setRef("DP:1");
        //sobekVehicleType.setDeckPlanRef(deckPlanRef);

        // When
        VehicleType netexVehicleType = mapper.mapToNetex(sobekVehicleType, context);

        // Then
        assertNotNull(netexVehicleType);
        assertEquals("1", netexVehicleType.getVersion());
        assertTrue(netexVehicleType.isLowFloor());
        assertEquals(new BigDecimal("12.5"), netexVehicleType.getLength());
        assertEquals(new BigDecimal("2.5"), netexVehicleType.getWidth());
        assertEquals(new BigDecimal("12000"), netexVehicleType.getWeight());
        assertEquals(new BigDecimal("100"), netexVehicleType.getMaximumVelocity());
        assertEquals("Euro6", netexVehicleType.getEuroClass());

        var keyList = netexVehicleType.getKeyList();
        keyList.getKeyValue().stream().filter(kv -> kv.getKey().equals("FormDragCoefficient")).findFirst().ifPresent(kv -> assertEquals("0.4", kv.getValue()));
        keyList.getKeyValue().stream().filter(kv -> kv.getKey().equals("RollResitanceCoefficient")).findFirst().ifPresent(kv -> assertNull(kv.getValue()));

        // Check deck plan ref
        //assertNotNull(netexVehicleType.getDeckPlanRef());
        //assertEquals("DP:1", netexVehicleType.getDeckPlanRef().getRef());
    }

    @Test
    void testBidirectionalMapping() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType originalSobek =
                new org.rutebanken.sobek.model.vehicle.VehicleType();
        originalSobek.setVersion(1);
        originalSobek.setLowFloor(false);
        originalSobek.setHasLiftOrRamp(true);
        originalSobek.setBoardingHeight(new BigDecimal("35.5"));
        originalSobek.setMonitored(true);

        // When - Round trip mapping
        VehicleType netex = mapper.mapToNetex(originalSobek, context);
        org.rutebanken.sobek.model.vehicle.VehicleType backToSobek =
                mapper.mapToSobek(netex, context);

        // Then
        assertEquals(originalSobek.getVersion(), backToSobek.getVersion());
        assertEquals(originalSobek.getLowFloor(), backToSobek.getLowFloor());
        assertEquals(originalSobek.getHasLiftOrRamp(), backToSobek.getHasLiftOrRamp());
        assertEquals(originalSobek.getBoardingHeight(), backToSobek.getBoardingHeight());
        assertEquals(originalSobek.getMonitored(), backToSobek.getMonitored());
    }

    @Test
    void testMapWithPassengerCapacity() {
        // Given
        VehicleType netexVehicleType = new VehicleType();
        netexVehicleType.setVersion("1");

        PassengerCapacityStructure capacity = new PassengerCapacityStructure();
        capacity.setSeatingCapacity(BigInteger.valueOf(40));
        capacity.setStandingCapacity(BigInteger.valueOf(20));
        capacity.setWheelchairPlaceCapacity(BigInteger.valueOf(2));
        netexVehicleType.setPassengerCapacity(capacity);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekVehicleType =
                mapper.mapToSobek(netexVehicleType, context);

        // Then
        assertNotNull(sobekVehicleType.getPassengerCapacity());
        assertEquals(BigInteger.valueOf(40), sobekVehicleType.getPassengerCapacity().getSeatingCapacity());
        assertEquals(BigInteger.valueOf(20), sobekVehicleType.getPassengerCapacity().getStandingCapacity());
        assertEquals(BigInteger.valueOf(2), sobekVehicleType.getPassengerCapacity().getWheelchairPlaceCapacity());
    }

}