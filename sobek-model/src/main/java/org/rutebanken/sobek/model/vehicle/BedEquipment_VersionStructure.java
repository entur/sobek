package org.rutebanken.sobek.model.vehicle;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BedEquipment_VersionStructure extends SpotEquipment_VersionStructure {
    protected BedTypeEnumeration bedType;
    protected Boolean isStowable;
    protected BigDecimal headroom;
    protected BigDecimal bedLength;
}
