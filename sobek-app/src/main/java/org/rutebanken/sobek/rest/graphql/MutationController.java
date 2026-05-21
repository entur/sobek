package org.rutebanken.sobek.rest.graphql;

import org.rutebanken.sobek.graphql.converter.DeckPlanNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleNeTExIdConverter;
import org.rutebanken.sobek.graphql.converter.VehicleTypeNeTExIdConverter;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@SchemaMapping(typeName = "Mutation")
public class MutationController {
    private final VehicleVersionedSaverService vehicleVersionedSaverService;
    private final VehicleNeTExIdConverter vehicleIdConverter;
    private final VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;
    private final VehicleTypeNeTExIdConverter vehicleTypeIdConverter;
    private final DeckPlanVersionedSaverService deckPlanVersionedSaverService;
    private final DeckPlanNeTExIdConverter deckPlanIdConverter;

    public MutationController(
            VehicleVersionedSaverService vehicleVersionedSaverService, VehicleNeTExIdConverter vehicleIdConverter, VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService, VehicleTypeNeTExIdConverter vehicleTypeIdConverter, DeckPlanVersionedSaverService deckPlanVersionedSaverService, DeckPlanNeTExIdConverter deckPlanNeTExIdConverter) {
        this.vehicleVersionedSaverService = vehicleVersionedSaverService;
        this.vehicleIdConverter = vehicleIdConverter;
        this.vehicleTypeVersionedSaverService = vehicleTypeVersionedSaverService;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.deckPlanVersionedSaverService = deckPlanVersionedSaverService;
        this.deckPlanIdConverter = deckPlanNeTExIdConverter;
    }

    @MutationMapping
    public String createOrUpdateVehicle (
            @Argument Vehicle input
    ) {
        input = vehicleIdConverter.convertIncomingId(input);
        var output = vehicleVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String createOrUpdateVehicleType (
            @Argument VehicleType input
    ) {
        input = vehicleTypeIdConverter.convertIncomingId(input);
        var output = vehicleTypeVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }

    @MutationMapping
    public String createOrUpdateDeckPlan (
            @Argument DeckPlan input
    ) {
        input = deckPlanIdConverter.convertIncomingId(input);
        var output = deckPlanVersionedSaverService.saveNewVersion(input);
        return output.getNetexId();
    }
}
