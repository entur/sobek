package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.rutebanken.netex.model.DeckPlan;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeckPlanMapperTest extends DataManagedObjectStructureMapperTestBase<
    DeckPlan,
    org.rutebanken.sobek.model.vehicle.DeckPlan> {

    @Autowired
    private DeckPlanMapper mapper;

    protected DeckPlanMapperTest() {
        super(org.rutebanken.netex.model.DeckPlan.class, org.rutebanken.sobek.model.vehicle.DeckPlan.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.DeckPlan mapToSobek(org.rutebanken.netex.model.DeckPlan source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.DeckPlan mapToNetex(org.rutebanken.sobek.model.vehicle.DeckPlan source) {
        return mapper.mapToNetex(source, context);
    }
}
