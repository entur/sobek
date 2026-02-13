package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for PointRefStructure.
 * Handles mapping between NeTEx PointRefStructure and Sobek PointRefStructure.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {EntityStructureMapper.class, ReferenceMapper.class}
)
public interface PointRefStructureMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx PointRefStructure to Sobek entity.
     */
    org.rutebanken.sobek.model.PointRefStructure mapToSobek(
            org.rutebanken.netex.model.PointRefStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx PointRefStructure.
     */
    org.rutebanken.netex.model.PointRefStructure mapToNetex(
            org.rutebanken.sobek.model.PointRefStructure source,
            @Context MappingContext context
    );

    /**
     * Maps a JAXBElement containing PointRefStructure from NeTEx to Sobek.
     */
    default JAXBElement<? extends org.rutebanken.sobek.model.PointRefStructure> mapJAXBElementToSobek(
            JAXBElement<? extends org.rutebanken.netex.model.PointRefStructure> source,
            @Context MappingContext context
    ) {
        if (source == null || source.getValue() == null) {
            return null;
        }
        org.rutebanken.sobek.model.PointRefStructure mapped = mapToSobek(source.getValue(), context);
        return new JAXBElement<>(source.getName(),
                (Class<org.rutebanken.sobek.model.PointRefStructure>) mapped.getClass(),
                mapped);
    }

    /**
     * Maps a JAXBElement containing PointRefStructure from Sobek to NeTEx.
     */
    default JAXBElement<? extends org.rutebanken.netex.model.PointRefStructure> mapJAXBElementToNetex(
            JAXBElement<? extends org.rutebanken.sobek.model.PointRefStructure> source,
            @Context MappingContext context
    ) {
        if (source == null || source.getValue() == null) {
            return null;
        }
        org.rutebanken.netex.model.PointRefStructure mapped = mapToNetex(source.getValue(), context);
        return OBJECT_FACTORY.createPointRef(mapped);
    }

    /**
     * Maps a list of JAXBElement<PointRefStructure> from NeTEx to Sobek.
     */
    default List<JAXBElement<? extends org.rutebanken.sobek.model.PointRefStructure>> mapPointRefListToSobek(
            List<JAXBElement<? extends org.rutebanken.netex.model.PointRefStructure>> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .map(element -> mapJAXBElementToSobek(element, context))
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of JAXBElement<PointRefStructure> from Sobek to NeTEx.
     */
    default List<JAXBElement<? extends org.rutebanken.netex.model.PointRefStructure>> mapPointRefListToNetex(
            List<JAXBElement<? extends org.rutebanken.sobek.model.PointRefStructure>> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .map(element -> mapJAXBElementToNetex(element, context))
                .collect(Collectors.toList());
    }
}