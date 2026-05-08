package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.stereotype.Service;

@Service
public class OwnedEntityMapper {

    public void updateSobekFromNetex(
            OwnedEntity target,
            MappingContext context
    ) {
        if (target != null) {
            target.setDataOwnerRef(context.getDataOwnerRef());
        }
    }
}
