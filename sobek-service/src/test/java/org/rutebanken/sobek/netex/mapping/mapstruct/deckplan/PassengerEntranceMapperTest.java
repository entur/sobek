package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.rutebanken.netex.model.PassengerEntrance;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PassengerEntranceMapperTest extends DataManagedObjectStructureMapperTestBase<
    PassengerEntrance,
    org.rutebanken.sobek.model.vehicle.PassengerEntrance> {

    @Autowired
    private PassengerEntranceMapper mapper;

    protected PassengerEntranceMapperTest() {
        super(org.rutebanken.netex.model.PassengerEntrance.class, org.rutebanken.sobek.model.vehicle.PassengerEntrance.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerEntrance mapToSobek(org.rutebanken.netex.model.PassengerEntrance source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.PassengerEntrance mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerEntrance source) {
        return mapper.mapToNetex(source, context);
    }
}
