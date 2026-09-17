package org.rutebanken.sobek.netex.mapping.mapstruct;

import net.opengis.gml._3.DirectPositionType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = SobekMapperConfig.class)
public abstract class SimplePointMapper {

    private static final Logger logger = LoggerFactory.getLogger(SimplePointMapper.class);

    @Autowired
    private GeometryFactory geometryFactory;

    /**
     * Converts a JTS Point to NeTEx SimplePoint_VersionStructure
     */
    public SimplePoint_VersionStructure pointToSimplePoint(Point point, @Context MappingContext context) {
        if (point == null) {
            return null;
        }

        return new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure()
                        .withPos(new DirectPositionType().withValue(point.getX(), point.getY())));
    }

    /**
     * Converts a NeTEx SimplePoint_VersionStructure to JTS Point
     */
    public Point simplePointToPoint(SimplePoint_VersionStructure simplePoint, @Context MappingContext context) {
        if (simplePoint == null || simplePoint.getLocation() == null) {
            return null;
        }

        // Throw error if the format is wrong
        if (hasLongLat(simplePoint)) {
            throw new NetexMappingException("Positions in the vehicle registry are not allowed to have long/lat values, use pos instead.");
        }

        // If no pos is set, treat it as "no data"
        if (noPosSet(simplePoint)) {
            logger.warn("Could not find pos from location: {}", simplePoint.getLocation());
            return null;
        }

        List<Double> values = simplePoint.getLocation().getPos().getValue();
        if (values.size() < 2) {
            logger.warn("Pos list does not contain 2 or more coordinates: {}", simplePoint);
            return null;
        }

        return geometryFactory.createPoint(new Coordinate(values.get(0), values.get(1)));
    }

    private boolean hasLongLat(SimplePoint_VersionStructure simplePoint) {
        return simplePoint.getLocation().getLongitude() != null
                || simplePoint.getLocation().getLatitude() != null;
    }

    private boolean noPosSet(SimplePoint_VersionStructure simplePoint) {
        return simplePoint.getLocation().getPos() == null || simplePoint.getLocation().getPos().getValue() == null;
    }
}