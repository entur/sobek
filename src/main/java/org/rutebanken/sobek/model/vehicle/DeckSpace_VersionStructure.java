package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.CoveredEnumeration;

import java.math.BigInteger;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public abstract class DeckSpace_VersionStructure extends DeckComponent_VersionStructure {

    @Enumerated(EnumType.STRING)
    private CoveredEnumeration covered;
    private Boolean airConditioned;
    private Boolean smokingAllowed;
    private BigInteger totalCapacity;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<PassengerEntrance> deckEntrances;

    @ManyToOne
    protected DeckSpace parentDeckSpace;

    @Transient
    private String parentDeckSpaceRefNotMapped;
    @Transient
    private String incomingId;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<DeckSpaceCapacity> deckSpaceCapacities;

    // TODO - TBD
//    protected TypeOfDeckSpaceProfileRefStructure typeOfDeckSpaceRef;
//    protected DeckEntranceCouples_RelStructure deckEntranceCouples;
//    protected DeckEntranceUsages_RelStructure deckEntranceUsages;
//    protected DeckWindows_RelStructure deckWindows;


}
