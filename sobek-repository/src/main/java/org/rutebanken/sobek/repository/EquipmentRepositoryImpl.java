package org.rutebanken.sobek.repository;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class EquipmentRepositoryImpl implements EquipmentRepositoryCustom {

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    public EquipmentRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    /**
     * Find stop place's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return stop place's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("equipment", key, values);
    }
}
