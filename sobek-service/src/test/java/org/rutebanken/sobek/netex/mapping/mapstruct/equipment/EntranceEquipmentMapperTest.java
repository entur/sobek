package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.EntranceEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EntranceEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    EntranceEquipment,
    org.rutebanken.sobek.model.vehicle.EntranceEquipment> {

    @Autowired
    private EntranceEquipmentMapper mapper;

    protected EntranceEquipmentMapperTest() {
        super(EntranceEquipment.class, org.rutebanken.sobek.model.vehicle.EntranceEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.EntranceEquipment mapToSobek(EntranceEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected EntranceEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.EntranceEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}
