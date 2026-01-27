package org.rutebanken.sobek.model.vehicle;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

import java.util.List;

@Entity
@Getter
@Setter
public class SchematicMap extends DataManagedObjectStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString name;

    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "short_name_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "short_name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString shortName;

    private String imageUri;

    private String depictedObjectRef;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<SchematicMapMember> members;
}
