package org.rutebanken.sobek.rest.graphql;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.rest.dto.DeactivateInput;
import org.rutebanken.sobek.rest.dto.FleetChangeInput;
import org.rutebanken.sobek.service.fleet.FleetMembershipService;
import org.rutebanken.sobek.service.deactivate.DeckPlanDeactivator;
import org.rutebanken.sobek.service.deactivate.FleetDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleTypeDeactivator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ValidationException;
import org.rutebanken.sobek.graphql.converter.DeckPlanNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.FleetNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleTypeNeTExIdConverter;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final VehicleTypeInputBinder vehicleTypeInputBinder;
    private final FleetVersionedSaverService fleetVersionedSaverService;
    private final FleetNeTExIdConverter fleetIdConverter;
    private final FleetDeactivator fleetDeactivator;
    private final VehicleRepository vehicleRepository;
    private final FleetMembershipService fleetMembershipService;

    public MutationController(
            VehicleVersionedSaverService vehicleVersionedSaverService,
            VehicleNeTExIdConverter vehicleIdConverter,
            VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService,
            VehicleTypeNeTExIdConverter vehicleTypeIdConverter,
            DeckPlanVersionedSaverService deckPlanVersionedSaverService,
            DeckPlanNeTExIdConverter deckPlanNeTExIdConverter,
            VehicleTypeRepository vehicleTypeRepository,
            AuthorizationService authorizationService,
            VehicleDeactivator vehicleDeactivator,
            VehicleTypeDeactivator vehicleTypeDeactivator,
            DeckPlanDeactivator deckPlanDeactivator,
            DeckPlanRepository deckPlanRepository,
            VehicleTypeInputBinder vehicleTypeInputBinder,
            FleetVersionedSaverService fleetVersionedSaverService,
            FleetNeTExIdConverter fleetIdConverter,
            FleetDeactivator fleetDeactivator,
            VehicleRepository vehicleRepository,
            FleetMembershipService fleetMembershipService) {
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
        this.vehicleTypeInputBinder = vehicleTypeInputBinder;
        this.fleetVersionedSaverService = fleetVersionedSaverService;
        this.fleetIdConverter = fleetIdConverter;
        this.fleetDeactivator = fleetDeactivator;
        this.vehicleRepository = vehicleRepository;
        this.fleetMembershipService = fleetMembershipService;
    }

    @MutationMapping
    public String createOrUpdateVehicle (
            @Argument Vehicle input
    ) throws ValidationException {
        authorizationService.verifyCanEditEntities(List.of(input));
        input = vehicleIdConverter.convertIncomingId(input);
        if (input.getTransportType() != null) {
            VehicleType vt = vehicleTypeIdConverter.convertIncomingId(input.getTransportType());
            vt = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(vt.getNetexId());
            if (vt == null) {
                throw new ValidationException("Vehicle refers to a vehicle type that is not found in the database: " + input.getTransportType().getNetexId());
            }
            input.setTransportType(vt);
        }
        var output = vehicleVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String createOrUpdateVehicleType (
            DataFetchingEnvironment env
    ) throws ValidationException {
        VehicleType input = vehicleTypeInputBinder.bind(env);
        authorizationService.verifyCanEditEntities(List.of(input));
        input = vehicleTypeIdConverter.convertIncomingId(input);
        if (input.getDeckPlan() != null) {
            DeckPlan dp = deckPlanIdConverter.convertIncomingId(input.getDeckPlan());
            dp = deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(dp.getNetexId());
            if (dp == null) {
                throw new ValidationException("Vehicle type refers to a deck plan that is not found in the database: " + input.getDeckPlan().getNetexId());
            }
            input.setDeckPlan(dp);
        }
        var output = vehicleTypeVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    /**
     * The members and transport types are replaced as a whole: what is not in the input is not in the new version.
     * They are given as references, and are looked up here in their latest versions.
     */
    @MutationMapping
    public String createOrUpdateFleet (
            @Argument Fleet input,
            DataFetchingEnvironment env
    ) throws ValidationException {
        authorizationService.verifyCanEditEntities(List.of(input));
        input = fleetIdConverter.convertIncomingId(input);
        addKeyValues(input, env);

        List<Vehicle> members = new ArrayList<>();
        for (Vehicle reference : input.getMembers()) {
            Vehicle member = vehicleRepository.findFirstByNetexIdOrderByVersionDesc(vehicleIdConverter.convertIncomingId(reference).getNetexId());
            if (member == null) {
                throw new ValidationException("Fleet refers to a vehicle that is not found in the database: " + reference.getNetexId());
            }
            if (!members.contains(member)) {
                members.add(member);
            }
        }
        input.setMembers(members);

        List<VehicleType> transportTypes = new ArrayList<>();
        for (VehicleType reference : input.getTransportTypes()) {
            VehicleType transportType = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(vehicleTypeIdConverter.convertIncomingId(reference).getNetexId());
            if (transportType == null) {
                throw new ValidationException("Fleet refers to a vehicle type that is not found in the database: " + reference.getNetexId());
            }
            if (!transportTypes.contains(transportType)) {
                transportTypes.add(transportType);
            }
        }
        input.setTransportTypes(transportTypes);

        var output = fleetVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String addToFleet(@Argument FleetChangeInput input) throws ValidationException {
        return fleetMembershipService.add(input.fleetNetexId(), input.vehicleNetexIds(), input.transportTypeNetexIds()).getNetexId();
    }

    @MutationMapping
    public String removeFromFleet(@Argument FleetChangeInput input) throws ValidationException {
        return fleetMembershipService.remove(input.fleetNetexId(), input.vehicleNetexIds(), input.transportTypeNetexIds()).getNetexId();
    }

    /**
     * The key values of the input are not bound to the entity, as it has no setter for them. As for any new version, the
     * key values of the previous version that the input does not give again are kept.
     */
    @SuppressWarnings("unchecked")
    private static void addKeyValues(Fleet fleet, DataFetchingEnvironment env) {
        Map<String, Object> rawInput = env.getArgument("input");
        Object rawKeyValues = rawInput == null ? null : rawInput.get("keyValues");
        if (rawKeyValues == null) {
            return;
        }
        for (Map<String, Object> keyValue : (List<Map<String, Object>>) rawKeyValues) {
            fleet.removeKeyValue((String) keyValue.get("key"));
            fleet.addKeyValue((String) keyValue.get("key"), (String) keyValue.get("value"));
        }
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
    public Fleet deactivateFleet(@Argument DeactivateInput input) throws ValidationException {
        return fleetDeactivator.deactivateFleet(input.netexId(), input.version(), input.deactivateAt());
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
