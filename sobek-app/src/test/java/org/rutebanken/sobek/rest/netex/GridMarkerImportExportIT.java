package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Grid markers are carried in the Extensions of a PassengerSpace, in Sobek's own namespace. A marker puts a label
 * on a cell of the deck's grid (a row and a column of the same deck), also for a room that has no spots.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class GridMarkerImportExportIT {

    private static final String GM_NS = "urn:entur:sobek:ext:gridmarkers:1.0";
    private static final String NETEX_NS = "http://www.netex.org.uk/netex";

    @LocalServerPort int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Grid markers on a room are kept, with their cell and label, through import and export")
    void gridMarkersSurviveImportAndExport() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String deckPlanId = "AKT:DeckPlan:run" + run;
        String toilet = "AKT:PassengerSpace:run" + run + "toilet";
        String silent = "AKT:PassengerSpace:run" + run + "silent";

        given()
                .contentType(ContentType.XML)
                .body(fixture("/fixtures/deck-plan-grid-markers.xml").replace("@RUN@", run))
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(200);

        String exportXml = given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", SingleOrganisationAuthorizationConfiguration.DATA_OWNER)
                .when()
                .get("/services/vehicles/netex/deckplans/" + deckPlanId)
                .then()
                .statusCode(200)
                .extract().body().asString();

        Document export = parse(exportXml);

        // Each room has its own markers, in the order they came in
        List<Element> toiletMarkers = markersOf(export, toilet);
        assertThat(toiletMarkers).extracting(m -> label(m)).containsExactly("WC", "WC rullestol");
        assertThat(toiletMarkers).extracting(m -> m.getAttribute("spotRowRef"))
                .containsExactly("AKT:SpotRow:run" + run + "r1", "AKT:SpotRow:run" + run + "r2");
        assertThat(toiletMarkers).extracting(m -> m.getAttribute("spotColumnRef"))
                .containsExactly("AKT:SpotColumn:run" + run + "c1", "AKT:SpotColumn:run" + run + "c1");

        List<Element> silentMarkers = markersOf(export, silent);
        assertThat(silentMarkers).extracting(m -> label(m)).containsExactly("Stillevogn");
        assertThat(silentMarkers.getFirst().getAttribute("spotColumnRef")).isEqualTo("AKT:SpotColumn:run" + run + "c2");
    }

    @Test
    @DisplayName("Importing the deck plan again makes a new version that still has the grid markers")
    void gridMarkersSurviveANewVersion() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture("/fixtures/deck-plan-grid-markers.xml").replace("@RUN@", run);

        for (int i = 0; i < 2; i++) {
            given().contentType(ContentType.XML).body(xml)
                    .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                    .then().statusCode(200);
        }

        Document export = parse(given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", SingleOrganisationAuthorizationConfiguration.DATA_OWNER)
                .when().get("/services/vehicles/netex/deckplans/AKT:DeckPlan:run" + run)
                .then().statusCode(200)
                .extract().body().asString());

        List<Element> markers = markersOf(export, "AKT:PassengerSpace:run" + run + "toilet");
        assertThat(markers).extracting(m -> label(m)).containsExactly("WC", "WC rullestol");
        assertThat(markers).extracting(m -> m.getAttribute("spotRowRef"))
                .containsExactly("AKT:SpotRow:run" + run + "r1", "AKT:SpotRow:run" + run + "r2");
    }

    @Test
    @DisplayName("A grid marker that refers to a row that is not in the deck is rejected")
    void gridMarkerWithUnknownRowIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture("/fixtures/deck-plan-grid-markers.xml").replace("@RUN@", run)
                .replace("spotRowRef=\"AKT:SpotRow:run" + run + "r1\"", "spotRowRef=\"AKT:SpotRow:run" + run + "missing\"");

        String body = given().contentType(ContentType.XML).body(xml)
                .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then().statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("AKT:SpotRow:run" + run + "missing");
    }

    private static String label(Element marker) {
        return marker.getElementsByTagNameNS(GM_NS, "Label").item(0).getTextContent();
    }

    private static List<Element> markersOf(Document export, String passengerSpaceId) {
        List<Element> markers = new ArrayList<>();
        NodeList spaces = export.getElementsByTagNameNS(NETEX_NS, "PassengerSpace");
        for (int i = 0; i < spaces.getLength(); i++) {
            Element space = (Element) spaces.item(i);
            if (passengerSpaceId.equals(space.getAttribute("id"))) {
                NodeList found = space.getElementsByTagNameNS(GM_NS, "GridMarker");
                for (int j = 0; j < found.getLength(); j++) {
                    markers.add((Element) found.item(j));
                }
            }
        }
        return markers;
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String fixture(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
