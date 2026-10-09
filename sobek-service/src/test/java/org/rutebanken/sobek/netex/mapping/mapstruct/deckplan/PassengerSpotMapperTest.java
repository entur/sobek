package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.rutebanken.netex.model.PassengerSpot;
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
        super(org.rutebanken.netex.model.PassengerSpot.class, org.rutebanken.sobek.model.vehicle.PassengerSpot.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerSpot mapToSobek(org.rutebanken.netex.model.PassengerSpot source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.PassengerSpot mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerSpot source) {
        return mapper.mapToNetex(source, context);
    }
}
