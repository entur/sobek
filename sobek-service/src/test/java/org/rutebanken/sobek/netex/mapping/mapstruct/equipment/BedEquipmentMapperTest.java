package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.entur.netex2.model.BedEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    BedEquipment,
    org.rutebanken.sobek.model.vehicle.BedEquipment> {

    @Autowired
    private BedEquipmentMapper mapper;

    protected BedEquipmentMapperTest() {
        super(org.entur.netex2.model.BedEquipment.class, org.rutebanken.sobek.model.vehicle.BedEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.BedEquipment mapToSobek(org.entur.netex2.model.BedEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.BedEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.BedEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}
