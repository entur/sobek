package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.rutebanken.netex.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;

/**
 * Generic helper for resolving NeTEx reference structures to Sobek entities.
 * This class provides utility methods for resolving references that inherit from VersionOfObjectRefStructure.
 */
public class ReferenceMapper {

    /**
     * Resolves a VersionOfObjectRefStructure to its corresponding entity using ReferenceResolver.
     *
     * @param refStructure the reference structure to resolve
     * @param targetClass  the expected entity class
     * @param context      the mapping context containing the reference resolver
     * @param <T>          the entity type extending DataManagedObjectStructure
     * @return the resolved entity, or null if resolution fails or resolver is not available
     */
    public static <T extends DataManagedObjectStructure> T resolveReference(
            VersionOfObjectRefStructure refStructure,
            Class<T> targetClass,
            MappingContext context
    ) {
        if (refStructure == null || refStructure.getRef() == null) {
            return null;
        }

        if (context.getReferenceResolver() == null) {
            return null;
        }

        try {
            ReferenceResolver resolver = context.getReferenceResolver();
            return resolver.resolve(
                    refStructure.getRef(),
                    refStructure.getVersion(),
                    targetClass
            );
        } catch (Exception e) {
            // Log error if needed, but don't fail the mapping
            return null;
        }
    }

    /**
     * Extracts the ref string from a VersionOfObjectRefStructure.
     *
     * @param refStructure the reference structure
     * @return the ref string, or null if the structure is null
     */
    public static String extractRef(VersionOfObjectRefStructure refStructure) {
        return refStructure != null ? refStructure.getRef() : null;
    }

    /**
     * Creates a VersionOfObjectRefStructure from an entity.
     *
     * @param entity       the entity to create a reference from
     * @param refClass     the reference structure class to instantiate
     * @param <R>          the reference type extending VersionOfObjectRefStructure
     * @return a new reference structure, or null if entity is null or has no netexId
     */
    public static <R extends VersionOfObjectRefStructure> R createReference(
            DataManagedObjectStructure entity,
            Class<R> refClass
    ) {
        if (entity == null || entity.getNetexId() == null) {
            return null;
        }

        try {
            R reference = refClass.getDeclaredConstructor().newInstance();
            reference.setRef(entity.getNetexId());
            reference.setVersion(String.valueOf(entity.getVersion()));
            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reference structure: " + refClass.getName(), e);
        }
    }

    /**
     * Creates a VersionOfObjectRefStructure from a ref string.
     *
     * @param ref      the reference string
     * @param refClass the reference structure class to instantiate
     * @param <R>      the reference type extending VersionOfObjectRefStructure
     * @return a new reference structure, or null if ref is null
     */
    public static <R extends VersionOfObjectRefStructure> R createReference(
            String ref,
            Class<R> refClass
    ) {
        if (ref == null) {
            return null;
        }

        try {
            R reference = refClass.getDeclaredConstructor().newInstance();
            reference.setRef(ref);
            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reference structure: " + refClass.getName(), e);
        }
    }
}