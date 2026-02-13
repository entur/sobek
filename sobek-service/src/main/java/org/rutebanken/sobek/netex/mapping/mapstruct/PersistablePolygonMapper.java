package org.rutebanken.sobek.netex.mapping.mapstruct;

import net.opengis.gml._3.PolygonType;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.rutebanken.sobek.model.PersistablePolygon;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MapStruct mapper for converting between Polygon and PersistablePolygon.
 * Handles mapping between JTS Polygon and the persistable entity wrapper.
 */
@Mapper(config = org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig.class)
public abstract class PersistablePolygonMapper {

    @Autowired
    private PolygonMapper polygonMapper;

    /**
     * Maps from Polygon to PersistablePolygon (Sobek entity).
     */
    @Named("mapToSobekPolygon")
    public PersistablePolygon mapToSobekPolygon(PolygonType polygon, @Context MappingContext context) {
        if (polygon == null) {
            return null;
        }

        PersistablePolygon persistablePolygon = new PersistablePolygon();
        persistablePolygon.setPolygon(polygonMapper.polygonTypeToPolygon(polygon));
        return persistablePolygon;
    }

    /**
     * Maps from PersistablePolygon to Polygon.
     */
    @Named("mapToNetexPolygon")
    public PolygonType mapToNetexPolygon(PersistablePolygon persistablePolygon, @Context MappingContext context) {
        if (persistablePolygon == null) {
            return null;
        }

        return polygonMapper.polygonToPolygonType(persistablePolygon.getPolygon());
    }
}