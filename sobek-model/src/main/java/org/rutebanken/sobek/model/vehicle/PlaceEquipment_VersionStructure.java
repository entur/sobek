package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.InstalledEquipment_VersionStructure;


@Getter
@Setter
@MappedSuperclass
public abstract class PlaceEquipment_VersionStructure extends InstalledEquipment_VersionStructure {
}
