package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public class TransportType_VersionStructure extends DataManagedObjectStructure {
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
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "description_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "description_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString description;
    @Embedded
    private PrivateCodeStructure privateCode;
    private String euroClass;
    private Boolean reversingDirection;
    private Boolean selfPropelled;
    @Convert(converter = PropulsionTypeListConverter.class)
    private List<PropulsionTypeEnumeration> propulsionTypes;
    @Convert(converter = FuelTypeListConverter.class)
    private List<FuelTypeEnumeration> fuelTypes;
    @Enumerated(EnumType.STRING)
    private FuelTypeEnumeration typeOfFuel;
    private BigDecimal maximumRange;
    private BigDecimal maximumVelocity;
    @Enumerated(EnumType.STRING)
    private AllPublicTransportModesEnumeration transportMode;
    @Transient
    private DeckPlanRefStructure deckPlanRef;
    @ManyToOne
    private org.rutebanken.sobek.model.vehicle.DeckPlan deckPlan;


}
