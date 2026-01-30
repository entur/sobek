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
import java.util.Optional;

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
    @GetMapping("/{netexId}")
    public ResponseEntity<VehicleType> getVehicleTypeById(@PathVariable String netexId) {
        log.info("Getting vehicle type with id: " + netexId);
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByNetexId(netexId);

        if(vehicleTypes.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(vehicleTypes.getFirst());
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
    @PutMapping("/{netexId}")
    public ResponseEntity<VehicleType> updateVehicleType(
            @PathVariable String netexId,
            @Valid @RequestBody VehicleType vehicleType) {
        log.info("Updating vehicle type with id: " + netexId);


        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByNetexId(netexId);
        if (vehicleTypes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VehicleType updatedVehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);
        return ResponseEntity.ok(updatedVehicleType);
    }

    /**
     * Delete a vehicle type ?? What should delete do? Update valid to date = today?
     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVehicleType(@PathVariable String id) {
//        log.info("Deleting vehicle type with id: " + id);
//
//        if (!vehicleTypeRepository.existsById(id)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        vehicleTypeRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
}