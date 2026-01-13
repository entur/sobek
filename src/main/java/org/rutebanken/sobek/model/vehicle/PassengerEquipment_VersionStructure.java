package org.rutebanken.sobek.model.vehicle;

import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.InstalledEquipment_VersionStructure;

@Getter
@Setter
public abstract class PassengerEquipment_VersionStructure extends InstalledEquipment_VersionStructure {
    protected Boolean fixed;
}