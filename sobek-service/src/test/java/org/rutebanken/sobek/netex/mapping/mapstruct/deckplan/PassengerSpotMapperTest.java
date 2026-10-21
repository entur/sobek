package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.entur.netex2.model.PassengerSpot;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PassengerSpotMapperTest extends DataManagedObjectStructureMapperTestBase<
    PassengerSpot,
    org.rutebanken.sobek.model.vehicle.PassengerSpot> {

    @Autowired
    private PassengerSpotMapper mapper;

    protected PassengerSpotMapperTest() {
        super(org.entur.netex2.model.PassengerSpot.class, org.rutebanken.sobek.model.vehicle.PassengerSpot.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerSpot mapToSobek(org.entur.netex2.model.PassengerSpot source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.PassengerSpot mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerSpot source) {
        return mapper.mapToNetex(source, context);
    }
}
