package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.TransportTypeRefStructure;
import org.rutebanken.netex.model.VehicleModel;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class VehicleModelMapperTest {

    @Autowired
    private VehicleModelMapper mapper;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;

    @Mock
    ReferenceResolver referenceResolver;

    private final ObjectFactory objectFactory = new ObjectFactory();

    private MappingContext context;

    @BeforeEach
    void setUp() {
        // Create mock resolver
        org.rutebanken.sobek.model.vehicle.VehicleModel mockVehicleModel = new org.rutebanken.sobek.model.vehicle.VehicleModel();
        mockVehicleModel.setNetexId("VM:Model:1");

        VehicleType mockVehicleType = new VehicleType();
        mockVehicleType.setNetexId("VT:Bus:1");

        context = new MappingContext();
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        when(referenceResolver.resolve(any(),any(),eq(VehicleType.class))).thenReturn(mockVehicleType);
        when(referenceResolver.resolve(any(),any(),eq(org.rutebanken.sobek.model.vehicle.VehicleModel.class))).thenReturn(mockVehicleModel);
        context.setReferenceResolver(referenceResolver);
        context.setKeyListStructureMapper(keyListStructureMapper);
    }

    @Test
    void testMapperIsInjected() {
        assertNotNull(mapper, "Mapper should be injected by Spring");
    }

    @Test
    void testMapToSobek() {
        // Given
        VehicleModel netexModel = new VehicleModel();
        netexModel.setId("VM:1");
        netexModel.setVersion("1");

        TransportTypeRefStructure transportTypeRef = new TransportTypeRefStructure()
                .withRef("TT:Bus:1");
        JAXBElement<TransportTypeRefStructure> jaxbRef =
                objectFactory.createTransportTypeRef(transportTypeRef);
        netexModel.setTransportTypeRef(jaxbRef);

        netexModel.setRange(new BigDecimal("500.0"));

        // When
        org.rutebanken.sobek.model.vehicle.VehicleModel sobekModel =
                mapper.mapToSobek(netexModel, context);

        // Then
        assertNotNull(sobekModel);
        assertEquals(1, sobekModel.getVersion());
        assertNotNull(sobekModel.getTransportType());
        assertEquals("VT:Bus:1", sobekModel.getTransportType().getNetexId());
        assertEquals(new BigDecimal("500.0"), sobekModel.getRange());
    }

    @Test
    void testMapToNetex() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleModel sobekModel =
                new org.rutebanken.sobek.model.vehicle.VehicleModel();
        sobekModel.setVersion(1);
        org.rutebanken.sobek.model.vehicle.VehicleType vehicleType = new org.rutebanken.sobek.model.vehicle.VehicleType();
        vehicleType.setNetexId("TT:Bus:1");
        sobekModel.setTransportType(vehicleType);
        sobekModel.setRange(new BigDecimal("500.0"));

        // When
        VehicleModel netexModel = mapper.mapToNetex(sobekModel, context);

        // Then
        assertNotNull(netexModel);
        assertEquals("1", netexModel.getVersion());
        assertNotNull(netexModel.getTransportTypeRef());
        assertEquals("TT:Bus:1", netexModel.getTransportTypeRef().getValue().getRef());
        assertEquals(new BigDecimal("500.0"), netexModel.getRange());
    }

    @Test
    void testBidirectionalMapping() {
        // Given
        org.rutebanken.sobek.model.vehicle.VehicleModel originalSobek =
                new org.rutebanken.sobek.model.vehicle.VehicleModel();
        originalSobek.setVersion(1);
        org.rutebanken.sobek.model.vehicle.VehicleType vehicleType = new org.rutebanken.sobek.model.vehicle.VehicleType();
        vehicleType.setNetexId("TT:Bus:1");
        originalSobek.setTransportType(vehicleType);
        originalSobek.setFullCharge(new BigDecimal("100.0"));
        originalSobek.setDescription(new EmbeddableMultilingualString("Description ABC", "nor"));

        // When - Round trip mapping
        VehicleModel netex = mapper.mapToNetex(originalSobek, context);
        org.rutebanken.sobek.model.vehicle.VehicleModel backToSobek =
                mapper.mapToSobek(netex, context);

        // Then
        assertEquals(originalSobek.getVersion(), backToSobek.getVersion());
        assertEquals(originalSobek.getFullCharge(), backToSobek.getFullCharge());
        assertEquals(originalSobek.getDescription(), backToSobek.getDescription());
        assertEquals(originalSobek.getDescription().getValue(), backToSobek.getDescription().getValue());
    }

    @Test
    void testMapToSobekWithNullTransportTypeRef() {
        // Given
        VehicleModel netexModel = new VehicleModel();
        netexModel.setVersion("1");
        // No transportTypeRef set

        // When
        org.rutebanken.sobek.model.vehicle.VehicleModel sobekModel =
                mapper.mapToSobek(netexModel, context);

        // Then
        assertNotNull(sobekModel);
        assertNull(sobekModel.getTransportType());
    }
}