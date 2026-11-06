package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

/**
 * A component of a {@link Train}: a train element in a position. The position in the owner's list is the NeTEx order.
 * The train element is only held as a reference (the id of a NeTEx TrainElement or TrainElementType).
 */
@Entity
@Getter
@Setter
public class TrainComponent extends VersionedChildStructure {

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

    private String trainElementRef;

    @Enumerated(EnumType.STRING)
    private VehicleOrientationEnumeration operationalOrientation;

    @Enumerated(EnumType.STRING)
    private ThroughAccessEnumeration forwardCouplingThroughAccess;
}
