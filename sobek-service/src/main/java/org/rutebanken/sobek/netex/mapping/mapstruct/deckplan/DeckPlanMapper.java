
package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.DeckPlan;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

import java.util.List;

/**
 * MapStruct mapper for DeckPlan.
 * Handles mapping between NeTEx DeckPlan and Sobek DeckPlan entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = { DataManagedObjectStructureMapper.class, DeckMapper.class }
)
public interface DeckPlanMapper {

    /**
     * Maps from NeTEx DeckPlan to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "configurationConditions", ignore = true) // Transient field
    org.rutebanken.sobek.model.vehicle.DeckPlan mapToSobek(
            DeckPlan source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx DeckPlan.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "configurationConditions", ignore = true)
    DeckPlan mapToNetex(
            org.rutebanken.sobek.model.vehicle.DeckPlan source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "configurationConditions", ignore = true)
    void updateSobekFromNetex(
            DeckPlan source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.DeckPlan target,
            @Context MappingContext context
    );

    /**
     * After mapping from NeTEx to Sobek: handle any custom mappings.
     */
    @AfterMapping
    default void afterMapToSobek(
            DeckPlan source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.DeckPlan target,
            @Context MappingContext context
    ) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
        context.getOwnedEntityMapper().updateSobekFromNetex(source, target, context);
    }

    /**
     * After mapping from Sobek to NeTEx: handle any custom mappings.
     */
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.DeckPlan source,
            @MappingTarget DeckPlan target,
            @Context MappingContext context
    ) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }

    List<org.rutebanken.sobek.model.vehicle.DeckPlan> mapAsList(List<DeckPlan> sourceList, @Context MappingContext context);
}