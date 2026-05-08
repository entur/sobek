package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Context;
import org.mapstruct.MappingTarget;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.stereotype.Service;

@Service
public class OwnedEntityMapper {

    public void updateSobekFromNetex(
            @MappingTarget OwnedEntity target,
            @Context MappingContext context
    ) {
        if (target != null) {
            target.setDataOwnerRef(context.getDataOwnerRef());
        }
    }
}
