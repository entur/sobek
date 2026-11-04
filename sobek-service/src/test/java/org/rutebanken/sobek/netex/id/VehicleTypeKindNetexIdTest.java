package org.rutebanken.sobek.netex.id;

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTypeKindNetexIdTest {

    private final ValidPrefixList validPrefixList = new ValidPrefixList("NMR", List.of("RUT"));
    private final NetexIdHelper netexIdHelper = new NetexIdHelper(validPrefixList);

    private VehicleType vehicleType(VehicleTypeKindEnumeration kind) {
        VehicleType vt = new VehicleType();
        vt.setVehicleTypeKind(kind);
        return vt;
    }

    @Test
    void physicalVehicleTypeGetsConfiguredPrefix() {
        assertThat(netexIdHelper.getNetexId(vehicleType(VehicleTypeKindEnumeration.PHYSICAL), 7)).isEqualTo("NMR:VehicleType:7");
    }

    @Test
    void vehicleTypeWithoutKindGetsConfiguredPrefix() {
        assertThat(netexIdHelper.getNetexId(vehicleType(null), 7)).isEqualTo("NMR:VehicleType:7");
    }

    @Test
    void contractualVehicleTypeGetsContractualCodespaceWithSameSequenceNumber() {
        assertThat(netexIdHelper.getNetexId(vehicleType(VehicleTypeKindEnumeration.CONTRACTUAL), 7)).isEqualTo("CVT:VehicleType:7");
    }

    @Test
    void otherEntitiesAreUnaffected() {
        assertThat(netexIdHelper.getNetexId(new Vehicle(), 7)).isEqualTo("NMR:Vehicle:7");
    }

    @Test
    void contractualCodespaceIsValidForVehicleTypesOnly() {
        assertThat(validPrefixList.isValidPrefixForType("CVT", VehicleType.class)).isTrue();
        assertThat(validPrefixList.isValidPrefixForType("CVT", Vehicle.class)).isFalse();
    }
}
