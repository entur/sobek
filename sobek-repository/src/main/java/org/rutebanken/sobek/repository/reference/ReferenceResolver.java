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

package org.rutebanken.sobek.repository.reference;

import org.rutebanken.sobek.model.CustomKeyValueTypes;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.TypeFromIdResolver;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.generic.GenericEntityInVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Note that: Not all references are references to DataManagedObjectStructure.
 * For instance, AccomodationRefStructure.
 * This means that key value lookup cannot happen for this entity.
 */
@Component
public class ReferenceResolver {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceResolver.class);

    @Autowired
    private GenericEntityInVersionRepository genericEntityInVersionRepository;

    @Autowired
    private ValidPrefixList validPrefixList;

    @Autowired
    private TypeFromIdResolver typeFromIdResolver;

    @Autowired
    private NetexIdHelper netexIdHelper;

    public <T extends DataManagedObjectStructure> T resolve(VersionOfObjectRefStructure versionOfObjectRefStructure, Class<T> clazz) {
        return resolve(versionOfObjectRefStructure.getRef(), versionOfObjectRefStructure.getVersion(), clazz);
    }

    public <T extends DataManagedObjectStructure> T resolve(String ref, String refVersion, Class<T> clazz) {

        logger.debug("Received reference: {} {}", ref, refVersion);

        if (ref == null) {
            throw new IllegalArgumentException("ref value cannot be null: ");
        }

        long colonCount = ref.chars().filter(ch -> ch == ':').count();
        if (colonCount != 2) {
            throw new IllegalArgumentException("Expected two number of colons in ref. Got: '" + ref + "'");
        }
        String memberClass = netexIdHelper.extractIdType(ref);

        String prefix = netexIdHelper.extractIdPrefix(ref);

        final String netexId;
        if (!validPrefixList.isValidPrefixForType(prefix, memberClass)) {
            logger.debug("Detected ID without valid prefix: {} and type {}. Will try to find it from original ID: {}.", prefix, memberClass, ref);
            Set<String> valuesArgument = new HashSet<>(Collections.singleton(ref));
            netexId = genericEntityInVersionRepository.findByKeyValue(CustomKeyValueTypes.ORIGINAL_ID_KEY, valuesArgument, clazz);
        } else {
            netexId = ref;
        }

        if (refVersion == null || refVersion.equals("any")) {
            return genericEntityInVersionRepository.findFirstByNetexIdOrderByVersionDesc(netexId, clazz);
        } else {
            long version = Long.parseLong(refVersion);
            return genericEntityInVersionRepository.findFirstByNetexIdAndVersion(netexId, version, clazz);
        }

    }

    public <T extends DataManagedObjectStructure> T resolve(VersionOfObjectRefStructure versionOfObjectRefStructure) {
        return resolve(versionOfObjectRefStructure.getRef(), versionOfObjectRefStructure.getVersion());
    }

    public <T extends DataManagedObjectStructure> T resolve(String ref, String refVersion) {
        logger.debug("Received reference: {} {}", ref, refVersion);

        if (ref == null) {
            throw new IllegalArgumentException("ref value cannot be null: ");
        }


        long colonCount = ref.chars().filter(ch -> ch == ':').count();
        if (colonCount != 2) {
            throw new IllegalArgumentException("Expected two number of colons in ref. Got: '" + ref + "'");
        }
        String memberClass = netexIdHelper.extractIdType(ref);

        Class<T> clazz = typeFromIdResolver.resolveClassFromId(ref);

        String prefix = netexIdHelper.extractIdPrefix(ref);

        final String netexId;
        if (!validPrefixList.isValidPrefixForType(prefix, memberClass)) {
            logger.debug("Detected ID without valid prefix: {} and type {}. Will try to find it from original ID: {}.", prefix, memberClass, ref);
            Set<String> valuesArgument = new HashSet<>(Collections.singleton(ref));
            netexId = genericEntityInVersionRepository.findByKeyValue(CustomKeyValueTypes.ORIGINAL_ID_KEY, valuesArgument, clazz);
        } else {
            netexId = ref;
        }

        if (refVersion == null || refVersion.equals("any")) {
            return genericEntityInVersionRepository.findFirstByNetexIdOrderByVersionDesc(netexId, clazz);
        } else {
            long version = Long.parseLong(refVersion);
            return genericEntityInVersionRepository.findFirstByNetexIdAndVersion(netexId, version, clazz);
        }

    }
}
