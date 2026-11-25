package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.TransportTypeRefStructure;
import org.entur.netex2.model.Vehicle;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class VehicleMapperTest extends DataManagedObjectStructureMapperTestBase<
    org.entur.netex2.model.Vehicle,
    org.rutebanken.sobek.model.vehicle.Vehicle> {

    @Autowired
    private VehicleMapper mapper;

    private final ObjectFactory objectFactory = new ObjectFactory();
    @Autowired
    private TemporalTypeMapper temporalTypeMapper;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;
    @Autowired
    private NetexIdHelper  netexIdHelper;
    @Autowired
    private ValidPrefixList validPrefixList;
    @Autowired
    private DataManagedObjectStructureMapper dataManagedObjectStructureMapper;
    @Autowired
    private OwnedEntityMapper ownedEntityMapper;

    protected VehicleMapperTest() {
        super(org.entur.netex2.model.Vehicle.class, org.rutebanken.sobek.model.vehicle.Vehicle.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.Vehicle mapToSobek(org.entur.netex2.model.Vehicle source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.entur.netex2.model.Vehicle mapToNetex(org.rutebanken.sobek.model.vehicle.Vehicle source) {
        return mapper.mapToNetex(source, context);
    }

    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    // The mapping context is shared by the tests of the module: put back what this test changes, see FleetMapperTest
    private ReferenceResolver originalReferenceResolver;
    private String originalDataOwnerRef;

    @BeforeEach
    public void setUp() {
        originalReferenceResolver = context.getReferenceResolver();
        originalDataOwnerRef = context.getDataOwnerRef();
        // Create mock resolver
        VehicleType mockVehicleType = new VehicleType();
        mockVehicleType.setNetexId("NMR:VehicleType:1");
        mockVehicleType.setDataOwnerRef("NOG:Authority:1");

        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        when(referenceResolver.resolve(any(),any(),eq(VehicleType.class))).thenReturn(mockVehicleType);
        context.setReferenceResolver(referenceResolver);
        context.setKeyListStructureMapper(keyListStructureMapper);
        context.setNetexIdHelper(netexIdHelper);
        context.setValidPrefixList(validPrefixList);
        context.setDataManagedObjectStructureMapper(dataManagedObjectStructureMapper);
        context.setOwnedEntityMapper(ownedEntityMapper);
        context.setDataOwnerRef("NOG:Authority:1");
    }

    @AfterEach
    void restoreContext() {
        context.setReferenceResolver(originalReferenceResolver);
        context.setDataOwnerRef(originalDataOwnerRef);
    }

    @Test
    void testMapToSobek() {
        // Given
        Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("NMR:Vehicle:123");
        netexVehicle.setVersion("1");
        netexVehicle.setRegistrationNumber("ABC123");
        netexVehicle.setChassisNumber("CHASSIS123");
        netexVehicle.setOperationalNumber("OP456");
        netexVehicle.setRegistrationDate(LocalDate.of(2023, 1, 15));

        // Transport Type Reference (VehicleType)
        TransportTypeRefStructure transportTypeRef = new TransportTypeRefStructure();
        transportTypeRef.setRef("NMR:VehicleType:1");
        JAXBElement<TransportTypeRefStructure> jaxbTransportType =
                objectFactory.createTransportTypeRef(transportTypeRef);
        netexVehicle.setTransportTypeRef(jaxbTransportType);

        // When
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                mapper.mapToSobek(netexVehicle, context);

        // Then
        assertNotNull(sobekVehicle);
        assertEquals(1, sobekVehicle.getVersion());
        assertEquals("ABC123", sobekVehicle.getRegistrationNumber());
        assertEquals("CHASSIS123", sobekVehicle.getChassisNumber());
        assertEquals("OP456", sobekVehicle.getOperationalNumber());
        assertEquals(temporalTypeMapper.localDateTimeToInstant(LocalDateTime.of(2023, 1, 15, 0, 0)), sobekVehicle.getRegistrationDate());
        assertEquals("NOG:Authority:1", sobekVehicle.getDataOwnerRef());

        // Check that references are stored in transient fields
        assertNotNull(sobekVehicle.getTransportType());
        assertEquals("NMR:VehicleType:1", sobekVehicle.getTransportType().getNetexId());
        assertEquals("NOG:Authority:1", sobekVehicle.getTransportType().getDataOwnerRef());
    }

    @Test
    void testMapToSobekWithResolvers() {
        // Given
        Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("NMR:Vehicle:123");
        netexVehicle.setVersion("1");
        netexVehicle.setRegistrationNumber("ABC123");

        TransportTypeRefStructure transportTypeRef = new TransportTypeRefStructure();
        transportTypeRef.setRef("NMR:VehicleType:1");
        JAXBElement<TransportTypeRefStructure> jaxbTransportType =
                objectFactory.createTransportTypeRef(transportTypeRef);
        netexVehicle.setTransportTypeRef(jaxbTransportType);

        // When
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                mapper.mapToSobek(netexVehicle, context);

        // Then
        assertNotNull(sobekVehicle.getTransportType());
        assertEquals("NMR:VehicleType:1", sobekVehicle.getTransportType().getNetexId());
    }

    @Test
    void testMapToNetex() {
        // Given
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setVersion(1);
        sobekVehicle.setRegistrationNumber("ABC123");
        sobekVehicle.setChassisNumber("CHASSIS123");
        sobekVehicle.setOperationalNumber("OP456");

        // Set up VehicleType with incomingId
        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId("NMR:VehicleType:1");
        sobekVehicle.setTransportType(vehicleType);

        // The model of the vehicle is the one of its type
        vehicleType.setManufacturer(new EmbeddableMultilingualString("Stadler", "en"));

        // When
        Vehicle netexVehicle = mapper.mapToNetex(sobekVehicle, context);

        // Then
        assertNotNull(netexVehicle);
        assertEquals("1", netexVehicle.getVersion());
        assertEquals("ABC123", netexVehicle.getRegistrationNumber());
        assertEquals("CHASSIS123", netexVehicle.getChassisNumber());
        assertEquals("OP456", netexVehicle.getOperationalNumber());

        // Check TransportTypeRef is wrapped in JAXBElement
        assertNotNull(netexVehicle.getTransportTypeRef());
        assertEquals("NMR:VehicleType:1", netexVehicle.getTransportTypeRef().getValue().getRef());

        // Check VehicleModelRef
        assertNotNull(netexVehicle.getVehicleModelRef());
        assertEquals("NMR:VehicleModel:1", netexVehicle.getVehicleModelRef().getRef());
    }

    @Test
    void testMapToNetexWithTransientReferences() {
        // Given - Vehicle with only transient references (entities not loaded)
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setVersion(1);
        sobekVehicle.setRegistrationNumber("ABC123");

        // Set transient references
        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId("NMR:VehicleType:1");
        sobekVehicle.setTransportType(vehicleType);

        vehicleType.setRange(new BigDecimal("500"));

        // When
        Vehicle netexVehicle = mapper.mapToNetex(sobekVehicle, context);

        // Then
        assertNotNull(netexVehicle.getTransportTypeRef());
        assertEquals("NMR:VehicleType:1", netexVehicle.getTransportTypeRef().getValue().getRef());

        assertNotNull(netexVehicle.getVehicleModelRef());
        assertEquals("NMR:VehicleModel:1", netexVehicle.getVehicleModelRef().getRef());
    }

    @Test
    void testMapToNetexWithoutVehicleModelDataHasNoVehicleModelRef() {
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setVersion(1);
        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId("NMR:VehicleType:1");
        sobekVehicle.setTransportType(vehicleType);

        Vehicle netexVehicle = mapper.mapToNetex(sobekVehicle, context);

        assertNotNull(netexVehicle.getTransportTypeRef());
        assertNull(netexVehicle.getVehicleModelRef());
    }

    @Test
    void testBidirectionalMapping() {
        // Given
        org.rutebanken.sobek.model.vehicle.Vehicle originalSobek =
                new org.rutebanken.sobek.model.vehicle.Vehicle();
        originalSobek.setVersion(1);
        originalSobek.setRegistrationNumber("ABC123");
        originalSobek.setChassisNumber("CHASSIS123");

        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId("NMR:VehicleType:1");
        originalSobek.setTransportType(vehicleType);

        // When - Round trip mapping
        Vehicle netex = mapper.mapToNetex(originalSobek, context);
        org.rutebanken.sobek.model.vehicle.Vehicle backToSobek =
                mapper.mapToSobek(netex, context);

        // Then
        assertEquals(originalSobek.getVersion(), backToSobek.getVersion());
        assertEquals(originalSobek.getRegistrationNumber(), backToSobek.getRegistrationNumber());
        assertEquals(originalSobek.getChassisNumber(), backToSobek.getChassisNumber());

        // References are preserved
        assertNotNull(backToSobek.getTransportType());
        assertEquals("NMR:VehicleType:1", backToSobek.getTransportType().getNetexId());
    }
}