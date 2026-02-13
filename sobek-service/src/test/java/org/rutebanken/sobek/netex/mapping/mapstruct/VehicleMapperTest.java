package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.TransportTypeRefStructure;
import org.rutebanken.netex.model.Vehicle;
import org.rutebanken.netex.model.VehicleModelRefStructure;
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class VehicleMapperTest {
    private MappingContext mappingContext;

    @Autowired
    private VehicleMapper mapper;

    private final ObjectFactory objectFactory = new ObjectFactory();
    @Autowired
    private TemporalTypeMapper temporalTypeMapper;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;

    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    @BeforeEach
    public void setUp() {
        // Create mock resolver
        VehicleModel mockVehicleModel = new VehicleModel();
        mockVehicleModel.setNetexId("VM:Model:1");

        VehicleType mockVehicleType = new VehicleType();
        mockVehicleType.setNetexId("VT:Bus:1");

        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        when(referenceResolver.resolve(any(),any(),eq(VehicleType.class))).thenReturn(mockVehicleType);
        when(referenceResolver.resolve(any(),any(),eq(VehicleModel.class))).thenReturn(mockVehicleModel);
        mappingContext = new MappingContext();
        mappingContext.setReferenceResolver(referenceResolver);
        mappingContext.setKeyListStructureMapper(keyListStructureMapper);
    }

    @Test
    void testMapToSobek() {
        // Given
        Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("V:123");
        netexVehicle.setVersion("1");
        netexVehicle.setRegistrationNumber("ABC123");
        netexVehicle.setChassisNumber("CHASSIS123");
        netexVehicle.setOperationalNumber("OP456");
        netexVehicle.setRegistrationDate(LocalDateTime.of(2023, 1, 15, 0, 0));

        // Vehicle Model Reference
        VehicleModelRefStructure vehicleModelRef = new VehicleModelRefStructure();
        vehicleModelRef.setRef("VM:Model:1");
        netexVehicle.setVehicleModelRef(vehicleModelRef);

        // Transport Type Reference (VehicleType)
        TransportTypeRefStructure transportTypeRef = new TransportTypeRefStructure();
        transportTypeRef.setRef("VT:Bus:1");
        JAXBElement<TransportTypeRefStructure> jaxbTransportType =
                objectFactory.createTransportTypeRef(transportTypeRef);
        netexVehicle.setTransportTypeRef(jaxbTransportType);

        // When
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                mapper.mapToSobek(netexVehicle, mappingContext);

        // Then
        assertNotNull(sobekVehicle);
        assertEquals(1, sobekVehicle.getVersion());
        assertEquals("ABC123", sobekVehicle.getRegistrationNumber());
        assertEquals("CHASSIS123", sobekVehicle.getChassisNumber());
        assertEquals("OP456", sobekVehicle.getOperationalNumber());
        assertEquals(temporalTypeMapper.localDateTimeToInstant(LocalDateTime.of(2023, 1, 15, 0, 0)), sobekVehicle.getRegistrationDate());

        // Check that references are stored in transient fields
        assertNotNull(sobekVehicle.getVehicleModel());
        assertEquals("VM:Model:1", sobekVehicle.getVehicleModel().getNetexId());

        assertNotNull(sobekVehicle.getTransportType());
        assertEquals("VT:Bus:1", sobekVehicle.getTransportType().getNetexId());
    }

    @Test
    void testMapToSobekWithResolvers() {
        // Given
        Vehicle netexVehicle = new Vehicle();
        netexVehicle.setId("V:123");
        netexVehicle.setVersion("1");
        netexVehicle.setRegistrationNumber("ABC123");

        VehicleModelRefStructure vehicleModelRef = new VehicleModelRefStructure();
        vehicleModelRef.setRef("VM:Model:1");
        netexVehicle.setVehicleModelRef(vehicleModelRef);

        TransportTypeRefStructure transportTypeRef = new TransportTypeRefStructure();
        transportTypeRef.setRef("VT:Bus:1");
        JAXBElement<TransportTypeRefStructure> jaxbTransportType =
                objectFactory.createTransportTypeRef(transportTypeRef);
        netexVehicle.setTransportTypeRef(jaxbTransportType);

        // When
        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle =
                mapper.mapToSobek(netexVehicle, mappingContext);

        // Then
        assertNotNull(sobekVehicle.getVehicleModel());
        assertEquals("VM:Model:1", sobekVehicle.getVehicleModel().getNetexId());

        assertNotNull(sobekVehicle.getTransportType());
        assertEquals("VT:Bus:1", sobekVehicle.getTransportType().getNetexId());
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
        vehicleType.setNetexId("VT:Bus:1");
        sobekVehicle.setTransportType(vehicleType);

        // Set up VehicleModel with incomingId
        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setNetexId("VM:Model:1");
        sobekVehicle.setVehicleModel(vehicleModel);

        // When
        Vehicle netexVehicle = mapper.mapToNetex(sobekVehicle, mappingContext);

        // Then
        assertNotNull(netexVehicle);
        assertEquals("1", netexVehicle.getVersion());
        assertEquals("ABC123", netexVehicle.getRegistrationNumber());
        assertEquals("CHASSIS123", netexVehicle.getChassisNumber());
        assertEquals("OP456", netexVehicle.getOperationalNumber());

        // Check TransportTypeRef is wrapped in JAXBElement
        assertNotNull(netexVehicle.getTransportTypeRef());
        assertEquals("VT:Bus:1", netexVehicle.getTransportTypeRef().getValue().getRef());

        // Check VehicleModelRef
        assertNotNull(netexVehicle.getVehicleModelRef());
        assertEquals("VM:Model:1", netexVehicle.getVehicleModelRef().getRef());
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
        vehicleType.setNetexId("VT:Bus:1");
        sobekVehicle.setTransportType(vehicleType);

        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setNetexId("VM:Model:1");
        sobekVehicle.setVehicleModel(vehicleModel);

        // When
        Vehicle netexVehicle = mapper.mapToNetex(sobekVehicle, mappingContext);

        // Then
        assertNotNull(netexVehicle.getTransportTypeRef());
        assertEquals("VT:Bus:1", netexVehicle.getTransportTypeRef().getValue().getRef());

        assertNotNull(netexVehicle.getVehicleModelRef());
        assertEquals("VM:Model:1", netexVehicle.getVehicleModelRef().getRef());
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
        vehicleType.setNetexId("VT:Bus:1");
        originalSobek.setTransportType(vehicleType);

        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setNetexId("VM:Model:1");
        originalSobek.setVehicleModel(vehicleModel);

        // When - Round trip mapping
        Vehicle netex = mapper.mapToNetex(originalSobek, mappingContext);
        org.rutebanken.sobek.model.vehicle.Vehicle backToSobek =
                mapper.mapToSobek(netex, mappingContext);

        // Then
        assertEquals(originalSobek.getVersion(), backToSobek.getVersion());
        assertEquals(originalSobek.getRegistrationNumber(), backToSobek.getRegistrationNumber());
        assertEquals(originalSobek.getChassisNumber(), backToSobek.getChassisNumber());

        // References are preserved
        assertNotNull(backToSobek.getTransportType());
        assertEquals("VT:Bus:1", backToSobek.getTransportType().getNetexId());

        assertNotNull(backToSobek.getVehicleModel());
        assertEquals("VM:Model:1", backToSobek.getVehicleModel().getNetexId());
    }
}