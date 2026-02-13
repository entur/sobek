
package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.*;

/**
 * Base mapper for handling ID mappings between NeTEx and Sobek entities.
 * This mapper provides consistent mapping of:
 * - NeTEx 'id' <-> Sobek 'netexId'
 * - Sobek JPA 'id' is always ignored during mapping
 */
@Mapper()
public interface EntityStructureMapper {

    /**
     * Maps NeTEx ID to Sobek netexId.
     * The JPA id is always ignored as it's managed by the persistence layer.
     */
    @EntityStructureToSobekMappings
    void mapNetexToSobek(EntityStructure source, @MappingTarget org.rutebanken.sobek.model.EntityStructure target);

    /**
     * Maps Sobek netexId back to NeTEx ID.
     */
    @EntityStructureToNetexMappings
    void mapSobekToNetex(org.rutebanken.sobek.model.EntityStructure source, @MappingTarget EntityStructure target);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "netexId", source = "id")
    @interface EntityStructureToSobekMappings {
    }

    @Mapping(target = "id", source = "netexId")
    @interface EntityStructureToNetexMappings {
    }
}
