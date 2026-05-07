package org.rutebanken.sobek.netex.mapping.mapstruct;

import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

@Mapper(
        config = SobekMapperConfig.class
)
@Getter
@Setter
public class OwnedEntityMapper {

    private String dataOwnerRef;

    public void updateSobekFromNetex(
            org.rutebanken.netex.model.DataManagedObjectStructure source,
            @MappingTarget OwnedEntity target,
            @Context MappingContext context
    ) {
        if (target != null) {
            target.setDataOwnerRef(dataOwnerRef);
        }
    }
}
