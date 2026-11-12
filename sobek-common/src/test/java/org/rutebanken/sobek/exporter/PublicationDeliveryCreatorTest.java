package org.rutebanken.sobek.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import org.entur.netex2.model.Fleet;
import org.entur.netex2.model.KeyListStructure;
import org.entur.netex2.model.KeyValueStructure;
import org.entur.netex2.model.OperatorRefStructure;
import org.entur.netex2.model.Fleets_RelStructure;
import org.entur.netex2.model.MobilityServiceFrame;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.PublicationDeliveryStructure;
import org.entur.netex2.model.ResourceFrame;
import org.entur.netex2.model.TransportTypeRefStructure;
import org.entur.netex2.model.TransportTypeRefs_RelStructure;
import org.entur.netex2.model.VehicleRefStructure;
import org.entur.netex2.model.VehicleType;
import org.entur.netex2.model.VehicleTypesInFrame_RelStructure;
import org.entur.netex2.model.Vehicle;
import org.entur.netex2.model.Vehicles_RelStructure;
import org.entur.netex2.model.VehiclesInFrame_RelStructure;
import org.entur.netex2.validation.NeTExValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.netex.id.ValidPrefixList;

class PublicationDeliveryCreatorTest {

    private final PublicationDeliveryCreator creator = new PublicationDeliveryCreator("2.0:NO-NeTEx:2.0", new ValidPrefixList("NMR", java.util.List.of()));

    @Test
    @DisplayName("A delivery of a resource frame and a mobility service frame has both frames")
    void deliveryHasBothFrames() {
        PublicationDeliveryStructure delivery = creator.createPublicationDelivery(
                new ResourceFrame().withId("RF:1"), new MobilityServiceFrame().withId("MSF:1"));

        assertThat(delivery.getDataObjects().getCompositeFrameOrCommonFrame())
                .extracting(JAXBElement::getValue)
                .hasSize(2)
                .hasAtLeastOneElementOfType(ResourceFrame.class)
                .hasAtLeastOneElementOfType(MobilityServiceFrame.class);

        // And they are the frames that were passed in, not copies or one frame twice
        assertThat(delivery.getDataObjects().getCompositeFrameOrCommonFrame())
                .extracting(frame -> frame.getValue().getId())
                .containsExactlyInAnyOrder("RF:1", "MSF:1");
    }

    @Test
    @DisplayName("Without a mobility service frame, the delivery has the resource frame only")
    void deliveryWithoutMobilityServiceFrame() {
        PublicationDeliveryStructure delivery = creator.createPublicationDelivery(new ResourceFrame().withId("RF:1"), (MobilityServiceFrame) null);

        assertThat(delivery.getDataObjects().getCompositeFrameOrCommonFrame()).extracting(JAXBElement::getValue)
                .hasSize(1).hasOnlyElementsOfType(ResourceFrame.class);
    }

