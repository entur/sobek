package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.Mapper;

/**
 * Utility mapper for common type conversions used across multiple mappers.
 */
@Mapper()
public interface CommonTypeMapper {

    /**
     * Extracts value from JAXBElement.
     */
    default <T> T unwrapJAXBElement(JAXBElement<T> element) {
        return element != null ? element.getValue() : null;
    }

    /**
     * Note: Wrapping into JAXBElement requires ObjectFactory and specific types,
     * so it's better handled in specific mappers with @AfterMapping methods.
     */
}
