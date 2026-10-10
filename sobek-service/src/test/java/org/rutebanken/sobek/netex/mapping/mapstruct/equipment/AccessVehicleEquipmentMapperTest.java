package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.AccessVehicleEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccessVehicleEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    AccessVehicleEquipment,
    org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment> {

    @Autowired
    private AccessVehicleEquipmentMapper mapper;

    protected AccessVehicleEquipmentMapperTest() {
        super(AccessVehicleEquipment.class, org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment mapToSobek(AccessVehicleEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected AccessVehicleEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}
