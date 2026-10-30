package org.rutebanken.sobek.exporter;

import org.entur.netex2.model.CompositeFrame;
import org.entur.netex2.model.PublicationDeliveryStructure;
import org.entur.netex2.model.ResourceFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.model.job.ExportParams;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleModelMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleTypeMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckPlanMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.EquipmentMapper;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.EquipmentRepository;
import org.rutebanken.sobek.repository.VehicleModelRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamingPublicationDeliveryTest {

    private static final String DATA_OWNER = "AKT";

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleTypeRepository vehicleTypeRepository;
    @Mock private VehicleModelRepository vehicleModelRepository;
    @Mock private DeckPlanRepository deckPlanRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private PublicationDeliveryCreator publicationDeliveryCreator;
    @Mock private VehicleTypeMapper vehicleTypeMapper;
    @Mock private EquipmentMapper equipmentMapper;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private DeckPlanMapper deckPlanMapper;
    @Mock private VehicleModelMapper vehicleModelMapper;
    @Mock private AuthorizationService authorizationService;
    @Mock private MappingContext mappingContext;
    @Mock private SobekResourceFrameExporter resourceFrameExporter;
    @Mock private SobekComositeFrameExporter compositeFrameExporter;

    private StreamingPublicationDelivery streamingPublicationDelivery;

    @BeforeEach
    void setUp() {
        streamingPublicationDelivery = new StreamingPublicationDelivery(
            vehicleRepository,
            vehicleTypeRepository,
            vehicleModelRepository,
            deckPlanRepository,
            equipmentRepository,
            publicationDeliveryCreator,
            vehicleTypeMapper,
            equipmentMapper,
            vehicleMapper,
            deckPlanMapper,
            vehicleModelMapper,
            authorizationService,
            mappingContext,
            resourceFrameExporter,
            compositeFrameExporter);

        // Skeleton needed to get through frame creation and marshalling (schema validation is off by default
        // when the @Value field is not injected).
        lenient().when(compositeFrameExporter.createCompositeFrame(any())).thenReturn(new CompositeFrame());
        lenient().when(resourceFrameExporter.createResourceFrame(any())).thenReturn(new ResourceFrame());
        lenient().when(publicationDeliveryCreator.createPublicationDelivery(any(CompositeFrame.class)))
            .thenReturn(new PublicationDeliveryStructure());

        // No data: the tests are about which repository methods are used, and with what arguments
        lenient().when(vehicleRepository.findCurrentFiltered(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        lenient().when(vehicleTypeRepository.findCurrentFiltered(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        lenient().when(deckPlanRepository.findCurrentFiltered(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        lenient().when(vehicleModelRepository.findCurrentFiltered(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        lenient().when(equipmentRepository.findCurrentFiltered(any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
    }

    private ExportParams exportParamsFor(String dataOwner) {
        ExportParams exportParams = mock(ExportParams.class);
        lenient().when(exportParams.getDataOwner()).thenReturn(dataOwner);
        return exportParams;
    }

    // --- 1. dataOwner filtering ---

    @Test
    void streamVehicles_withDataOwner_filtersAllEntityTypesOnThatDataOwner() throws Exception {
        streamingPublicationDelivery.streamVehicles(exportParamsFor(DATA_OWNER), new ByteArrayOutputStream());

        verify(vehicleRepository).findCurrentFiltered(eq(DATA_OWNER), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(vehicleTypeRepository).findCurrentFiltered(eq(DATA_OWNER), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(deckPlanRepository).findCurrentFiltered(eq(DATA_OWNER), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(equipmentRepository).findCurrentFiltered(eq(DATA_OWNER), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(vehicleModelRepository).findCurrentFiltered(eq(DATA_OWNER), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
    }

    // --- 2. admin: no dataOwner means "all dataowners" ---

    @Test
    void streamVehicles_withoutDataOwner_queriesAllDataOwners() throws Exception {
        streamingPublicationDelivery.streamVehicles(exportParamsFor(null), new ByteArrayOutputStream());

        verify(vehicleRepository).findCurrentFiltered(isNull(), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(vehicleTypeRepository).findCurrentFiltered(isNull(), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(deckPlanRepository).findCurrentFiltered(isNull(), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(equipmentRepository).findCurrentFiltered(isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
        verify(vehicleModelRepository).findCurrentFiltered(isNull(), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
    }

    // --- 4. only current versions, never the whole history ---

    @Test
    void streamVehicles_neverReadsFullVersionHistory() throws Exception {
        streamingPublicationDelivery.streamVehicles(exportParamsFor(DATA_OWNER), new ByteArrayOutputStream());

        verify(vehicleRepository, never()).findAll();
        verify(vehicleTypeRepository, never()).findAll();
        verify(deckPlanRepository, never()).findAll();
        verify(vehicleModelRepository, never()).findAll();
        verify(equipmentRepository, never()).findAll();
    }

    @Test
    void streamVehicles_withoutData_producesOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        streamingPublicationDelivery.streamVehicles(exportParamsFor(DATA_OWNER), out);

        assertThat(out.toString()).contains("PublicationDelivery");
    }

    // --- 3. access check on single-object export ---

    @Test
    void streamOneDeckPlan_userNotAuthorizedForDataOwner_isDenied() {
        DeckPlan deckPlan = new DeckPlan();
        deckPlan.setDataOwnerRef("OTHER:Authority:1");
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc("NMR:DeckPlan:1")).thenReturn(deckPlan);
        doNothing().when(authorizationService).verifyCanViewDataOwner(eq(DATA_OWNER));
        doThrow(new AccessDeniedException("Not authorized"))
            .when(authorizationService).verifyCanViewDataOwner("OTHER:Authority:1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExportParams exportParams = exportParamsFor(DATA_OWNER);

        assertThatThrownBy(() -> streamingPublicationDelivery.streamOneDeckPlan(exportParams, "NMR:DeckPlan:1", out))
            .isInstanceOf(AccessDeniedException.class);

        verify(deckPlanMapper, never()).mapToNetex(any(), any());
        assertThat(out.size()).isZero();
    }

    @Test
    void streamOneDeckPlan_userAuthorizedForDataOwner_verifiesAccessAndExports() throws Exception {
        DeckPlan deckPlan = new DeckPlan();
        deckPlan.setDataOwnerRef(DATA_OWNER);
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc("NMR:DeckPlan:1")).thenReturn(deckPlan);
        when(deckPlanMapper.mapToNetex(eq(deckPlan), any())).thenReturn(new org.entur.netex2.model.DeckPlan());

        streamingPublicationDelivery.streamOneDeckPlan(exportParamsFor(DATA_OWNER), "NMR:DeckPlan:1", new ByteArrayOutputStream());

        verify(authorizationService, times(2)).verifyCanViewDataOwner(DATA_OWNER);
        verify(deckPlanMapper).mapToNetex(eq(deckPlan), any());
    }
}