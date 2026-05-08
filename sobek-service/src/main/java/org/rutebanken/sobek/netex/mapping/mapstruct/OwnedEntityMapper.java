package org.rutebanken.sobek.netex.mapping.mapstruct;

import lombok.Getter;
import lombok.Setter;
import org.mapstruct.MappingTarget;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class OwnedEntityMapper {

    private String dataOwnerRef;

    public void updateSobekFromNetex(
            @MappingTarget OwnedEntity target
    ) {
        if (target != null) {
            target.setDataOwnerRef(dataOwnerRef);
        }
    }
}
