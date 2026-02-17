package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.VehicleType;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import org.rutebanken.sobek.model.Value;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.rutebanken.sobek.model.CustomKeyValueTypes.ORIGINAL_ID_KEY;
import static org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper.CHANGED_BY;
import static org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper.VERSION_COMMENT;

/**
 * Unit tests for DataManagedObjectStructureMapper.
 * Tests the mapping between NeTEx DataManagedObjectStructure and Sobek DataManagedObjectStructure entities.
 */
@SpringBootTest()
public class DataManagedObjectStructureMapperTest {

    @Autowired
    private DataManagedObjectStructureMapper mapper;
    @Autowired
    private NetexIdHelper netexIdHelper;
    @Autowired
    private ValidPrefixList validPrefixList;

    private PublicationDeliveryHelper publicationDeliveryHelper = new PublicationDeliveryHelper();

    private MappingContext context;

    @Autowired
    KeyListStructureMapper keyListStructureMapper;

    @BeforeEach
    void setUp() {
        context = new MappingContext();
        context.setKeyListStructureMapper(keyListStructureMapper);
        context.setNetexIdHelper(netexIdHelper);
        context.setValidPrefixList(validPrefixList);
    }

    @Test
    void testMapToSobek_BasicProperties() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:123");
        netexEntity.setVersion("1");

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getNetexId()).isEqualTo("NMR:VehicleType:123");
        assertThat(sobekEntity.getVersion()).isEqualTo(1L);
    }

    @Test
    void testMapToSobek_WithVersionAny() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:123");
        netexEntity.setVersion("any");

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getVersion()).isEqualTo(-1L);
    }

    @Test
    void testMapToSobek_WithChangedBy() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:123");
        netexEntity.setVersion("1");

        KeyListStructure keyList = new KeyListStructure();
        keyList.withKeyValue(
                new KeyValueStructure()
                        .withKey(DataManagedObjectStructureMapper.CHANGED_BY)
                        .withValue("testuser@example.com")
        );
        netexEntity.setKeyList(keyList);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getChangedBy()).isEqualTo("testuser@example.com");
    }

    @Test
    void testMapToSobek_WithVersionComment() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:123");
        netexEntity.setVersion("1");

        KeyListStructure keyList = new KeyListStructure();
        keyList.withKeyValue(
                new KeyValueStructure()
                        .withKey(DataManagedObjectStructureMapper.VERSION_COMMENT)
                        .withValue("Initial version")
        );
        netexEntity.setKeyList(keyList);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getVersionComment()).isEqualTo("Initial version");
    }

    @Test
    void testMapToNetex_BasicProperties() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.setNetexId("NMR:VehicleType:456");
        sobekEntity.setVersion(2L);

        // When
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        mapper.mapToNetex(sobekEntity, netexEntity, context);

        // Then
        assertThat(netexEntity).isNotNull();
        assertThat(netexEntity.getId()).isEqualTo("NMR:VehicleType:456");
        assertThat(netexEntity.getVersion()).isEqualTo("2");
    }

    @Test
    void testMapToNetex_WithVersionComment() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.setNetexId("NMR:VehicleType:456");
        sobekEntity.setVersion(2L);
        sobekEntity.setVersionComment("Updated version");

        // When
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        mapper.mapToNetex(sobekEntity, netexEntity, context);

        // Then
        assertThat(netexEntity).isNotNull();
        assertThat(netexEntity.getKeyList()).isNotNull();
        assertThat(netexEntity.getKeyList().getKeyValue())
                .isNotEmpty()
                .anyMatch(kv ->
                        DataManagedObjectStructureMapper.VERSION_COMMENT.equals(kv.getKey())
                                && "Updated version".equals(kv.getValue())
                );
    }

    @Test
    void testMapToNetex_WithoutVersionComment_EmptyKeyList() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.setNetexId("NMR:VehicleType:456");
        sobekEntity.setVersion(2L);
        sobekEntity.setVersionComment(null);

        // When
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        mapper.mapToNetex(sobekEntity, netexEntity, context);

        // Then
        assertThat(netexEntity).isNotNull();
        // KeyList should be null when empty (as per mapper logic)
        assertThat(netexEntity.getKeyList()).isNull();
    }

    @Test
    void testMapToNetex_ChangedByNotIncluded() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.setNetexId("NMR:VehicleType:456");
        sobekEntity.setVersion(2L);
        sobekEntity.setChangedBy("testuser@example.com");

        // When
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        mapper.mapToNetex(sobekEntity, netexEntity, context);

        // Then
        assertThat(netexEntity).isNotNull();
        // CHANGED_BY should NOT be exported (as per mapper configuration)
        if (netexEntity.getKeyList() != null && netexEntity.getKeyList().getKeyValue() != null) {
            assertThat(netexEntity.getKeyList().getKeyValue())
                    .noneMatch(kv -> DataManagedObjectStructureMapper.CHANGED_BY.equals(kv.getKey()));
        }
    }

    @Test
    void testUpdateSobekFromNetex() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:789");
        netexEntity.setVersion("3");

        KeyListStructure keyList = new KeyListStructure();
        keyList.withKeyValue(
                new KeyValueStructure()
                        .withKey(DataManagedObjectStructureMapper.VERSION_COMMENT)
                        .withValue("Updated comment")
        );
        netexEntity.setKeyList(keyList);

        org.rutebanken.sobek.model.vehicle.VehicleType existingSobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        existingSobekEntity.setNetexId("NMR:VehicleType:789");
        existingSobekEntity.setVersion(2L);
        existingSobekEntity.setVersionComment("Old comment");

        // When
        mapper.updateSobekFromNetex(netexEntity, existingSobekEntity, context);

        // Then
        assertThat(existingSobekEntity.getNetexId()).isEqualTo("NMR:VehicleType:789");
        assertThat(existingSobekEntity.getVersion()).isEqualTo(3L);
        assertThat(existingSobekEntity.getVersionComment()).isEqualTo("Updated comment");
    }

    @Test
    void testMapToSobek_WithMultipleKeyValues() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:999");
        netexEntity.setVersion("1");

        KeyListStructure keyList = new KeyListStructure();
        keyList.withKeyValue(
                new KeyValueStructure()
                        .withKey(DataManagedObjectStructureMapper.CHANGED_BY)
                        .withValue("user1@example.com")
        ).withKeyValue(
                new KeyValueStructure()
                        .withKey(DataManagedObjectStructureMapper.VERSION_COMMENT)
                        .withValue("Test comment")
        );
        netexEntity.setKeyList(keyList);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getChangedBy()).isEqualTo("user1@example.com");
        assertThat(sobekEntity.getVersionComment()).isEqualTo("Test comment");
    }

    @Test
    void testMapToSobek_NullKeyList() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("NMR:VehicleType:111");
        netexEntity.setVersion("1");
        netexEntity.setKeyList(null);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getNetexId()).isEqualTo("NMR:VehicleType:111");
    }

    @Test
    void testMapToSobek_NullKeyList_InvalidPrefix() {
        // Given
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        netexEntity.setId("BS:VehicleType:111");
        netexEntity.setVersion("1");
        netexEntity.setKeyList(null);

        // When
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        mapper.mapToSobek(netexEntity, sobekEntity, context);

        // Then
        assertThat(sobekEntity).isNotNull();
        assertThat(sobekEntity.getNetexId()).isNull();
        assertThat(sobekEntity.getKeyValues()).containsKey(ORIGINAL_ID_KEY);
        assertEquals("BS:VehicleType:111", sobekEntity.getKeyValues().get(ORIGINAL_ID_KEY).getItems().stream().findFirst().orElse(null));
    }

    @Test
    void testMapToNetex_WithMultipleProperties() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.setNetexId("NMR:VehicleType:222");
        sobekEntity.setVersion(5L);
        sobekEntity.setVersionComment("Multi-property test");
        sobekEntity.setChangedBy("admin@example.com");

        // When
        org.rutebanken.netex.model.VehicleType netexEntity = new org.rutebanken.netex.model.VehicleType();
        mapper.mapToNetex(sobekEntity, netexEntity, context);

        // Then
        assertThat(netexEntity).isNotNull();
        assertThat(netexEntity.getId()).isEqualTo("NMR:VehicleType:222");
        assertThat(netexEntity.getVersion()).isEqualTo("5");
        assertThat(netexEntity.getKeyList()).isNotNull();
        assertThat(netexEntity.getKeyList().getKeyValue())
                .hasSize(1) // Only VERSION_COMMENT should be present
                .anyMatch(kv ->
                        DataManagedObjectStructureMapper.VERSION_COMMENT.equals(kv.getKey())
                                && "Multi-property test".equals(kv.getValue())
                );
    }

    // Converted from Orika KeyValuesToKeyListConverterTest
    @Test
    public void convertFrom() throws Exception {

        org.rutebanken.sobek.model.DataManagedObjectStructure sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        sobekEntity.getOrCreateValues("key").add("value");

        VehicleType netexEntity = new VehicleType();
        mapper.mapToNetex(sobekEntity, (org.rutebanken.netex.model.DataManagedObjectStructure) netexEntity, context);
        KeyListStructure keyValueStructure = netexEntity.getKeyList();
        assertThat(keyValueStructure.getKeyValue())
                .isNotEmpty()
                .extracting(KeyValueStructure::getKey).contains("key");
        assertThat(keyValueStructure.getKeyValue())
                .extracting(KeyValueStructure::getValue).contains("value");

    }

    /**
     * Expect null to avoid empty keylist in netex xml
     */
    @Test
    public void convertFromEmptyExpectsNull() throws Exception {
        Map<String, Value> keyValues = new HashMap<>();

        org.rutebanken.sobek.model.vehicle.VehicleType sobekEntity = new org.rutebanken.sobek.model.vehicle.VehicleType();
        VehicleType netexEntity = new VehicleType();
        mapper.mapToNetex(sobekEntity, (org.rutebanken.netex.model.DataManagedObjectStructure) netexEntity, context);
        KeyListStructure keyValueStructure = netexEntity.getKeyList();
        assertThat(keyValueStructure).isNull();
    }

    @Test
    public void mappingChangedBToNetex() {

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setChangedBy("me");

        org.rutebanken.netex.model.Vehicle netexVehicle = new org.rutebanken.netex.model.Vehicle();
        mapper.mapToNetex(sobekVehicle, netexVehicle, context);
        assertThat(publicationDeliveryHelper.getValueByKey(netexVehicle, CHANGED_BY)).isNull();
    }

    @Test
    public void mappingChangedByFromNetex() {

        String changedBy = "someone";

        org.rutebanken.netex.model.Vehicle netexVehicle = new org.rutebanken.netex.model.Vehicle();
        netexVehicle.withKeyList(new KeyListStructure()
                .withKeyValue(new KeyValueStructure()
                        .withKey(CHANGED_BY)
                        .withValue(changedBy)));

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();

        mapper.mapToSobek(netexVehicle, sobekVehicle, context);

        assertThat(sobekVehicle.getChangedBy()).isEqualTo(changedBy);
    }

    @Test
    public void mappingVersionCommentToNetex() {

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setVersionComment("good changes");

        org.rutebanken.netex.model.Vehicle netexVehicle = new org.rutebanken.netex.model.Vehicle();
        mapper.mapToNetex(sobekVehicle, netexVehicle, context);

        assertThat(publicationDeliveryHelper.getValueByKey(netexVehicle, VERSION_COMMENT)).isEqualTo(sobekVehicle.getVersionComment());
    }

    @Test
    public void mappingVersionCommentFromNetex() {

        String comment = "some change";

        org.rutebanken.netex.model.Vehicle netexVehicle = new org.rutebanken.netex.model.Vehicle();
        netexVehicle.withKeyList(new KeyListStructure()
                .withKeyValue(new KeyValueStructure()
                        .withKey(VERSION_COMMENT)
                        .withValue(comment)));

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();

        mapper.mapToSobek(netexVehicle, sobekVehicle, context);

        assertThat(sobekVehicle.getVersionComment()).isEqualTo(comment);
    }


    @Test
    public void mappingChangedByToNetex() {

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setChangedBy("me");

        org.rutebanken.netex.model.Vehicle netexVehicle = new org.rutebanken.netex.model.Vehicle();
        mapper.mapToNetex(sobekVehicle, netexVehicle, context);
        assertNull(publicationDeliveryHelper.getValueByKey(netexVehicle, CHANGED_BY));
    }

}