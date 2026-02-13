/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.netex.mapping.mapstruct;

import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.ObjectFactory;
import net.opengis.gml._3.PolygonType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PolygonConverterTest {

    private static final net.opengis.gml._3.ObjectFactory openGisObjectFactory = new ObjectFactory();

    @Autowired
    private PolygonMapper polygonConverter;
    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    public void convertFrom() throws Exception {
        List<Double> values = new ArrayList<>();
        values.add(9.8468);
        values.add(59.2649);
        values.add(9.8456);
        values.add(59.2654);
        values.add(9.8457);
        values.add(59.2655);
        values.add(values.get(0));
        values.add(values.get(1));

        DirectPositionListType positionList = new DirectPositionListType().withValue(values);

        LinearRingType linearRing = new LinearRingType()
                .withPosList(positionList);

        PolygonType polygonType = new PolygonType()
                .withId("KVE-07")
                .withExterior(new AbstractRingPropertyType()
                        .withAbstractRing(openGisObjectFactory.createLinearRing(linearRing)));

        Polygon polygon = polygonConverter.polygonTypeToPolygon(polygonType);

        assertNotNull(polygon);
        assertInstanceOf(Polygon.class, polygon);
        assertEquals(values.size() / 2, polygon.getExteriorRing().getCoordinates().length);
        assertCoordinatesMatch(polygon.getExteriorRing(), values, "Exterior ring");
    }

    @Test
    public void convertFromWithHoles() throws Exception {
        List<Double> values = new ArrayList<>();
        values.add(9.8468);
        values.add(59.2649);
        values.add(9.8456);
        values.add(59.2654);
        values.add(9.8457);
        values.add(59.2655);
        values.add(values.get(0));
        values.add(values.get(1));

        DirectPositionListType positionList = new DirectPositionListType().withValue(values);

        LinearRingType linearRing = new LinearRingType()
                .withPosList(positionList);

        PolygonType polygonType = new PolygonType()
                .withId("KVE-07")
                .withExterior(new AbstractRingPropertyType()
                        .withAbstractRing(openGisObjectFactory.createLinearRing(linearRing)))
                .withInterior(new AbstractRingPropertyType().withAbstractRing(openGisObjectFactory.createLinearRing(linearRing)));

        Polygon polygon = polygonConverter.polygonTypeToPolygon(polygonType);

        assertNotNull(polygon);
        assertEquals(values.size() / 2, polygon.getExteriorRing().getCoordinates().length);
        assertEquals(1, polygon.getNumInteriorRing());
        assertCoordinatesMatch(polygon.getExteriorRing(), values, "Exterior ring");
        assertInteriorRingsMatch(polygon, List.of(values));
    }

    @Test
    public void convertTo() throws Exception {

        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(9.8468, 59.2649),
                new Coordinate(9.8456, 59.2654),
                new Coordinate(9.8457, 59.2655),
                new Coordinate(9.8468, 59.2649)};

        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(coordinates), geometryFactory);
        Polygon polygon = new Polygon(linearRing, null, geometryFactory);

        PolygonType actual = polygonConverter.polygonToPolygonType(polygon);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertFalse(actual.getId().isBlank());

        List<Double> values = polygonConverter.extractValues(actual.getExterior());
        assertEquals(coordinates.length * 2, values.size());


        // Sobek is storing polygons with X, Y
        // In NeTEx we receive polygons with Y, X
        // Expect Y, X when converting to PolygonType (Netex)
        int counter = 0;
        for(Coordinate coordinate : coordinates) {
            assertEquals(coordinate.y, values.get(counter++).doubleValue());
            assertEquals(coordinate.x, values.get(counter++).doubleValue());
        }
    }

    @Test
    public void convertToWithHoles() throws Exception {

        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(9.8468, 59.2649),
                new Coordinate(9.8456, 59.2654),
                new Coordinate(9.8457, 59.2655),
                new Coordinate(9.8468, 59.2649)};

        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(coordinates), geometryFactory);
        LinearRing[] holes = new LinearRing[] { new LinearRing(new CoordinateArraySequence(coordinates), geometryFactory)};
        Polygon polygon = new Polygon(linearRing, holes, geometryFactory);

        PolygonType actual = polygonConverter.polygonToPolygonType(polygon);
        assertNotNull(actual);

        List<Double> actualDoublevalues = polygonConverter.extractValues(actual.getExterior());
        assertEquals(coordinates.length * 2, actualDoublevalues.size());

        List<Double> actualHoleDoubleValues = polygonConverter.extractValues(actual.getInterior().getFirst());
        assertEquals(coordinates.length * 2, actualHoleDoubleValues.size());

    }

    private void assertCoordinatesMatch(LineString actual, List<Double> expectedExteriorValues, String description) {
        int counter = 0;
        for (Coordinate coordinate : actual.getCoordinates()) {
            assertEquals(expectedExteriorValues.get(counter++), coordinate.y, description + " x coordinate");
            assertEquals(expectedExteriorValues.get(counter++), coordinate.x, description + " y coordinate");
        }
    }

    private void assertInteriorRingsMatch(Polygon actual, List<List<Double>> expectedInteriorValues) {
        for (int interiorIndex = 0; interiorIndex < actual.getNumInteriorRing(); interiorIndex++) {
            assertCoordinatesMatch(actual.getInteriorRingN(interiorIndex), expectedInteriorValues.getFirst(), "interior ring number " + interiorIndex);
        }
    }


}