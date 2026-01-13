package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class PassengerSpace extends DeckSpace {

    @Enumerated(EnumType.STRING)
    private PassengerSpaceTypeEnumeration passengerSpaceType;
    private Boolean standingAllowed;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<PassengerSpot> passengerSpots;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<LuggageSpot> luggageSpots;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<SpotAffinity> spotAffinities;

    //TODO - TBD
//    protected PassengerVehicleSpots_RelStructure passengerVehicleSpots;

}
