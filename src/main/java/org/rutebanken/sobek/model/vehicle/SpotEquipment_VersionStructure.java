package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.InstalledEquipment_VersionStructure;

import java.math.BigDecimal;

@MappedSuperclass
@Getter
@Setter
public class SpotEquipment_VersionStructure extends InstalledEquipment_VersionStructure {
    private BigDecimal width;
    private BigDecimal length;
    private BigDecimal height;
    private BigDecimal heightFromFloor;
    private Boolean hasPowerSupply;
    private Boolean hasUsbPowerSocket;
}