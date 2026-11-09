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

package org.rutebanken.sobek.netex.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ValidPrefixList {

    private static final Logger logger = LoggerFactory.getLogger(ValidPrefixList.class);

    public static final String ANY_PREFIX = "*";

    /** Codespace of contractual vehicle types. Physical vehicle types use the configured netex.validPrefix. */
    public static final String CONTRACTUAL_VEHICLE_TYPE_PREFIX = "CVT";

    /** The vehicle type and its subtypes. They share one id sequence and the contractual codespace. */
    private static final Set<String> VEHICLE_TYPE_NAMES = Set.of("VehicleType", "Train", "CompoundTrain", "TrainElement");

    private final String validNetexPrefix;

    private final List<String> validCodeSpaces;

    @Autowired
    public ValidPrefixList(@Value("${netex.validPrefix:NMR}") String validNetexPrefix,
                           @Value("#{${netex.valid.codespaces.list:{}}}") List<String> validCodeSpaces) {

        this.validNetexPrefix = validNetexPrefix;
        this.validCodeSpaces = validCodeSpaces;
    }

    /**
     * Gets the configured prefix in netex IDs. Ex: NMR
     * See the property netex.validPrefix.
     * @return the prefix
     */
    public String getValidNetexPrefix() {
        return validNetexPrefix;
    }

    public List<String> get(Class clazz) {
        return get(clazz.getSimpleName());
    }
    public List<String> get(String type) {
        logger.trace("Looking for valid prefixes for type: {}", type);
        return validCodeSpaces;
    }

    public boolean isValidPrefixForType(String prefix, Class clazz) {
        return isValidPrefixForType(prefix, clazz.getSimpleName());
    }

    /**
     * True if the codespace is one Sobek keeps the id of as it is: its own or a registered one.
     */
    public boolean isValidPrefixForType(String prefix, String type) {
        return getCodespaceTypeOfPrefix(prefix, type) != CodespaceType.EXTERNAL;
    }

    /**
     * The type of the codespace the id is in, for an entity of the given type. The codespace is the text before
     * the first colon, whatever the rest of the id looks like: a malformed id such as {@code NMR:foo} or
     * {@code CVT:VehicleType:abc} still claims Sobek's codespace, so it is treated as Sobek's (and rejected as a
     * new id) rather than as an external id. Only an id with no colon at all has no codespace and is external.
     */
    public CodespaceType getCodespaceTypeOf(String netexId, Class<?> entityType) {
        return getCodespaceTypeOf(netexId, entityType.getSimpleName());
    }

    public CodespaceType getCodespaceTypeOf(String netexId, String type) {
        if (netexId == null) {
            return CodespaceType.EXTERNAL;
        }
        int firstColon = netexId.indexOf(':');
        if (firstColon < 0) {
            return CodespaceType.EXTERNAL;
        }
        return getCodespaceTypeOfPrefix(netexId.substring(0, firstColon), type);
    }

    public CodespaceType getCodespaceTypeOfPrefix(String prefix, String type) {
        if (prefix == null) {
            return CodespaceType.EXTERNAL;
        }
        if (prefix.equals(validNetexPrefix)
                || (prefix.equals(CONTRACTUAL_VEHICLE_TYPE_PREFIX) && VEHICLE_TYPE_NAMES.contains(type))) {
            return CodespaceType.SOBEK;
        }

        List<String> registered = this.get(type);
        if (registered != null && (registered.contains(prefix) || registered.contains(ANY_PREFIX))) {
            return CodespaceType.REGISTERED;
        }
        return CodespaceType.EXTERNAL;
    }

    public static boolean isContractualVehicleTypeId(String netexId) {
        return netexId.startsWith(ValidPrefixList.CONTRACTUAL_VEHICLE_TYPE_PREFIX + ":");
    }
}
