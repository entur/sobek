package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.rutebanken.netex.model.LuggageSpot;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LuggageSpotMapperTest extends DataManagedObjectStructureMapperTestBase<
    LuggageSpot,
    org.rutebanken.sobek.model.vehicle.LuggageSpot> {

    @Autowired
    private LuggageSpotMapper mapper;

    protected LuggageSpotMapperTest() {
        super(org.rutebanken.netex.model.LuggageSpot.class, org.rutebanken.sobek.model.vehicle.LuggageSpot.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.LuggageSpot mapToSobek(org.rutebanken.netex.model.LuggageSpot source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.LuggageSpot mapToNetex(org.rutebanken.sobek.model.vehicle.LuggageSpot source) {
        return mapper.mapToNetex(source, context);
    }

}
