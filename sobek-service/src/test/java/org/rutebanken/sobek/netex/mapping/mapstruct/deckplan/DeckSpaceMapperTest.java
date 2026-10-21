package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.entur.netex2.model.PassengerSpace;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeckSpaceMapperTest extends DataManagedObjectStructureMapperTestBase<
    PassengerSpace,
    org.rutebanken.sobek.model.vehicle.PassengerSpace> {

    @Autowired
    private DeckSpaceMapper mapper;

    protected DeckSpaceMapperTest() {
        super(org.entur.netex2.model.PassengerSpace.class, org.rutebanken.sobek.model.vehicle.PassengerSpace.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerSpace mapToSobek(org.entur.netex2.model.PassengerSpace source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.PassengerSpace mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerSpace source) {
        return mapper.mapToNetex(source, context);
    }
}
