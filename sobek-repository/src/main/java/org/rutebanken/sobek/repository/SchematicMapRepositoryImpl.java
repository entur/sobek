package org.rutebanken.sobek.repository;

import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class SchematicMapRepositoryImpl implements SchematicMapRepositoryCustom {

    private final DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository;

    public SchematicMapRepositoryImpl(DataManagedObjectStructureRepositoryImpl dataManagedObjectStructureRepository) {
        this.dataManagedObjectStructureRepository = dataManagedObjectStructureRepository;
    }

    /**
     * Find deck plan's netex ID by key value
     *
     * @param key    key in key values for stop
     * @param values list of values to check for
     * @return deck plan's netex ID
     */
    @Override
    public String findFirstByKeyValues(String key, Set<String> values) {
        return dataManagedObjectStructureRepository.findFirstByKeyValues("schematic_map", key, values);
    }
}
