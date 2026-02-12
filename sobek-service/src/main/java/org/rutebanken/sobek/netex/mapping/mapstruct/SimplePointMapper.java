package org.rutebanken.sobek.netex.mapping.mapstruct;

import com.google.common.base.Strings;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CRSAuthorityFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(config = SobekMapperConfig.class)
public abstract class SimplePointMapper {

    private static final Logger logger = LoggerFactory.getLogger(SimplePointMapper.class);

    private static final int SCALE = 6;

    private static final CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);

    @Autowired
    private GeometryFactory geometryFactory;

    private CoordinateReferenceSystem epsg4326;

    private String internalSrsName;

    @Autowired
    public void initializeCoordinateSystem(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.internalSrsName = "EPSG:" + geometryFactory.getSRID();
        try {
            this.epsg4326 = factory.createCoordinateReferenceSystem(internalSrsName);
        } catch (FactoryException e) {
            logger.warn("Cannot create coordinatereferenceSystem {}", internalSrsName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a JTS Point to NeTEx SimplePoint_VersionStructure
     */
    public SimplePoint_VersionStructure pointToSimplePoint(Point point, @Context MappingContext context) {
        if (point == null) {
            return null;
        }

        BigDecimal longitude = round(BigDecimal.valueOf(point.getX()));
        BigDecimal latitude = round(BigDecimal.valueOf(point.getY()));

        return new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure()
                        .withLongitude(longitude)
                        .withLatitude(latitude));
    }

    /**
     * Converts a NeTEx SimplePoint_VersionStructure to JTS Point
     */
    public Point simplePointToPoint(SimplePoint_VersionStructure simplePoint, @Context MappingContext context) {
        if (simplePoint == null || simplePoint.getLocation() == null) {
            return null;
        }

        if (noCoordinatesSet(simplePoint)) {
            logger.warn("Could not find long/lat or pos from location: {}", simplePoint.getLocation());
            return null;
        }

        if (hasLongLat(simplePoint)) {
            logger.debug("Detected longitude and latitude: {}", simplePoint);
            String sourceSrsName = simplePoint.getLocation().getSrsName();
            Coordinate coordinate = convertAndRoundLongLat(simplePoint);
            return transformIfDifferentSrs(coordinate, sourceSrsName);

        } else if (simplePoint.getLocation().getPos() != null) {
            logger.debug("Detected pos value: {}", simplePoint);
            String sourceSrsName = simplePoint.getLocation().getPos().getSrsName();

            List<Double> values = simplePoint.getLocation().getPos().getValue();
            if (values.size() < 2) {
                logger.warn("Pos list does not contain 2 or more coordinates: {}", simplePoint);
                return null;
            }

            Coordinate coordinate = new Coordinate(values.get(1), values.get(0));
            return transformIfDifferentSrs(coordinate, sourceSrsName);
        }

        return null;
    }

    private Point transformIfDifferentSrs(Coordinate coordinate, String sourceSrsName) {
        if (Strings.isNullOrEmpty(sourceSrsName)) {
            logger.debug("SRS is null or empty. Assuming {}: {}", geometryFactory.getSRID(), sourceSrsName);
        } else if (!sourceSrsName.equals(internalSrsName)) {
            Coordinate transformed = transform(coordinate, sourceSrsName);
            if (transformed == null) {
                return null;
            } else {
                return geometryFactory.createPoint(transformed);
            }
        }
        return geometryFactory.createPoint(coordinate);
    }

    private Coordinate transform(Coordinate source, String srsName) {
        try {
            logger.debug("Transforming {} from {}", source, srsName);
            CoordinateReferenceSystem fromCoordinateReferenceSystem = factory.createCoordinateReferenceSystem(srsName);
            MathTransform transform = CRS.findMathTransform(fromCoordinateReferenceSystem, epsg4326);
            Coordinate destination = JTS.transform(source, null, transform);
            logger.debug("Transformed {} into {}", source, destination);
            return destination;

        } catch (TransformException | FactoryException e) {
            logger.warn("Cannot transform coordinate {} to internal coordinate reference system", source, e);
            // Do not return coordinate without transformation
            return null;
        }
    }

    private boolean hasLongLat(SimplePoint_VersionStructure simplePoint) {
        return simplePoint.getLocation().getLongitude() != null
                && simplePoint.getLocation().getLatitude() != null;
    }

    private boolean noCoordinatesSet(SimplePoint_VersionStructure simplePoint) {
        return simplePoint.getLocation().getLongitude() == null
                && simplePoint.getLocation().getLatitude() == null
                && simplePoint.getLocation().getPos() == null;
    }

    private Coordinate convertAndRoundLongLat(SimplePoint_VersionStructure simplePoint) {
        logger.debug("Converting point {}", simplePoint);

        return new Coordinate(
                round(simplePoint.getLocation().getLongitude()).doubleValue(),
                round(simplePoint.getLocation().getLatitude()).doubleValue());
    }

    private BigDecimal round(BigDecimal bigDecimal) {
        return bigDecimal.setScale(SCALE, RoundingMode.HALF_UP);
    }
}