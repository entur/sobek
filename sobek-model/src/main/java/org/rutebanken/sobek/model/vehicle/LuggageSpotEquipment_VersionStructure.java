package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LuggageSpotEquipment_VersionStructure extends SpotEquipment_VersionStructure {
    @Enumerated(EnumType.STRING)
    private LuggageSpotTypeEnumeration luggageSpotType;
    private BigDecimal headroomForLuggage;
    private Boolean isLockable;
    private Boolean hasDoor;
}
