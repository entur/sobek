package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@MappedSuperclass
@Getter
@Setter
public class SeatEquipment_VersionStructure extends SpotEquipment_VersionStructure {
    private BigDecimal seatBackHeight;
    private BigDecimal seatDepth;
    private Boolean isFoldup;
    private Boolean isReclining;
    private BigInteger maximumRecline;
    private Boolean isReversible;
    private Boolean canRotate;
}