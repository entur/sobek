package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.SpotColumn;

public class SpotColumnMapper extends CustomMapper<SpotColumn, org.rutebanken.sobek.model.vehicle.SpotColumn> {

    @Override
    public void mapAtoB(SpotColumn netexSpotColumn, org.rutebanken.sobek.model.vehicle.SpotColumn sobekSpotColumn, MappingContext context) {
        super.mapAtoB(netexSpotColumn, sobekSpotColumn, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.SpotColumn sobekSpotColumn, SpotColumn netexSpotColumn, MappingContext context) {
        super.mapBtoA(sobekSpotColumn, netexSpotColumn, context);
    }
}
