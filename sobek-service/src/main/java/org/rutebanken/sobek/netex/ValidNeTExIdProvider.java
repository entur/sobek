package org.rutebanken.sobek.netex;

import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValidNeTExIdProvider {
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final DeckPlanRepository deckPlanRepository;

    public ValidNeTExIdProvider(VehicleRepository vehicleRepository, VehicleTypeRepository vehicleTypeRepository, DeckPlanRepository deckPlanRepository) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.deckPlanRepository = deckPlanRepository;
    }

    public List<String> getValidNeTExIds() {
        var vehicleIds = vehicleRepository.findCurrentNeTExIds();
        var vehicleTypeIds = vehicleTypeRepository.findCurrentNeTExIds();
        var deckPlanIds = deckPlanRepository.findCurrentNeTExIds();

        List<String> allIds = new ArrayList<>(vehicleIds);
        allIds.addAll(vehicleTypeIds);
        allIds.addAll(deckPlanIds);
        return allIds;
    }
}
