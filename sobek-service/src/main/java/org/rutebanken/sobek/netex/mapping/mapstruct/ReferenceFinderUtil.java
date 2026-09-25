package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReferenceFinderUtil {

    private static final Logger log = LoggerFactory.getLogger(ReferenceFinderUtil.class);

    private ReferenceFinderUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Finds an entity in a list by matching its NeTEx ID with the provided reference string.
     * Logs a warning if no matching element is found.
     *
     * @param entities the list of entities to search through
     * @param reference the NeTEx reference ID to match
     * @param entityTypeName optional descriptive name of the entity type for logging purposes
     * @param <T> the entity type extending DataManagedObjectStructure
     * @return the matching entity, or null if not found
     */
    public static <T extends DataManagedObjectStructure> T findByNetexId(
            List<T> entities,
            String reference,
            String entityTypeName
    ) {
        if (reference == null || reference.isEmpty()) {
            return null;
        }

        if (entities == null) {
            return null;
        }

        T result = entities.stream()
                .filter(entity -> entity.netexIdEquals(reference))
                .findFirst()
                .orElse(null);

        if (result == null) {
            String typeName = entityTypeName != null ? entityTypeName : "entity";
            log.warn("No matching {} found for reference: {}", typeName, reference);
        }

        return result;
    }

    /**
     * Finds an entity in a list by matching its NeTEx ID with the provided reference string.
     * Logs a warning if no matching element is found.
     *
     * @param entities the list of entities to search through
     * @param reference the NeTEx reference ID to match
     * @param <T> the entity type extending DataManagedObjectStructure
     * @return the matching entity, or null if not found
     */
    public static <T extends DataManagedObjectStructure> T findByNetexId(
            List<T> entities,
            String reference
    ) {
        return findByNetexId(entities, reference, null);
    }
}

