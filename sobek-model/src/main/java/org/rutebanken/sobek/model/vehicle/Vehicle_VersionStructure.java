package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import jakarta.xml.bind.JAXBElement;
import lombok.Getter;
import lombok.Setter;

import org.rutebanken.sobek.model.*;
import org.rutebanken.sobek.model.authorization.OwnedEntity;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public class Vehicle_VersionStructure extends DataManagedObjectStructure implements OwnedEntity {
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
    private Instant buildDate;
    private String chassisNumber;
    private String registrationNumber;
    private Instant registrationDate;
    private String operationalNumber;
    @Embedded
    private PrivateCodeStructure privateCode;
    //@Transient
    //private JAXBElement<? extends TransportOrganisationRefStructure> transportOrganisationRef;
    //@Transient
    //private ContactRefStructure contactRef;

    @ManyToOne
    private VehicleType transportType;
    @Transient
    private String transportTypeRef;

    @ManyToOne
    private VehicleModel vehicleModel;
    @Transient
    private String vehicleModelRef;

    @Transient
    private VehicleEquipmentProfileRefs_RelStructure equipmentProfiles;
    //@Transient
    //private JAXBElement<? extends VehicleModelProfileRefStructure> vehicleModelProfileRef;
    @Transient
    private Equipments_RelStructure actualVehicleEquipments;
    private Boolean monitored;

    private String dataOwnerRef;

    @Override
    public String getDataOwnerRef() {
        return dataOwnerRef;
    }

    @Override
    public void setDataOwnerRef(String dataOwnerRef) {
        this.dataOwnerRef = dataOwnerRef;
    }

}
