package org.rutebanken.sobek.repository;

import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class VehicleModelRepositoryImpl implements VehicleModelRepositoryCustom {

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;
    public VehicleModelRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }
    /**
     * Find vehicle model's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return vehicle model's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("vehicle_model", key, values);
    }
}
