package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

/**
 * A component of a {@link CompoundTrain}. The position in the owner's list is the NeTEx order.
 * The target is a Train or another CompoundTrain ({@link #train}), or a TrainElementType that is only held as a reference.
 */
@Entity
@Getter
@Setter
public class TrainInCompoundTrain extends VersionedChildStructure {

    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "label_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "label_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString label;

    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "description_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "description_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString description;

    @ManyToOne
    private VehicleType train;

    private String trainElementTypeRef;

    @Enumerated(EnumType.STRING)
    private VehicleOrientationEnumeration operationalOrientation;
}
