
package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.EquipmentMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.SeatEquipmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeatEquipmentMapperTest {

    @Autowired
    private SeatEquipmentMapper mapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private MappingContext context;

    @Test
    void testMapperIsInjected() {
        assertThat(mapper).isNotNull();
    }

    @Test
    void testMapToSobek() {
        // Given - NeTEx SeatEquipment
        SeatEquipment netexSeatEquipment = new SeatEquipment()
                .withId("NMR:SeatEquipment:SE001")
                .withVersion("1")
                .withCreated(LocalDateTime.now())
                .withChanged(LocalDateTime.now())
                .withName(new MultilingualString().withContent("Standard Seat"))
                .withDescription(new MultilingualString().withContent("Comfortable standard seat"))
                // SpotEquipment properties
                .withWidth(BigDecimal.valueOf(50.0))
                .withLength(BigDecimal.valueOf(80.0))
                .withHeight(BigDecimal.valueOf(120.0))
                .withHeightFromFloor(BigDecimal.valueOf(45.0))
                .withHasPowerSupply(true)
                .withHasUsbPowerSocket(true)
                // SeatEquipment specific properties
                .withSeatBackHeight(BigDecimal.valueOf(75.0))
                .withSeatDepth(BigDecimal.valueOf(45.0))
                .withIsFoldup(false)
                .withIsReclining(true)
                .withMaximumRecline(BigInteger.valueOf(30))
                .withIsReversible(true)
                .withCanRotate(false);

        // When
        org.rutebanken.sobek.model.vehicle.Equipment sobekEquipment = equipmentMapper.mapToSobekManual(netexSeatEquipment, context);

        assertThat(sobekEquipment).isNotNull();
        assertThat(sobekEquipment).isInstanceOf(org.rutebanken.sobek.model.vehicle.SeatEquipment.class);

        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment =  (org.rutebanken.sobek.model.vehicle.SeatEquipment)sobekEquipment;

        // Then
        assertThat(sobekSeatEquipment).isNotNull();
        assertThat(sobekSeatEquipment.getNetexId()).isEqualTo("NMR:SeatEquipment:SE001");
        assertThat(sobekSeatEquipment.getVersion()).isEqualTo(1L);

        // Verify SpotEquipment properties
        assertThat(sobekSeatEquipment.getWidth()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(sobekSeatEquipment.getLength()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(sobekSeatEquipment.getHeight()).isEqualByComparingTo(BigDecimal.valueOf(120.0));
        assertThat(sobekSeatEquipment.getHeightFromFloor()).isEqualByComparingTo(BigDecimal.valueOf(45.0));
        assertThat(sobekSeatEquipment.getHasPowerSupply()).isTrue();
        assertThat(sobekSeatEquipment.getHasUsbPowerSocket()).isTrue();

        // Verify SeatEquipment specific properties
        assertThat(sobekSeatEquipment.getSeatBackHeight()).isEqualByComparingTo(BigDecimal.valueOf(75.0));
        assertThat(sobekSeatEquipment.getSeatDepth()).isEqualByComparingTo(BigDecimal.valueOf(45.0));
        assertThat(sobekSeatEquipment.getIsFoldup()).isFalse();
        assertThat(sobekSeatEquipment.getIsReclining()).isTrue();
        assertThat(sobekSeatEquipment.getMaximumRecline()).isEqualTo(BigInteger.valueOf(30));
        assertThat(sobekSeatEquipment.getIsReversible()).isTrue();
        assertThat(sobekSeatEquipment.getCanRotate()).isFalse();
    }

    @Test
    void testMapToNetex() {
        // Given - Sobek SeatEquipment
        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment =
                new org.rutebanken.sobek.model.vehicle.SeatEquipment();
        sobekSeatEquipment.setNetexId("NMR:SeatEquipment:SE002");
        sobekSeatEquipment.setVersion(2L);

        // SpotEquipment properties
        sobekSeatEquipment.setWidth(BigDecimal.valueOf(55.0));
        sobekSeatEquipment.setLength(BigDecimal.valueOf(85.0));
        sobekSeatEquipment.setHeight(BigDecimal.valueOf(125.0));
        sobekSeatEquipment.setHeightFromFloor(BigDecimal.valueOf(50.0));
        sobekSeatEquipment.setHasPowerSupply(false);
        sobekSeatEquipment.setHasUsbPowerSocket(true);

        // SeatEquipment specific properties
        sobekSeatEquipment.setSeatBackHeight(BigDecimal.valueOf(80.0));
        sobekSeatEquipment.setSeatDepth(BigDecimal.valueOf(50.0));
        sobekSeatEquipment.setIsFoldup(true);
        sobekSeatEquipment.setIsReclining(false);
        sobekSeatEquipment.setMaximumRecline(BigInteger.valueOf(0));
        sobekSeatEquipment.setIsReversible(false);
        sobekSeatEquipment.setCanRotate(true);

        // When
        Equipment_VersionStructure equipment = equipmentMapper.mapToNetexManual(sobekSeatEquipment, context);
        assertThat(equipment).isNotNull();
        assertThat(equipment).isInstanceOf(SeatEquipment.class);

        SeatEquipment netexSeatEquipment = (SeatEquipment)equipment;

        // Then
        assertThat(netexSeatEquipment).isNotNull();
        assertThat(netexSeatEquipment.getId()).isEqualTo("NMR:SeatEquipment:SE002");
        assertThat(netexSeatEquipment.getVersion()).isEqualTo("2");

        // Verify SpotEquipment properties
        assertThat(netexSeatEquipment.getWidth()).isEqualByComparingTo(BigDecimal.valueOf(55.0));
        assertThat(netexSeatEquipment.getLength()).isEqualByComparingTo(BigDecimal.valueOf(85.0));
        assertThat(netexSeatEquipment.getHeight()).isEqualByComparingTo(BigDecimal.valueOf(125.0));
        assertThat(netexSeatEquipment.getHeightFromFloor()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(netexSeatEquipment.isHasPowerSupply()).isFalse();
        assertThat(netexSeatEquipment.isHasUsbPowerSocket()).isTrue();

        // Verify SeatEquipment specific properties
        assertThat(netexSeatEquipment.getSeatBackHeight()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(netexSeatEquipment.getSeatDepth()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(netexSeatEquipment.isIsFoldup()).isTrue();
        assertThat(netexSeatEquipment.isIsReclining()).isFalse();
        assertThat(netexSeatEquipment.getMaximumRecline()).isEqualTo(BigInteger.valueOf(0));
        assertThat(netexSeatEquipment.isIsReversible()).isFalse();
        assertThat(netexSeatEquipment.isCanRotate()).isTrue();
    }

    @Test
    void testBidirectionalMapping() {
        // Given - NeTEx SeatEquipment
        SeatEquipment originalNetex = new SeatEquipment()
                .withId("NMR:SeatEquipment:SE003")
                .withVersion("1")
                .withWidth(BigDecimal.valueOf(52.0))
                .withLength(BigDecimal.valueOf(82.0))
                .withSeatBackHeight(BigDecimal.valueOf(77.0))
                .withSeatDepth(BigDecimal.valueOf(47.0))
                .withIsReclining(true)
                .withMaximumRecline(BigInteger.valueOf(25))
                .withIsReversible(true);

        // When - Map to Sobek and back to NeTEx
        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment = mapper.mapToSobek(originalNetex, context);

        SeatEquipment mappedBackNetex = mapper.mapToNetex(sobekSeatEquipment, context);

        // Then - Verify bidirectional consistency
        assertThat(mappedBackNetex.getId()).isEqualTo(originalNetex.getId());
        assertThat(mappedBackNetex.getVersion()).isEqualTo(originalNetex.getVersion());
        assertThat(mappedBackNetex.getWidth()).isEqualByComparingTo(originalNetex.getWidth());
        assertThat(mappedBackNetex.getLength()).isEqualByComparingTo(originalNetex.getLength());
        assertThat(mappedBackNetex.getSeatBackHeight()).isEqualByComparingTo(originalNetex.getSeatBackHeight());
        assertThat(mappedBackNetex.getSeatDepth()).isEqualByComparingTo(originalNetex.getSeatDepth());
        assertThat(mappedBackNetex.isIsReclining()).isEqualTo(originalNetex.isIsReclining());
        assertThat(mappedBackNetex.getMaximumRecline()).isEqualTo(originalNetex.getMaximumRecline());
        assertThat(mappedBackNetex.isIsReversible()).isEqualTo(originalNetex.isIsReversible());
    }

    @Test
    void testUpdateSobekFromNetex() {
        // Given - Existing Sobek entity
        org.rutebanken.sobek.model.vehicle.SeatEquipment existingSobek =
                new org.rutebanken.sobek.model.vehicle.SeatEquipment();
        existingSobek.setNetexId("NMR:SeatEquipment:SE004");
        existingSobek.setVersion(1);
        existingSobek.setSeatBackHeight(BigDecimal.valueOf(70.0));
        existingSobek.setIsReclining(false);

        // And - Updated NeTEx data
        SeatEquipment updatedNetex = new SeatEquipment()
                .withId("NMR:SeatEquipment:SE004")
                .withVersion("2")
                .withSeatBackHeight(BigDecimal.valueOf(75.0))
                .withIsReclining(true)
                .withMaximumRecline(BigInteger.valueOf(30));

        // When
        mapper.updateSobekFromNetex(updatedNetex, existingSobek, context);

        // Then - Verify update
        assertThat(existingSobek.getNetexId()).isEqualTo("NMR:SeatEquipment:SE004");
        assertThat(existingSobek.getVersion()).isEqualTo(2L);
        assertThat(existingSobek.getSeatBackHeight()).isEqualByComparingTo(BigDecimal.valueOf(75.0));
        assertThat(existingSobek.getIsReclining()).isTrue();
        assertThat(existingSobek.getMaximumRecline()).isEqualTo(BigInteger.valueOf(30));
    }

    @Test
    void testMapToSobek_withNullValues() {
        // Given - NeTEx SeatEquipment with minimal data
        SeatEquipment netexSeatEquipment = new SeatEquipment()
                .withId("NMR:SeatEquipment:SE005");

        // When
        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment = mapper.mapToSobek(netexSeatEquipment, context);

        // Then
        assertThat(sobekSeatEquipment).isNotNull();
        assertThat(sobekSeatEquipment.getNetexId()).isEqualTo("NMR:SeatEquipment:SE005");
        assertThat(sobekSeatEquipment.getSeatBackHeight()).isNull();
        assertThat(sobekSeatEquipment.getSeatDepth()).isNull();
        assertThat(sobekSeatEquipment.getIsFoldup()).isNull();
        assertThat(sobekSeatEquipment.getIsReclining()).isNull();
    }

    @Test
    void testMapToNetex_withNullValues() {
        // Given - Sobek SeatEquipment with minimal data
        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment =
                new org.rutebanken.sobek.model.vehicle.SeatEquipment();
        sobekSeatEquipment.setNetexId("NMR:SeatEquipment:SE006");

        // When
        SeatEquipment netexSeatEquipment = mapper.mapToNetex(sobekSeatEquipment, context);

        // Then
        assertThat(netexSeatEquipment).isNotNull();
        assertThat(netexSeatEquipment.getId()).isEqualTo("NMR:SeatEquipment:SE006");
        assertThat(netexSeatEquipment.getSeatBackHeight()).isNull();
        assertThat(netexSeatEquipment.getSeatDepth()).isNull();
        assertThat(netexSeatEquipment.isIsFoldup()).isNull();
        assertThat(netexSeatEquipment.isIsReclining()).isNull();
    }

    @Test
    void testMapToSobek_withKeyValues() {
        // Given - NeTEx SeatEquipment with minimal data
        SeatEquipment netexSeatEquipment = new SeatEquipment()
                .withId("NMR:SeatEquipment:SE005");

        netexSeatEquipment.withKeyList(new KeyListStructure()).getKeyList().withKeyValue(new KeyValueStructure().withKey("key").withValue("value"));

        // When
        org.rutebanken.sobek.model.vehicle.SeatEquipment sobekSeatEquipment = mapper.mapToSobek(netexSeatEquipment, context);

        // Then
        assertThat(sobekSeatEquipment).isNotNull();
        assertThat(sobekSeatEquipment.getNetexId()).isEqualTo("NMR:SeatEquipment:SE005");
        assertThat(sobekSeatEquipment.getSeatBackHeight()).isNull();
        assertThat(sobekSeatEquipment.getKeyValues())
                .isNotNull()
                .isNotEmpty()
                .anyMatch(kv -> kv.getKey().equals("key") && kv.getValue().equals("value"));
    }
}