package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.rutebanken.netex.model.SpotAffinity;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpotAffinityMapperTest extends DataManagedObjectStructureMapperTestBase<
    SpotAffinity,
    org.rutebanken.sobek.model.vehicle.SpotAffinity> {

    @Autowired
    private SpotAffinityMapper mapper;

    protected SpotAffinityMapperTest() {
        super(SpotAffinity.class, org.rutebanken.sobek.model.vehicle.SpotAffinity.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.SpotAffinity mapToSobek(SpotAffinity source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected SpotAffinity mapToNetex(org.rutebanken.sobek.model.vehicle.SpotAffinity source) {
        return mapper.mapToNetex(source, context);
    }
}
