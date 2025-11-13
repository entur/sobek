package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import net.opengis.gml._3.MultiSurfaceType;
import net.opengis.gml._3.PolygonType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.rutebanken.netex.OmitNullsToStringStyle;

@MappedSuperclass
@Getter
@Setter
public class DeckComponent_VersionStructure extends OnboardSpace_VersionStructure {
    private Boolean publicUse;

    @Enumerated(EnumType.STRING)
    private FareClassEnumeration fareClass;

//    protected DeckLevelRefStructure deckLevelRef;
//    protected ClassOfUseRef classOfUseRef;

//    protected AccessibilityAssessment accessibilityAssessment;

}
