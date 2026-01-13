package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Getter
@Setter
public class SpotAffinity extends SpotAffinity_VersionStructure {
    @ManyToMany
    @Cascade({ CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "spot_affinity_member",
            joinColumns = @JoinColumn(name = "spot_affinity_id"),
            inverseJoinColumns = @JoinColumn(name = "locatable_spot_id")
    )
    private java.util.List<LocatableSpot> members;}
