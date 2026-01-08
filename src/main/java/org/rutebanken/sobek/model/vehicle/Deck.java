package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.Zone_VersionStructure;

@Entity
@Getter
@Setter
public class Deck extends Zone_VersionStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "label_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "label_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString label;

    @OneToMany(cascade = CascadeType.ALL)
    private List<PassengerSpace> deckSpaces;

    @OneToMany(cascade = CascadeType.ALL)
    private List<SpotRow> spotRows;

    @OneToMany(cascade = CascadeType.ALL)
    private List<SpotColumn> spotColumns;

    // TODO - TBD
//    protected DeckLevelRefStructure deckLevelRef;
//    protected DeckPathJunctionRefs_RelStructure deckPathJunctions;
//    protected DeckPathLinkRefs_RelStructure deckPathLinks;
//    protected DeckNavigationPaths_RelStructure deckNavigationPaths;

}
