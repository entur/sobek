package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@MappedSuperclass
public class StaircaseEquipment_VersionStructure extends StairEquipment_VersionStructure {
    private Boolean continuousHandrail;
    private Boolean withoutRiser;
    private Boolean spiralStair;
    private BigInteger numberOfFlights;
    //private StairFlights_RelStructure flights;
}

