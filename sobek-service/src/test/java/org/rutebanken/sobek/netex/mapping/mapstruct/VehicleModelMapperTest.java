package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.entur.netex2.model.VehicleModel;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.VehicleModelId;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sobek keeps the vehicle model in the vehicle type: the NeTEx vehicle model is derived from the type on export.
 */
class VehicleModelMapperTest {

    private final VehicleModelMapper mapper = new VehicleModelMapper();

    private static VehicleType typeWithModelData() {
        VehicleType type = new VehicleType();
        type.setNetexId("NMR:VehicleType:12");
        type.setVersion(3);
        type.setName(new EmbeddableMultilingualString("Flirt", "en"));
        type.setManufacturer(new EmbeddableMultilingualString("Stadler", "en"));
        type.setRange(new BigDecimal("500.00"));
        type.setFullCharge(new BigDecimal("100.00"));
        return type;
    }

    @Test
    void theModelOfATypeIsDerivedFromIt() {
        Optional<VehicleModel> model = mapper.mapToNetex(typeWithModelData());

        assertFalse(model.isEmpty());
        assertEquals("NMR:VehicleModel:12", model.get().getId());
        assertEquals("3", model.get().getVersion());
        assertEquals(new BigDecimal("500.00"), model.get().getRange());
        assertEquals(new BigDecimal("100.00"), model.get().getFullCharge());
        assertNotNull(model.get().getManufacturer());
        assertEquals("Stadler", model.get().getManufacturer().getContent().getFirst());
        assertEquals("NMR:VehicleType:12", model.get().getTransportTypeRef().getValue().getRef());
    }

    @Test
    void aTypeWithoutModelDataHasNoModel() {
        VehicleType type = new VehicleType();
        type.setNetexId("NMR:VehicleType:12");
        type.setName(new EmbeddableMultilingualString("Flirt", "en"));

        assertEquals(Optional.empty(), mapper.mapToNetex(type));
    }

    @Test
    void anyOfTheModelDataIsEnoughForAModel() {
        VehicleType type = new VehicleType();
        type.setNetexId("CVT:VehicleType:7");
        type.setRange(new BigDecimal("80"));

        assertEquals("CVT:VehicleModel:7", mapper.mapToNetex(type).orElseThrow().getId());
    }

    @Test
    void modelIdIsDerivedFromTheIdOfTheType() {
        assertEquals("NMR:VehicleModel:12", VehicleModelId.forVehicleType("NMR:VehicleType:12"));
        assertEquals("NMR:VehicleModel:5", VehicleModelId.forVehicleType("NMR:Train:5"));
        assertThrows(IllegalArgumentException.class, () -> VehicleModelId.forVehicleType("12"));
        assertThrows(IllegalArgumentException.class, () -> VehicleModelId.forVehicleType(null));
    }
}
