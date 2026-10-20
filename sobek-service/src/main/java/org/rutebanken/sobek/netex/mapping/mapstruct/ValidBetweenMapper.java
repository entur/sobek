package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.*;
import org.entur.netex2.model.ValidBetween;

/**
 * MapStruct mapper for ValidBetween.
 */
@Mapper(uses = {CommonTypeMapper.class, TemporalTypeMapper.class})
public interface ValidBetweenMapper {

    org.rutebanken.sobek.model.ValidBetween mapToSobek(
            ValidBetween source
    );

    ValidBetween mapToNetex(
            org.rutebanken.sobek.model.ValidBetween source
    );

    void updateSobekFromNetex(
            ValidBetween source,
            @MappingTarget org.rutebanken.sobek.model.ValidBetween target
    );
}