package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.*;
import org.rutebanken.netex.model.*;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.sobek.model.vehicle.LocatableSpot;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        config = SobekMapperConfig.class,
        uses = {DataManagedObjectStructureMapper.class
        }
)
public interface SpotAffinityMapper {
    /**
     * Maps from NeTEx Deck to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "members", ignore = true) // Handled by AfterMapping.
    org.rutebanken.sobek.model.vehicle.SpotAffinity mapToSobek(
            SpotAffinity source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx Deck.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "members", ignore = true) // Handled by AfterMapping.
    SpotAffinity mapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotAffinity source,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            SpotAffinity source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotAffinity target,
            @Context MappingContext context
    ) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
        if(source.getMembers() != null &&
                source.getMembers().getLocatableSpotRef() != null &&
                !source.getMembers().getLocatableSpotRef().isEmpty()) {
            org.rutebanken.sobek.model.vehicle.PassengerSpace passengerSpace = (org.rutebanken.sobek.model.vehicle.PassengerSpace) context.getCurrentSobekDeckSpace(); // TODO: Type check
            if (passengerSpace != null && passengerSpace.getPassengerSpots() != null) {
                List<LocatableSpot> sobekSpots = source.getMembers().getLocatableSpotRef().stream()
                        .map(JAXBElement::getValue)
                        .map(n -> mapNetexRef2Sobek(n, passengerSpace.getPassengerSpots(), passengerSpace.getLuggageSpots()))
                        .toList();
                if(!sobekSpots.isEmpty()) {
                    target.setMembers(sobekSpots);
                }
            }
        }
    }

    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotAffinity source,
            @MappingTarget SpotAffinity target,
            @Context MappingContext context
    ) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
        var sobekMembers = source.getMembers();
        if(sobekMembers != null && !sobekMembers.isEmpty()) {
            LocatableSpotRefs_RelStructure members = new LocatableSpotRefs_RelStructure();
            members.getLocatableSpotRef().addAll(sobekMembers.stream()
                    .map(this::mapToNetexRef)
                    .filter(Objects::nonNull)
                    .toList());
            target.setMembers(members);
        }
    }

    default List<org.rutebanken.sobek.model.vehicle.SpotAffinity> mapListToSobek(SpotAffinities_RelStructure source, @Context MappingContext context) {
        if(source == null || source.getSpotAffinity() == null) {
            return null;
        }
        return source.getSpotAffinity().stream()
                .map(ds -> mapToSobek(ds, context))
                .collect(Collectors.toList());
    }

    default SpotAffinities_RelStructure mapListToNetex(List<org.rutebanken.sobek.model.vehicle.SpotAffinity> source, @Context MappingContext context) {
        if(source == null || source.isEmpty()) {
            return null;
        }

        return new SpotAffinities_RelStructure()
                .withSpotAffinity(source.stream()
                        .map(ds -> mapToNetex(ds, context))
                        .collect(Collectors.toList()));
    }

    default LocatableSpot mapNetexRef2Sobek(LocatableSpotRefStructure locatableSpotRefStructure, List<org.rutebanken.sobek.model.vehicle.PassengerSpot> passengerSpots, List<org.rutebanken.sobek.model.vehicle.LuggageSpot> luggageSpots) {
        if (locatableSpotRefStructure.getRef() == null || locatableSpotRefStructure.getRef().isEmpty()) {
            return null;
        }

        var locatableSpots = switch (locatableSpotRefStructure) {
            case org.rutebanken.netex.model.PassengerSpotRefStructure passengerSpotRefStructure ->
                    passengerSpots;
            case org.rutebanken.netex.model.LuggageSpotRefStructure luggageSpotRefStructure ->
                    luggageSpots;
            default -> null;
        };
        if(locatableSpots == null) {
            return null;
        }
        return locatableSpots.stream()
                .filter(spot -> spot.getNetexId().equals(locatableSpotRefStructure.getRef()))
                .findFirst()
                .orElse(null);
    }
    default JAXBElement<? extends LocatableSpotRefStructure> mapToNetexRef(LocatableSpot spot) {
        ObjectFactory objectFactory = new ObjectFactory();
        return switch (spot) {
            case org.rutebanken.sobek.model.vehicle.PassengerSpot passengerSpot -> objectFactory.createPassengerSpotRef(new PassengerSpotRefStructure().withRef(passengerSpot.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.LuggageSpot luggageSpot -> objectFactory.createLuggageSpotRef(new LuggageSpotRefStructure().withRef(luggageSpot.getNetexId()));
            default -> null;
        };
    }
}
