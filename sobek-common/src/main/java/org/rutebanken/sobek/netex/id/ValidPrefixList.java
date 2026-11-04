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

@Component
public class ValidPrefixList {

    private static final Logger logger = LoggerFactory.getLogger(ValidPrefixList.class);

    public static final String ANY_PREFIX = "*";

    /** Codespace of contractual vehicle types. Physical vehicle types use the configured netex.validPrefix. */
    public static final String CONTRACTUAL_VEHICLE_TYPE_PREFIX = "CVT";

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
     * The type of the codespace the id is in, for an entity of the given type. An id that is not on the form
     * {@code <codespace>:<type>:<number>} is treated as external, as it cannot be one Sobek or a registered
     * codespace has assigned.
     */
    public CodespaceType getCodespaceTypeOf(String netexId, Class<?> entityType) {
        return getCodespaceTypeOf(netexId, entityType.getSimpleName());
    }

    public CodespaceType getCodespaceTypeOf(String netexId, String type) {
        if (netexId == null) {
            return CodespaceType.EXTERNAL;
        }
        String[] parts = netexId.split(":", -1);
        if (parts.length != 3) {
            return CodespaceType.EXTERNAL;
        }
        return getCodespaceTypeOfPrefix(parts[0], type);
    }

    public CodespaceType getCodespaceTypeOfPrefix(String prefix, String type) {
        if (prefix == null) {
            return CodespaceType.EXTERNAL;
        }
        if (prefix.equals(validNetexPrefix)
                || (prefix.equals(CONTRACTUAL_VEHICLE_TYPE_PREFIX) && "VehicleType".equals(type))) {
            return CodespaceType.SOBEK;
        }

        List<String> registered = this.get(type);
        if (registered != null && (registered.contains(prefix) || registered.contains(ANY_PREFIX))) {
            return CodespaceType.REGISTERED;
        }
        return CodespaceType.EXTERNAL;
    }
}
