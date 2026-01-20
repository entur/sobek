package org.rutebanken.sobek.model.vehicle;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

import java.math.BigInteger;

@Getter
@Setter
@Entity
public class DeckSpaceCapacity extends VersionedChildStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString name;

    private TypeOfLocatableSpotEnumeration locatableSpotType;
    private BigInteger capacity;

    // TODO - TBD
//    protected TypeOfLocatableSpot typeOfLocatableSpotRef;
}
