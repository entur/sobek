package org.rutebanken.sobek.model.vehicle;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

@Entity
@Getter
@Setter
public class SchematicMapMember extends VersionedChildStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString name;

    private Boolean hide;
    private Boolean displayAsIcon;
    private Float x;
    private Float y;

    // TODO - TBD
//    private JAXBElement<? extends VersionOfObjectRefStructure> versionOfObjectRef;
//    private InfoLinkStructure infoLink;
//    private JAXBElement<? extends Projection_VersionStructure> projection;
}
