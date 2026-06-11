package org.rutebanken.sobek.versioning;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;

@Mapper(
        config = SobekMapperConfig.class)
public interface VersionCopyMapper {

    @Mapping(target = "id", ignore = true)
    Vehicle copy(Vehicle source);

    @Mapping(target = "id", ignore = true)
    VehicleType copy(VehicleType source);

    @Mapping(target = "id", ignore = true)
    DeckPlan copy(DeckPlan source);
}
