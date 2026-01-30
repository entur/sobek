package org.rutebanken.sobek.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services/rest/vehicle-types")
@RequiredArgsConstructor
@Log
public class VehicleTypeController {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;

    /**
     * Get all vehicle types with pagination
     */
    @GetMapping
    public ResponseEntity<Page<VehicleType>> getAllVehicleTypes(Pageable pageable) {
        log.info("Getting all vehicle types with pagination");
        Page<VehicleType> vehicleTypes = vehicleTypeRepository.findAll(pageable);
        return ResponseEntity.ok(vehicleTypes);
    }

    /**
     * Get a specific vehicle type by ID
     */
    @GetMapping("/{neTExId}")
    public ResponseEntity<VehicleType> getVehicleTypeById(@PathVariable String neTExId) {
        log.info("Getting vehicle type with id: " + neTExId);
        List<VehicleType> vehicleType = vehicleTypeRepository.findByNetexId(neTExId);

        if(!vehicleType.isEmpty()) {
            return ResponseEntity.ok(vehicleType.getFirst());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new vehicle type
     */
    @PostMapping
    public ResponseEntity<VehicleType> createVehicleType(@Valid @RequestBody VehicleType vehicleType) {
        log.info("Creating new vehicle type: " + vehicleType.getName());
        VehicleType savedVehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicleType);
    }

    /**
     * Update an existing vehicle type
     */
    @PutMapping("/{neTExId}")
    public ResponseEntity<VehicleType> updateVehicleType(
            @PathVariable String neTExId,
            @Valid @RequestBody VehicleType vehicleType) {
        log.info("Updating vehicle type with neTExId: " + neTExId);

        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByNetexId(neTExId);
        if (vehicleTypes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VehicleType updatedVehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);
        return ResponseEntity.ok(updatedVehicleType);
    }

    /**
     * Delete a vehicle type
     */
//    @DeleteMapping("/{neTExId}")
//    public ResponseEntity<Void> deleteVehicleType(@PathVariable String neTExId) {
//        log.info("Deleting vehicle type with neTExId: " + neTExId);
//
//        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByNetexId(neTExId);
//        if (vehicleTypes.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
// ?? Set valid to date = today? What does "delete" mean?
//        vehicleTypeRepository.;
//        return ResponseEntity.noContent().build();
//    }
}