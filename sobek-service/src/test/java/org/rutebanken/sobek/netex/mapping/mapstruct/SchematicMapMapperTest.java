package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SchematicMapMapperTest extends DataManagedObjectStructureMapperTestBase<
    org.rutebanken.netex.model.SchematicMap,
    org.rutebanken.sobek.model.vehicle.SchematicMap> {

    @Autowired
    private SchematicMapMapper mapper;

    protected SchematicMapMapperTest() {
        super(org.rutebanken.netex.model.SchematicMap.class, org.rutebanken.sobek.model.vehicle.SchematicMap.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.SchematicMap mapToSobek(org.rutebanken.netex.model.SchematicMap source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.SchematicMap mapToNetex(org.rutebanken.sobek.model.vehicle.SchematicMap source) {
        return mapper.mapToNetex(source, context);
    }

}
