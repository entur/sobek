package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@MappedSuperclass
public abstract class AccessEquipment_VersionStructure extends PlaceEquipment_VersionStructure {
    protected BigDecimal width;
    protected BigDecimal height;
    protected DirectionOfUseEnumeration directionOfUse;
    protected BigInteger passengersPerMinute;
    protected BigInteger relativeWeighting;
    protected Boolean safeForGuideDog;
}
