
package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

@Entity
@Getter
@Setter
public class DeckLevel extends DataManagedObjectStructure {
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "label_value")),
        @AttributeOverride(name = "lang", column = @Column(name = "label_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString label;

    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "name_value")),
        @AttributeOverride(name = "lang", column = @Column(name = "name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString name;

    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "description_value")),
        @AttributeOverride(name = "lang", column = @Column(name = "description_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString description;

    private Boolean publicUse;

}