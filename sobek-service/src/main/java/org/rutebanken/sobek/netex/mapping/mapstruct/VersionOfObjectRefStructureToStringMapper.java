package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

@Mapper()
interface VersionOfObjectRefStructureToStringMapper {
    /**
     * Maps from NeTEx Ref structure to String.
     */
    default String mapNetexToString(
            VersionOfObjectRefStructure source,
            @Context MappingContext context
    ) {
        if(source == null) {
            return null;
        }
        return source.getRef();
    };

    /**
     * Maps from String back to NeTEx Ref structure.
     */
    default VersionOfObjectRefStructure mapStringToNetex(
            String source,
            @Context MappingContext context
    ) {
        if(source == null) {
            return null;
        }
        return new VersionOfObjectRefStructure().withRef(source);
    };

}
