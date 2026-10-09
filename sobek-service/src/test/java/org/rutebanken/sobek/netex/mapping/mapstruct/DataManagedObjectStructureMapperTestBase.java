package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.KeyValue;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test class for testing MapStruct mappers that inherit from DataManagedObjectStructureMapper.
 * Tests for double-mapping of keyValues and ensures proper inheritance.
 */
public abstract class DataManagedObjectStructureMapperTestBase<
    NETEX_TYPE extends DataManagedObjectStructure,
    SOBEK_TYPE extends org.rutebanken.sobek.model.DataManagedObjectStructure> {

    private final Class<NETEX_TYPE> netexClass;
    private final Class<SOBEK_TYPE> sobekClass;

    @Autowired
    protected MappingContext context;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;

    protected DataManagedObjectStructureMapperTestBase(Class<NETEX_TYPE> netexClass, Class<SOBEK_TYPE> sobekClass) {
        this.netexClass = netexClass;
        this.sobekClass = sobekClass;
    }

    @BeforeEach
    public void setUpBase() {
        context.setKeyListStructureMapper(keyListStructureMapper);
    }

    // Helper method to create instances
    protected NETEX_TYPE createNetexEntity() throws Exception {
        return netexClass.getDeclaredConstructor().newInstance();
    }

    protected SOBEK_TYPE createSobekEntity() throws Exception {
        return sobekClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Override this to perform the mapping from NeTEx to Sobek
     */
    protected abstract SOBEK_TYPE mapToSobek(NETEX_TYPE source);

    /**
     * Override this to perform the mapping from Sobek to NeTEx
     */
    protected abstract NETEX_TYPE mapToNetex(SOBEK_TYPE source);

    @Test
    @DisplayName("KeyValues should not be duplicated when mapper runs twice - ToSobek")
    public void testKeyValuesNotDuplicatedOnDoubleMapping_ToSobek() throws Exception {
        // Arrange
        NETEX_TYPE netexEntity = createNetexEntity();
        KeyListStructure keyList = new KeyListStructure();
        keyList.getKeyValue().add(createKeyValue("key1", "value1"));
        keyList.getKeyValue().add(createKeyValue("key2", "value2"));
        keyList.getKeyValue().add(createKeyValue("key3", "value3"));
        netexEntity.setKeyList(keyList);

        // Act - Run mapper twice
        SOBEK_TYPE sobekEntity = mapToSobek(netexEntity);

        // Assert
        assertEquals(3, sobekEntity.getKeyValues().size(), "Mapping should map 3 keyValues");

        // Verify each key appears exactly once
        List<String> keys = sobekEntity.getKeyValues().stream()
            .map(KeyValue::getKey)
            .toList();

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));

        // Ensure no duplicates
        assertEquals(keys.size(), keys.stream().distinct().count(),
            "KeyValues should not contain duplicate keys");
    }

    @Test
    @DisplayName("KeyValues should not be duplicated when mapper runs twice - ToNetex")
    public void testKeyValuesNotDuplicatedOnDoubleMapping_ToNetex() throws Exception {
        // Arrange
        SOBEK_TYPE sobekEntity = createSobekEntity();
        sobekEntity.addKeyValue("key1", "value1");
        sobekEntity.addKeyValue("key2", "value2");
        sobekEntity.addKeyValue("key3", "value3");

        // Act - Run mapper twice
        NETEX_TYPE netexEntity = mapToNetex(sobekEntity);
        int kvSize = netexEntity.getKeyList() != null && netexEntity.getKeyList().getKeyValue() != null
            ? netexEntity.getKeyList().getKeyValue().size() : 0;

        // Assert
        assertEquals(3, kvSize, "Mapped object should map 3 keyValues");

        // Verify each key appears exactly once
        List<String> keys = netexEntity.getKeyList().getKeyValue().stream()
            .map(KeyValueStructure::getKey)
            .toList();

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));

        // Ensure no duplicates
        assertEquals(keys.size(), keys.stream().distinct().count(),
            "KeyValues should not contain duplicate keys");
    }

    @Test
    @DisplayName("All keyValues should be mapped correctly - ToSobek")
    public void testAllKeyValuesMappedCorrectly_ToSobek() throws Exception {
        // Arrange
        NETEX_TYPE netexEntity = createNetexEntity();
        KeyListStructure keyList = new KeyListStructure();
        keyList.getKeyValue().add(createKeyValue("originalId", "ABC:123"));
        keyList.getKeyValue().add(createKeyValue("VERSION_COMMENT", "Test comment"));
        keyList.getKeyValue().add(createKeyValue("customKey", "customValue"));
        netexEntity.setKeyList(keyList);

        // Act
        SOBEK_TYPE sobekEntity = mapToSobek(netexEntity);

        // Assert - VERSION_COMMENT should be removed from keyValues and set as property
        assertNotNull(sobekEntity.getVersionComment(), "VersionComment should be set");
        assertEquals("Test comment", sobekEntity.getVersionComment());

        // VERSION_COMMENT should be removed from keyValues
        boolean hasVersionCommentKey = sobekEntity.getKeyValues().stream()
            .anyMatch(kv -> "VERSION_COMMENT".equals(kv.getKey()));
        assertFalse(hasVersionCommentKey, "VERSION_COMMENT should be removed from keyValues");

        // Other keys should still be present
        assertTrue(sobekEntity.getKeyValues().stream()
            .anyMatch(kv -> "originalId".equals(kv.getKey()) && "ABC:123".equals(kv.getValue())));
        assertTrue(sobekEntity.getKeyValues().stream()
            .anyMatch(kv -> "customKey".equals(kv.getKey()) && "customValue".equals(kv.getValue())));
    }

    @Test
    @DisplayName("All keyValues should be mapped correctly - ToNetex")
    public void testAllKeyValuesMappedCorrectly_ToNetex() throws Exception {
        // Arrange
        SOBEK_TYPE sobekEntity = createSobekEntity();
        sobekEntity.addKeyValue("originalId", "ABC:123");
        sobekEntity.addKeyValue("customKey", "customValue");
        sobekEntity.setVersionComment("Test comment");

        // Act
        NETEX_TYPE netexEntity = mapToNetex(sobekEntity);

        // Assert
        assertNotNull(netexEntity.getKeyList(), "KeyList should not be null");
        List<KeyValueStructure> keyValues = netexEntity.getKeyList().getKeyValue();

        // VERSION_COMMENT should be added to keyValues
        assertTrue(keyValues.stream()
                .anyMatch(kv -> "VERSION_COMMENT".equals(kv.getKey()) && "Test comment".equals(kv.getValue())),
            "VERSION_COMMENT should be in keyValues");

        // Other keys should be present
        assertTrue(keyValues.stream()
            .anyMatch(kv -> "originalId".equals(kv.getKey()) && "ABC:123".equals(kv.getValue())));
        assertTrue(keyValues.stream()
            .anyMatch(kv -> "customKey".equals(kv.getKey()) && "customValue".equals(kv.getValue())));
    }

    @Test
    @DisplayName("Empty keyValues should not create empty KeyList in NeTEx")
    public void testEmptyKeyValuesNotCreatingEmptyKeyList() throws Exception {
        // Arrange
        SOBEK_TYPE sobekEntity = createSobekEntity();

        // Act
        NETEX_TYPE netexEntity = mapToNetex(sobekEntity);

        // Assert
        assertNull(netexEntity.getKeyList(), "KeyList should be null when there are no keyValues");
    }

    @Test
    @DisplayName("Inherited properties should be mapped correctly")
    public void testInheritedPropertiesMapped() throws Exception {
        // Arrange
        NETEX_TYPE netexEntity = createNetexEntity();
        netexEntity.setId("TEST:VehicleType:1");
        netexEntity.setVersion("1");

        // Act
        SOBEK_TYPE sobekEntity = mapToSobek(netexEntity);

        // Assert
        // Since the codespace of the ID is "not allowed", the ID should be mapped to "imported-id"
        assertTrue(sobekEntity.getKeyValues().stream()
            .anyMatch(kv -> "imported-id".equals(kv.getKey()) && netexEntity.getId().equals(kv.getValue())));
        assertEquals(1, sobekEntity.getVersion(), "Version should be mapped");
    }

    @Test
    @DisplayName("Clear keyValues should work correctly before remapping")
    public void testClearKeyValuesBeforeRemapping() throws Exception {
        // Arrange
        NETEX_TYPE netexEntity = createNetexEntity();
        KeyListStructure keyList = new KeyListStructure();
        keyList.getKeyValue().add(createKeyValue("key1", "value1"));
        netexEntity.setKeyList(keyList);

        // Act
        SOBEK_TYPE sobekEntity = mapToSobek(netexEntity);

        // Assert
        assertEquals(1, sobekEntity.getKeyValues().size(), "Should have one keyValue");
    }

    private KeyValueStructure createKeyValue(String key, String value) {
        KeyValueStructure kv = new KeyValueStructure();
        kv.setKey(key);
        kv.setValue(value);
        return kv;
    }
}