package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@MappedSuperclass
public class LuggageSpot_VersionStructure extends LocatableSpot {
    private Boolean isAccessibleOnVoyage;
    private BigDecimal heightFromFloor;
}
