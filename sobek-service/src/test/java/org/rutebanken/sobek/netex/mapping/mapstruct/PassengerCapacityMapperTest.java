package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PassengerCapacityMapperTest extends DataManagedObjectStructureMapperTestBase<
    org.entur.netex2.model.PassengerCapacityStructure,
    org.rutebanken.sobek.model.vehicle.PassengerCapacity> {

    @Autowired
    private PassengerCapacityMapper mapper;

    protected PassengerCapacityMapperTest() {
        super(org.entur.netex2.model.PassengerCapacityStructure.class, org.rutebanken.sobek.model.vehicle.PassengerCapacity.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.PassengerCapacity mapToSobek(org.entur.netex2.model.PassengerCapacityStructure source) {
        return mapper.mapPassengerCapacityToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.PassengerCapacityStructure mapToNetex(org.rutebanken.sobek.model.vehicle.PassengerCapacity source) {
        return mapper.mapPassengerCapacityToNetex(source, context);
    }
}
