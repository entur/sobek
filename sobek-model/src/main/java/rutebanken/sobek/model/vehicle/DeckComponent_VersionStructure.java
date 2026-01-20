package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class DeckComponent_VersionStructure extends OnboardSpace_VersionStructure {
    private Boolean publicUse;

    @Enumerated(EnumType.STRING)
    private org.rutebanken.sobek.model.vehicle.FareClassEnumeration fareClass;

//    protected DeckLevelRefStructure deckLevelRef;
//    protected ClassOfUseRef classOfUseRef;

//    protected AccessibilityAssessment accessibilityAssessment;

}
