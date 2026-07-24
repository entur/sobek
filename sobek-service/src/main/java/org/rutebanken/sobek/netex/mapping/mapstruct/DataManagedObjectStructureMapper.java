
package org.rutebanken.sobek.netex.mapping.mapstruct;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import org.mapstruct.*;
import org.rutebanken.netex.model.DataManagedObjectStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.KeyValue;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.rutebanken.sobek.model.CustomKeyValueTypes.ORIGINAL_ID_KEY;

/**
 * MapStruct mapper for DataManagedObjectStructure.
 * Handles mapping between NeTEx DataManagedObjectStructure and Sobek DataManagedObjectStructure entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                KeyListStructureMapper.class
        }
)
public interface DataManagedObjectStructureMapper {

    Logger logger = LoggerFactory.getLogger(DataManagedObjectStructureMapper.class);

    String CHANGED_BY = "CHANGED_BY";
    String VERSION_COMMENT = "VERSION_COMMENT";

    /**
     * Setters for internal sobek model when mapping from netex.
     */
    Map<String, BiConsumer<String, org.rutebanken.sobek.model.DataManagedObjectStructure>> sobekEntitySetFunctions =
            new ImmutableMap.Builder<String, BiConsumer<String, org.rutebanken.sobek.model.DataManagedObjectStructure>>()
                    .put(CHANGED_BY, (value, sobekEntity) -> sobekEntity.setChangedBy(value))
                    .put(VERSION_COMMENT, (value, sobekEntity) -> sobekEntity.setVersionComment(value))
                    .build();

    /**
     * Properties to map to key values in netex format. Getters for the sobek entity.
     */
    Map<String, Function<org.rutebanken.sobek.model.DataManagedObjectStructure, String>> sobekEntityGetFunctions =
            new ImmutableMap.Builder<String, Function<org.rutebanken.sobek.model.DataManagedObjectStructure, String>>()
                    /*
                     * Disabled as this is a system field that should not be part of export.
                     */
                    //.put(CHANGED_BY, org.rutebanken.sobek.model.DataManagedObjectStructure::getChangedBy)
                    .put(VERSION_COMMENT, org.rutebanken.sobek.model.DataManagedObjectStructure::getVersionComment)
                    .build();

    /**
     * Maps from NeTEx DataManagedObjectStructure to Sobek entity.
     */
    @ToSobekMappings
    void mapToSobek(
            DataManagedObjectStructure source,
            @MappingTarget org.rutebanken.sobek.model.DataManagedObjectStructure target,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx DataManagedObjectStructure.
     */
    @ToNetexMappings
    void mapToNetex(
            org.rutebanken.sobek.model.DataManagedObjectStructure source,
            @MappingTarget DataManagedObjectStructure target,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @ToSobekMappings
    void updateSobekFromNetex(
            DataManagedObjectStructure source,
            @MappingTarget org.rutebanken.sobek.model.DataManagedObjectStructure target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMappingToSobek(
            DataManagedObjectStructure netexEntity,
            @MappingTarget org.rutebanken.sobek.model.DataManagedObjectStructure sobekEntity,
            @Context MappingContext context
    ) {
        if (netexEntity.getKeyList() != null && netexEntity.getKeyList().getKeyValue() != null) {
            sobekEntity.clearKeyValues();
            if (!netexEntity.getKeyList().getKeyValue().isEmpty()) {
                List<KeyValue> mappedKeyValues = context.getKeyListStructureMapper().mapToSobek(netexEntity.getKeyList(), context);
                if (mappedKeyValues != null && !mappedKeyValues.isEmpty()) {
                    mappedKeyValues.forEach(kv -> sobekEntity.addKeyValue(kv.getKey(), kv.getValue()));
                }

                // Handle special keyValue processing
                netexEntity.getKeyList().getKeyValue().forEach(keyValueStructure -> {
                    logger.debug("Copy key values to sobek model {}", sobekEntity.getNetexId());
                    if (sobekEntitySetFunctions.containsKey(keyValueStructure.getKey())) {
                        sobekEntitySetFunctions.get(keyValueStructure.getKey()).accept(keyValueStructure.getValue(), sobekEntity);

                        // Remove the KeyValue with this key from the list
                        sobekEntity.removeKeyValue(keyValueStructure.getKey());
                    }
                });
            }
        }

        if(netexEntity.getId() == null) {
            sobekEntity.setNetexId(null);
        } else {
            try {
                String prefix = context.getNetexIdHelper().extractIdPrefix(netexEntity.getId());
                if (context.getValidPrefixList().isValidPrefixForType(prefix, sobekEntity.getClass())) {
                    logger.debug("Detected ID with valid prefix: {}. ", netexEntity.getId());
                    sobekEntity.setNetexId(netexEntity.getId().trim());
                } else {
                    logger.debug("Received ID {}. Will map it as key value ", netexEntity.getId());
                    moveOriginalIdToKeyValueList(sobekEntity, netexEntity.getId(), context.getNetexIdHelper());
                    sobekEntity.setNetexId(null);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Received malformed ID {}. Will map it as key value instead of failing", netexEntity.getId(), e);
                moveOriginalIdToKeyValueList(sobekEntity, netexEntity.getId(), context.getNetexIdHelper());
                sobekEntity.setNetexId(null);
            }
        }
    }

    @AfterMapping
    default void afterMappingToNetex(
            org.rutebanken.sobek.model.DataManagedObjectStructure sobekEntity,
            @MappingTarget DataManagedObjectStructure netexEntity,
            @Context MappingContext context
    ) {
        var keyList = context.getKeyListStructureMapper().mapToNetex(sobekEntity.getKeyValues(), context);
        if (keyList == null) {
            if (netexEntity.getKeyList() == null) {
                netexEntity.withKeyList(new KeyListStructure());
            }
        } else {
            if(netexEntity.getKeyList() == null) {
                netexEntity.withKeyList(keyList);
            } else {
                netexEntity.getKeyList().withKeyValue(keyList.getKeyValue());
            }
        }

        sobekEntityGetFunctions.forEach((property, function) ->
                setKey(netexEntity, property, function.apply(sobekEntity)));

        if (netexEntity.getKeyList().getKeyValue() == null || netexEntity.getKeyList().getKeyValue().isEmpty()) {
            // Do not allow empty key list
            netexEntity.withKeyList(null);
        }
    }

    default void setKey(DataManagedObjectStructure netexEntity, String key, String value) {
        if (value == null) return;

        netexEntity.getKeyList()
                .withKeyValue(new KeyValueStructure()
                        .withKey(key)
                        .withValue(value));
    }

    @Named("versionToSobekDMO")
    default Long versionToSobek(String version) {
        if (version != null) {
            if (version.equals("any")) {
                return -1L; // Need to handle this value in import.
            } else {
                Long longVersion = Longs.tryParse(version);
                if (longVersion != null) {
                    return longVersion;
                } else {
                    throw new NetexMappingException("Received version in netex format. " +
                            "But cannot parse version. Expecting a long value or the String 'any'. " +
                            "Value is: " + version);
                }
            }
        } else {
            return null;
        }
    }


    @Mapping(target = "id", ignore = true) // Handle in AfterMapping
    @Mapping(target = "netexId", ignore = true) // Handle in AfterMapping
    @Mapping(target = "version", source = "version", qualifiedByName = "versionToSobekDMO")
    @Mapping(target = "keyValues", ignore = true) // Handle in AfterMapping
    @interface ToSobekMappings {
    }

    @Mapping(target = "id", source = "netexId")
    @Mapping(target = "keyList", ignore = true) // Handle in AfterMapping
    @interface ToNetexMappings {
    }

    private void addKeyValueAvoidEmpty(org.rutebanken.sobek.model.DataManagedObjectStructure sobekEntity, final String key, final String value, boolean ignoreEmptyPostfix, NetexIdHelper netexIdHelper) {

        String keytoAdd = key.trim();
        String valueToAdd = value.trim();

        if(ignoreEmptyPostfix) {
            if(Strings.isNullOrEmpty(netexIdHelper.extractIdPostfix(valueToAdd))) {
                logger.debug("Ignoring empty postfix for key value: key {} and value '{}'", keytoAdd, valueToAdd);
                return;
            }
        }


        if(!Strings.isNullOrEmpty(keytoAdd) && !Strings.isNullOrEmpty(valueToAdd)) {
            logger.trace("Adding key {} and value {}", keytoAdd, valueToAdd);
            var existing =  sobekEntity.getKeyValues().stream().filter(kv -> kv.getKey().equals(keytoAdd)).findFirst();
            if(existing.isPresent()) {
                logger.debug("Key {} already exists. Will overwrite it", keytoAdd);
                existing.get().setValue(valueToAdd);
            } else {
                sobekEntity.addKeyValue(keytoAdd, valueToAdd);
            }
        }
    }

    /**
     * Writes netex ID to keyval in internal Tiamat model
     * @param dataManagedObjectStructure to set the keyval on (tiamat model)
     * @param netexId The id to add to values, using the key #{ORIGINAL_ID_KEY}
     */
    default void moveOriginalIdToKeyValueList(org.rutebanken.sobek.model.DataManagedObjectStructure dataManagedObjectStructure, String netexId, NetexIdHelper netexIdHelper) {
        addKeyValueAvoidEmpty(dataManagedObjectStructure, ORIGINAL_ID_KEY, netexId, true, netexIdHelper);
    }
}