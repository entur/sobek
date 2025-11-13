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

package org.rutebanken.sobek.netex.mapping.mapper;

import com.google.common.base.Strings;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.EntityStructure;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.rutebanken.sobek.netex.mapping.mapper.DataManagedObjectStructureMapper.CHANGED_BY;
import static org.rutebanken.sobek.netex.mapping.mapper.DataManagedObjectStructureMapper.VERSION_COMMENT;

/**
 * Note: Implemented because of an issue with using
 * CustomMapper<EntityStructure, org.rutebanken.sobek.model.EntityStructure>
 * and missing default mapping for subtypes
 **/
@Component
public class NetexIdMapper {

    private static final Logger logger = LoggerFactory.getLogger(NetexIdMapper.class);

    public static final String ORIGINAL_ID_KEY = "imported-id";
    public static final String MERGED_ID_KEY = "merged-id";

    private static final List<String> IGNORE_KEYS = Arrays.asList(CHANGED_BY, VERSION_COMMENT);

    @Autowired
    private final ValidPrefixList validPrefixList;

    @Autowired
    private final NetexIdHelper netexIdHelper;

    public NetexIdMapper(ValidPrefixList validPrefixList, NetexIdHelper netexIdHelper) {
        this.validPrefixList = validPrefixList;
        this.netexIdHelper = netexIdHelper;
    }

    public void toNetexModel(EntityStructure internalEntity, org.rutebanken.netex.model.EntityStructure netexEntity) {
        if(internalEntity.getNetexId() == null) {
            logger.warn("Netex ID for internal model object is null. Mapping to null value. Object: {}", internalEntity);
            netexEntity.setId(null);
        } else {
            netexEntity.setId(internalEntity.getNetexId());
        }
    }

    public void toSobekModel(org.rutebanken.netex.model.EntityInVersionStructure netexEntity, EntityInVersionStructure sobekEntity) {

        if(netexEntity.getId() == null) {
            sobekEntity.setNetexId(null);
        } else if(validPrefixList.isValidPrefixForType(netexIdHelper.extractIdPrefix(netexEntity.getId()), sobekEntity.getClass())) {
            logger.debug("Detected ID with valid prefix: {}. ", netexEntity.getId());
            sobekEntity.setNetexId(netexEntity.getId().trim());
        } else {
            logger.debug("Received ID {}. Will map it as key value ", netexEntity.getId());
            if(sobekEntity instanceof  DataManagedObjectStructure) {
                moveOriginalIdToKeyValueList((DataManagedObjectStructure) sobekEntity, netexEntity.getId());
                sobekEntity.setNetexId(null);
            }
        }
        if(netexEntity instanceof org.rutebanken.netex.model.DataManagedObjectStructure && sobekEntity instanceof DataManagedObjectStructure) {
            logger.debug("Copy key values to sobek model {}", sobekEntity.getNetexId());
            copyKeyValuesToSobekModel((org.rutebanken.netex.model.DataManagedObjectStructure) netexEntity, (DataManagedObjectStructure) sobekEntity);
        }
    }

    /**
     * Copies key values from netex object to internal Sobek model.
     * The internal Sobek model can hold lists of values for each key.
     * Therefore, if the key matches ORIGINAL_ID_KEY, the incoming values will be separated by comma.
     * @param netexEntity netexEntity containing key values. If it contains ORIGINAL_ID_KEY. Values will be separated.
     * @param sobekEntity sobek entity to add key values to.
     */
    public void copyKeyValuesToSobekModel(org.rutebanken.netex.model.DataManagedObjectStructure netexEntity, DataManagedObjectStructure sobekEntity) {
        if(netexEntity.getKeyList() != null) {
            if(netexEntity.getKeyList().getKeyValue() != null) {

                for(KeyValueStructure keyValueStructure : netexEntity.getKeyList().getKeyValue()) {
                    String value = keyValueStructure.getValue();
                    String key = keyValueStructure.getKey();

                    if(IGNORE_KEYS.contains(key)) {
                        // Mapped elsewhere
                        continue;
                    }

                    boolean ignoreEmptyPostfix = (key.equals(ORIGINAL_ID_KEY) | key.equals(MERGED_ID_KEY));

                    if (value.contains(",")) {
                        String[] originalIds = value.split(",");
                        for (String originalId : originalIds) {
                            addKeyValueAvoidEmpty(sobekEntity, key, originalId, ignoreEmptyPostfix);
                        }
                    } else {
                        addKeyValueAvoidEmpty(sobekEntity, key, value, ignoreEmptyPostfix);
                    }
                }
            }
        }
    }

    private void addKeyValueAvoidEmpty(DataManagedObjectStructure sobekEntity, final String key, final String value, boolean ignoreEmptyPostfix) {

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
            sobekEntity.getOrCreateValues(keytoAdd).add(valueToAdd);
        }
    }

    /**
     * Writes netex ID to keyval in internal Sobek model
     * @param dataManagedObjectStructure to set the keyval on (sobek model)
     * @param netexId The id to add to values, using the key #{ORIGINAL_ID_KEY}
     */
    public void moveOriginalIdToKeyValueList(DataManagedObjectStructure dataManagedObjectStructure, String netexId) {
        addKeyValueAvoidEmpty(dataManagedObjectStructure, ORIGINAL_ID_KEY, netexId, true);
    }

}
