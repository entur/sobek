package org.rutebanken.sobek.netex.mapping.config;

import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.rutebanken.sobek.netex.mapping.mapstruct.EntityStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.MultilingualStringMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.TemporalTypeMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.ValidBetweenListConverter;

/**
 * Base configuration for all MapStruct mappers in Sobek.
 * All mappers should use this config to ensure consistent behavior.
 */
@MapperConfig(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        builder = @Builder(disableBuilder = true),
        uses = {TemporalTypeMapper.class, ValidBetweenListConverter.class, EntityStructureMapper.class, MultilingualStringMapper.class}
)
public interface SobekMapperConfig {
}