    @Test
    @DisplayName("A delivery of a resource frame and a mobility service frame with a fleet is valid NeTEx when serialized")
    void deliveryWithFleetIsValidAgainstTheSchema() throws Exception {
        ObjectFactory objectFactory = new ObjectFactory();
        Fleet fleet = new Fleet()
                .withId("AKT:Fleet:1")
                .withVersion("1")
                .withName(new org.entur.netex2.model.MultilingualString().withContent("City buses"))
                .withMembers(new Vehicles_RelStructure().withVehicleRefOrVehicle(
                        objectFactory.createVehicleRef(new VehicleRefStructure().withRef("AKT:Vehicle:1").withVersion("1")),
                        objectFactory.createVehicleRef(new VehicleRefStructure().withRef("AKT:Vehicle:2").withVersion("1"))))
                // The organisation is not in the delivery, and the reference has no version, so the schema accepts it
                .withTransportOrganisationRef(objectFactory.createOperatorRef(new OperatorRefStructure().withRef("NOG:GeneralOrganisation:1")))
                .withKeyList(new KeyListStructure().withKeyValue(new KeyValueStructure().withKey("contract-number").withValue("C-2026-17")))
                .withTransportTypes(new TransportTypeRefs_RelStructure().withTransportTypeRef(
                        objectFactory.createTransportTypeRef(new TransportTypeRefStructure().withRef("AKT:VehicleType:1").withVersion("1"))));
        // The schema has key references: the vehicles and the vehicle type that the fleet refers to must be in the delivery
        ResourceFrame resourceFrame = new ResourceFrame().withId("RF:1").withVersion("1")
                .withVehicleTypes(new VehicleTypesInFrame_RelStructure().withTransportType(
                        objectFactory.createVehicleType(new VehicleType().withId("AKT:VehicleType:1").withVersion("1"))))
                .withVehicles(new VehiclesInFrame_RelStructure().withVehicle(
                        vehicle(objectFactory, "AKT:Vehicle:1"), vehicle(objectFactory, "AKT:Vehicle:2")));
        PublicationDeliveryStructure delivery = creator.createPublicationDelivery(
                resourceFrame,
                new MobilityServiceFrame().withId("MSF:1").withVersion("1").withFleets(new Fleets_RelStructure().withFleet(fleet)));

        Marshaller marshaller = JAXBContext.newInstance(PublicationDeliveryStructure.class).createMarshaller();
        marshaller.setSchema(NeTExValidator.getNeTExValidator().getSchema());
        StringWriter xml = new StringWriter();

        // The marshaller throws on the first schema violation
        marshaller.marshal(objectFactory.createPublicationDelivery(delivery), xml);

        assertThat(xml.toString())
                .contains("<ResourceFrame", "<MobilityServiceFrame", "<Fleet ")
                // The order of the attributes is up to the serializer
                .contains("id=\"AKT:Fleet:1\"", "<members>", "<transportTypes>")
                .containsOnlyOnce("<VehicleRef ref=\"AKT:Vehicle:1\"")
                .containsOnlyOnce("<VehicleRef ref=\"AKT:Vehicle:2\"")
                .contains("<OperatorRef ref=\"NOG:GeneralOrganisation:1\"")
                .contains("<Key>contract-number</Key>", "<Value>C-2026-17</Value>");
    }

    @Test
    @DisplayName("An OperatorRef with a version needs the Operator in the same delivery, which is why the fleet's is written without one")
    void operatorRefWithVersionIsRefusedWithoutTheOperator() throws Exception {
        ObjectFactory objectFactory = new ObjectFactory();
        Fleet fleet = new Fleet()
                .withId("AKT:Fleet:1")
                .withVersion("1")
                .withTransportOrganisationRef(objectFactory.createOperatorRef(
                        new OperatorRefStructure().withRef("NOG:GeneralOrganisation:1").withVersion("1")));
        PublicationDeliveryStructure delivery = creator.createPublicationDelivery(
                new ResourceFrame().withId("RF:1").withVersion("1"),
                new MobilityServiceFrame().withId("MSF:1").withVersion("1").withFleets(new Fleets_RelStructure().withFleet(fleet)));

        Marshaller marshaller = JAXBContext.newInstance(PublicationDeliveryStructure.class).createMarshaller();
        marshaller.setSchema(NeTExValidator.getNeTExValidator().getSchema());

        assertThatThrownBy(() -> marshaller.marshal(objectFactory.createPublicationDelivery(delivery), new StringWriter()))
                // The schema's message is the cause of the marshalling exception
                .hasStackTraceContaining("Operator_KeyRef");
    }

    private static Vehicle vehicle(ObjectFactory objectFactory, String id) {
        return new Vehicle().withId(id).withVersion("1")
                .withTransportTypeRef(objectFactory.createTransportTypeRef(
                        new TransportTypeRefStructure().withRef("AKT:VehicleType:1").withVersion("1")));
    }
}
