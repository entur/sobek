package org.rutebanken.sobek.netex.mapping.mapstruct;

import com.google.common.primitives.Longs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;

/**
 * Base mapper for Entities with version
 */
@Mapper(config = SobekMapperConfig.class)
public interface EntityInVersionMapper {

    @ToSobekMappings
    void mapNetexToSobek(EntityInVersionStructure source, @MappingTarget org.rutebanken.sobek.model.EntityInVersionStructure target);

    /**
     * Maps Sobek netexId back to NeTEx ID.
     */
    @ToNetexMappings
    void mapSobekToNetex(org.rutebanken.sobek.model.EntityInVersionStructure source, @MappingTarget EntityInVersionStructure target);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "netexId", source = "id")
    @Mapping(target = "version", source = "version", qualifiedByName = "versionToSobek")
    @interface ToSobekMappings {
    }

    @Mapping(target = "id", source = "netexId")
    @interface ToNetexMappings {
    }

    @Named("versionToSobek")
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
}
