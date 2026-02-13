
package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.ObjectFactory;
import net.opengis.gml._3.PolygonType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.rutebanken.sobek.geo.DoubleValuesToCoordinateSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Mapper(config = org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig.class)
public abstract class PolygonMapper {

    private static final Logger logger = LoggerFactory.getLogger(PolygonMapper.class);

    private static final ObjectFactory openGisObjectFactory = new ObjectFactory();

    private static final AtomicLong polygonIdCounter = new AtomicLong();

    @Autowired
    protected GeometryFactory geometryFactory;

    @Autowired
    protected DoubleValuesToCoordinateSequence doubleValuesToCoordinateSequence;

    @Named("polygonTypeToPolygon")
    public Polygon polygonTypeToPolygon(PolygonType polygonType) {
        if (polygonType == null) {
            return null;
        }

        Optional<List<Double>> optionalExteriorValues = Optional.ofNullable(polygonType)
                .map(PolygonType::getExterior)
                .map(this::extractValues);

        Optional<List<List<Double>>> interiorValues = Optional.ofNullable(polygonType)
                .map(PolygonType::getInterior)
                .map(list -> list.stream()
                        .map(this::extractValues)
                        .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty());

        if (optionalExteriorValues.isPresent()) {
            List<Double> exteriorValues = optionalExteriorValues.get();

            CoordinateSequence exteriorCoordinateSequence = doubleValuesToCoordinateSequence.convert(exteriorValues);
            LinearRing exteriorLinearRing = geometryFactory.createLinearRing(exteriorCoordinateSequence);

            LinearRing[] interiorHoles = null;

            if (interiorValues.isPresent()) {
                interiorHoles = interiorValues.get().stream()
                        .map(doubleValuesToCoordinateSequence::convert)
                        .map(geometryFactory::createLinearRing)
                        .toArray(LinearRing[]::new);
            }

            return geometryFactory.createPolygon(exteriorLinearRing, interiorHoles);
        }

        logger.warn("Cannot convert polygon from PolygonType. Cannot find exterior values: {}", polygonType);
        return null;
    }

    @Named("polygonToPolygonType")
    public PolygonType polygonToPolygonType(Polygon polygon) {
        if (polygon == null) {
            return null;
        }

        Optional<Coordinate[]> optionalCoordinates = Optional.ofNullable(polygon)
                .map(Polygon::getExteriorRing)
                .map(LineString::getCoordinates)
                .filter(coordinates -> coordinates.length > 0);

        if (optionalCoordinates.isPresent()) {
            List<Double> values = toList(optionalCoordinates.get());
            return new PolygonType()
                    .withId("GEN-PolygonType-" + polygonIdCounter.incrementAndGet())
                    .withExterior(of(values))
                    .withInterior(ofInteriorRings(polygon));
        }

        return null;
    }

    protected List<Double> extractValues(AbstractRingPropertyType abstractRingPropertyType) {
        return Optional.of(abstractRingPropertyType)
                .map(AbstractRingPropertyType::getAbstractRing)
                .map(JAXBElement::getValue)
                .map(abstractRing -> ((LinearRingType) abstractRing))
                .map(LinearRingType::getPosList)
                .map(DirectPositionListType::getValue)
                .orElse(null);
    }

    protected List<AbstractRingPropertyType> ofInteriorRings(Polygon polygon) {
        List<AbstractRingPropertyType> list = new ArrayList<>();
        for (int n = 0; n < polygon.getNumInteriorRing(); n++) {
            if (polygon.getInteriorRingN(n).getCoordinates() != null) {
                List<Double> values = toList(polygon.getInteriorRingN(n).getCoordinates());
                list.add(of(values));
            }
        }
        return list;
    }

    protected AbstractRingPropertyType of(List<Double> values) {
        return new AbstractRingPropertyType()
                .withAbstractRing(openGisObjectFactory.createLinearRing(
                        new LinearRingType()
                                .withPosList(
                                        new DirectPositionListType().withValue(values))));
    }

    protected List<Double> toList(Coordinate[] coordinates) {
        List<Double> values = new ArrayList<>(coordinates.length * 2);
        for (Coordinate coordinate : coordinates) {
            values.add(coordinate.y);
            values.add(coordinate.x);
        }
        return values;
    }
}