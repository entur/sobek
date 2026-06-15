package org.rutebanken.sobek.rest.graphql;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.rest.dto.DeactivateInput;
import org.rutebanken.sobek.service.deactivate.DeckPlanDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleTypeDeactivator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.xml.bind.ValidationException;
import org.rutebanken.sobek.graphql.converter.DeckPlanNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleTypeNeTExIdConverter;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@SchemaMapping(typeName = "Mutation")
@Transactional(rollbackFor = { ValidationException.class, AccessDeniedException.class})
public class MutationController {
    private final VehicleVersionedSaverService vehicleVersionedSaverService;
    private final VehicleNeTExIdConverter vehicleIdConverter;
    private final VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;
    private final VehicleTypeNeTExIdConverter vehicleTypeIdConverter;
    private final DeckPlanVersionedSaverService deckPlanVersionedSaverService;
    private final DeckPlanNeTExIdConverter deckPlanIdConverter;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final AuthorizationService authorizationService;
    private final VehicleDeactivator vehicleDeactivator;
    private final VehicleTypeDeactivator vehicleTypeDeactivator;
    private final DeckPlanDeactivator deckPlanDeactivator;
    private final DeckPlanRepository deckPlanRepository;

    public MutationController(
            VehicleVersionedSaverService vehicleVersionedSaverService,
            VehicleNeTExIdConverter vehicleIdConverter,
            VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService,
            VehicleTypeNeTExIdConverter vehicleTypeIdConverter,
            DeckPlanVersionedSaverService deckPlanVersionedSaverService,
            DeckPlanNeTExIdConverter deckPlanNeTExIdConverter,
            VehicleTypeRepository vehicleTypeRepository, AuthorizationService authorizationService, VehicleDeactivator vehicleDeactivator, VehicleTypeDeactivator vehicleTypeDeactivator, DeckPlanDeactivator deckPlanDeactivator, DeckPlanRepository deckPlanRepository) {
        this.vehicleVersionedSaverService = vehicleVersionedSaverService;
        this.vehicleIdConverter = vehicleIdConverter;
        this.vehicleTypeVersionedSaverService = vehicleTypeVersionedSaverService;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.deckPlanVersionedSaverService = deckPlanVersionedSaverService;
        this.deckPlanIdConverter = deckPlanNeTExIdConverter;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.authorizationService = authorizationService;
        this.vehicleDeactivator = vehicleDeactivator;
        this.vehicleTypeDeactivator = vehicleTypeDeactivator;
        this.deckPlanDeactivator = deckPlanDeactivator;
        this.deckPlanRepository = deckPlanRepository;
    }

    @MutationMapping
    public String createOrUpdateVehicle (
            @Argument Vehicle input
    ) throws ValidationException {
        authorizationService.verifyCanEditEntities(List.of(input));
        input = vehicleIdConverter.convertIncomingId(input);
        if (input.getTransportType() != null) {
            VehicleType vt = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(input.getTransportType().getNetexId());
            if (vt == null) {
                throw new ValidationException("Vehicle refers to a vehicle type that is not found in the database.");
            }
            input.setTransportType(vehicleTypeIdConverter.convertIncomingId(vt));
        }
        var output = vehicleVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String createOrUpdateVehicleType (
            @Argument VehicleType input
    ) throws ValidationException {
        authorizationService.verifyCanEditEntities(List.of(input));
        input = vehicleTypeIdConverter.convertIncomingId(input);
        if (input.getDeckPlan() != null) {
            DeckPlan dp = deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(input.getDeckPlan().getNetexId());
            if (dp == null) {
                throw new ValidationException("Vehicle type refers to a deck plan that is not found in the database.");
            }
            input.setDeckPlan(deckPlanIdConverter.convertIncomingId(dp));
        }
        var output = vehicleTypeVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String createOrUpdateDeckPlan (
            @Argument DeckPlan input
    ) {
        authorizationService.verifyCanEditEntities(List.of(input));
        input = deckPlanIdConverter.convertIncomingId(input);
        var output = deckPlanVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public Vehicle deactivateVehicle(@Argument DeactivateInput input) throws ValidationException {
        return vehicleDeactivator.deactivateVehicle(input.netexId(), input.version(), input.deactivateAt());
    }

    @MutationMapping
    public VehicleType deactivateVehicleType(@Argument DeactivateInput input) throws ValidationException {
        return vehicleTypeDeactivator.deactivateVehicleType(input.netexId(), input.version(), input.deactivateAt());
    }

    @MutationMapping
    public DeckPlan deactivateDeckPlan(@Argument DeactivateInput input) throws ValidationException {
        return deckPlanDeactivator.deactivateDeckPlan(input.netexId(), input.version(), input.deactivateAt());
    }

}
