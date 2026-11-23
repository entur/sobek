package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.PrivateCodeStructure;
import org.rutebanken.sobek.model.authorization.OwnedEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A group of vehicles that are managed together. Corresponds to NeTEx Fleet, which is a GroupOfEntities.
 * <p>
 * The members are Vehicle rows, and the transport types are the vehicle types the fleet is made of. Both are
 * references to the rows that are current: when a vehicle or a vehicle type gets a new version, the references
 * follow it (see FleetRepository#moveMember and #moveTransportType), as the vehicle's own reference to its vehicle type does.
 * <p>
 * Not yet supported: TransportOrganisationRef and TypeOfFleetRef.
 */
@Entity
@Getter
@Setter
public class Fleet extends DataManagedObjectStructure implements OwnedEntity {

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

    // Eager: GraphQL and the exports read the members after the query that loaded the fleet has ended.
    // SUBSELECT loads the members of all fleets of a query in one extra select.
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "fleet_members",
            joinColumns = @JoinColumn(name = "fleet_id"),
            inverseJoinColumns = @JoinColumn(name = "members_id"))
    private List<Vehicle> members = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = "fleet_transport_types",
            joinColumns = @JoinColumn(name = "fleet_id"),
            inverseJoinColumns = @JoinColumn(name = "transport_types_id"))
    private List<VehicleType> transportTypes = new ArrayList<>();

    /**
     * The organisation of the fleet, as the id of an organisation in the organisation registry. It is a generic
     * organisation: its type (authority, operator or other) is not part of the fleet. In NeTEx it is the
     * TransportOrganisationRef, which can only be written as an OperatorRef or an AuthorityRef.
     */
    private String transportOrganisationRef;

    /**
     * The number of the contract of the fleet. NeTEx has no place for it in a Fleet, so it is a key value there (see
     * {@link org.rutebanken.sobek.model.CustomKeyValueTypes#CONTRACT_NUMBER_KEY}).
     */
    private String contractNumber;

    /**
     * The fleet is shared with everyone: users of any organisation see it, and its vehicles and vehicle types. Read only.
     */
    private boolean sharedWithAll;

    /**
     * The organisations (refs in the organisation registry) the fleet is shared with, in addition to the owner: users of these
     * see the fleet, and its vehicles and vehicle types. Read only.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "fleet_shared_with", joinColumns = @JoinColumn(name = "fleet_id"))
    @Column(name = "organisation_ref", nullable = false)
    private Set<String> sharedWith = new HashSet<>();

    /**
     * Set by the share and unshare operations on the new version they make. A new version that does not come from them
     * (an update of the fleet, an import) keeps the sharing of the previous version, as it does not mention it.
     */
    @Transient
    private boolean sharingChanged;

    @Setter(AccessLevel.NONE)
    private String dataOwnerRef;

    @Override
    public void setDataOwnerRef(String dataOwnerRef) {
        this.dataOwnerRef = dataOwnerRef;
    }

    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if (existingVersion instanceof Fleet fleetExisting) {
            mergeKeyValues(fleetExisting);
            if (!sharingChanged) {
                sharedWithAll = fleetExisting.isSharedWithAll();
                sharedWith = new HashSet<>(fleetExisting.getSharedWith());
            }
        }
    }
}
