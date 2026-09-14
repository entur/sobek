/*
 * Licensed under the EUPL, Version 1.2 or – as soon as they will be approved by
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

import jakarta.xml.bind.ValidationException;
import net.opengis.gml._3.DirectPositionType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimplePointVersionStructureConverterTest {

    @Autowired
    private SimplePointMapper simplePointVersionStructureConverter;

    @Autowired
    private MappingContext mappingContext;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    public void convertingLongLatShouldThrowException() {
        double longitude = 10.01;
        double latitude = 20.24;
        SimplePoint_VersionStructure simplePointversionStructure = new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure()
                        .withLongitude(BigDecimal.valueOf(longitude))
                        .withLatitude(BigDecimal.valueOf(latitude)));
        assertThrows(ValidationException.class, () ->
                        simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext),
                    "Expected RuntimeException due to using latitude and longitude, but none was thrown.");
    }

    @Test
    public void usePos() {
        double longitude = 10.123456789;
        double latitude = 20.123123123123;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        SimplePoint_VersionStructure simplePointversionStructure =  simplePointVersionStructureConverter.pointToSimplePoint(point, mappingContext);

        assertEquals(10.123456789, simplePointversionStructure.getLocation().getPos().getValue().get(0));
        assertEquals(20.123123123123, simplePointversionStructure.getLocation().getPos().getValue().get(1));
    }

    @Test
    public void parsePos2PosEnum() throws ValidationException {
        double longitude = 10.1;
        double latitude = 20.1;
        SimplePoint_VersionStructure simplePointversionStructure = new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure()
                        .withPos(new DirectPositionType().withValue(longitude, latitude)));
        Point point = simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext);

        assertEquals(10.1, point.getX());
        assertEquals(20.1, point.getY());
    }

    @Test
    public void nullCheckLocation() throws ValidationException {
        SimplePoint_VersionStructure simplePointversionStructure = new SimplePoint_VersionStructure();
        simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext);
    }

    @Test
    public void nullCheckSimplePoint() throws ValidationException {
        SimplePoint_VersionStructure simplePointversionStructure = null;
        simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext);
    }

    @Test
    public void nullCheckLatitude() {
        SimplePoint_VersionStructure simplePointversionStructure = new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure().withLongitude(BigDecimal.valueOf(10.00)));
        assertThrows(ValidationException.class, () ->
                simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext),
            "Expected RuntimeException due to using latitude and longitude, but none was thrown.");
    }

    @Test
    public void nullCheckLongitude() {
        SimplePoint_VersionStructure simplePointversionStructure = new SimplePoint_VersionStructure()
                .withLocation(new LocationStructure().withLatitude(BigDecimal.valueOf(10.00)));
        assertThrows(ValidationException.class, () ->
                simplePointVersionStructureConverter.simplePointToPoint(simplePointversionStructure, mappingContext),
            "Expected RuntimeException due to using latitude and longitude, but none was thrown.");
    }
}