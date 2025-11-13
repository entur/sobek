package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.SpotRow;

public class SpotRowMapper extends CustomMapper<SpotRow, org.rutebanken.sobek.model.vehicle.SpotRow> {

    @Override
    public void mapAtoB(SpotRow netexSpotRow, org.rutebanken.sobek.model.vehicle.SpotRow sobekSpotRow, MappingContext context) {
        super.mapAtoB(netexSpotRow, sobekSpotRow, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.SpotRow sobekSpotRow, SpotRow netexSpotRow, MappingContext context) {
        super.mapBtoA(sobekSpotRow, netexSpotRow, context);

        if (sobekSpotRow.getLabel() != null) {
            netexSpotRow.getLabel().withContent(sobekSpotRow.getLabel().getValue());
        }
    }
}
