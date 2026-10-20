package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.entur.netex2.model.LuggageSpot;
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
        super(org.entur.netex2.model.LuggageSpot.class, org.rutebanken.sobek.model.vehicle.LuggageSpot.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.LuggageSpot mapToSobek(org.entur.netex2.model.LuggageSpot source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.LuggageSpot mapToNetex(org.rutebanken.sobek.model.vehicle.LuggageSpot source) {
        return mapper.mapToNetex(source, context);
    }

}
