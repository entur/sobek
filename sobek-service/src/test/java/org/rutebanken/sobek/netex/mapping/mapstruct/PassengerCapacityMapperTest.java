package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PassengerCapacityMapperTest extends DataManagedObjectStructureMapperTestBase<
    org.rutebanken.netex.model.PassengerCapacityStructure,
    org.rutebanken.sobek.model.vehicle.PassengerCapacity> {

    @Autowired
    private PassengerCapacityMapper mapper;

    protected PassengerCapacityMapperTest() {
        super(org.rutebanken.netex.model.PassengerCapacityStructure.class, org.rutebanken.sobek.model.vehicle.PassengerCapacity.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerCapacity mapToSobek(org.rutebanken.netex.model.PassengerCapacityStructure source) {
        return mapper.mapPassengerCapacityToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.PassengerCapacityStructure mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerCapacity source) {
        return mapper.mapPassengerCapacityToNetex(source, context);
    }
}
