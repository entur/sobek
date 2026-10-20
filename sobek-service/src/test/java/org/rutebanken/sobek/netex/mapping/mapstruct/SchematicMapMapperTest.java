package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SchematicMapMapperTest extends DataManagedObjectStructureMapperTestBase<
    org.entur.netex2.model.SchematicMap,
    org.rutebanken.sobek.model.vehicle.SchematicMap> {

    @Autowired
    private SchematicMapMapper mapper;

    protected SchematicMapMapperTest() {
        super(org.entur.netex2.model.SchematicMap.class, org.rutebanken.sobek.model.vehicle.SchematicMap.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.SchematicMap mapToSobek(org.entur.netex2.model.SchematicMap source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.SchematicMap mapToNetex(org.rutebanken.sobek.model.vehicle.SchematicMap source) {
        return mapper.mapToNetex(source, context);
    }

}
