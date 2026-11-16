package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

/**
 * Puts a label on a cell of the grid of a deck: a row and a column of the same deck. It belongs to a deck space, which
 * can have several. Not a part of NeTEx: it is carried in the Extensions of the deck space.
 */
@Entity
@Getter
@Setter
public class GridMarker extends VersionedChildStructure {

    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "label_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "label_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString label;

    @ManyToOne
    private SpotRow spotRow;

    @ManyToOne
    private SpotColumn spotColumn;
}
