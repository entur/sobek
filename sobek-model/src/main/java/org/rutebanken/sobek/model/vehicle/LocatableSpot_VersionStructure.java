package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class LocatableSpot_VersionStructure extends OnboardSpace_VersionStructure {
    @Enumerated(EnumType.STRING)
    private TypeOfLocatableSpotEnumeration locatableSpotType;

    @ManyToOne
    private SpotColumn spotColumn;

    @ManyToOne
    private SpotRow spotRow;

//    private TypeOfLocatableSpotRefStructure typeOfLocatableSpotRef;
//    private SensorsInSpot_RelStructure sensorsInSpot;

}
