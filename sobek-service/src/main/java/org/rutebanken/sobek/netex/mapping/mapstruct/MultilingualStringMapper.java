
package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Mapper;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * MapStruct mapper for MultilingualString conversions.
 * Handles mapping between:
 * - NeTEx: org.rutebanken.netex.model.MultilingualString (with List of values per language)
 * - Sobek: org.rutebanken.sobek.model.EmbeddableMultilingualString (single value and language)
 *
 * Note: NeTEx MultilingualString can contain multiple language variants, but Sobek's
 * EmbeddableMultilingualString only stores one. When mapping from NeTEx to Sobek,
 * the first non-null value is used.
 */
@Mapper()
public interface MultilingualStringMapper {

    /**
     * Maps NeTEx MultilingualString to Sobek EmbeddableMultilingualString.
     *
     * NeTEx MultilingualString has a list of values (one per language).
     * Sobek EmbeddableMultilingualString stores only one value and language.
     *
     * @param source NeTEx MultilingualString (may contain multiple language variants)
     * @return Sobek EmbeddableMultilingualString (single value and language)
     */
    default EmbeddableMultilingualString mapToSobek(MultilingualString source) {
        if(source == null ||
                source.getContent() == null ||
                source.getContent().isEmpty()) {
            return null;
        }

        return new EmbeddableMultilingualString(source.getContent().getFirst().toString(), source.getLang());
    }

    /**
     * Maps Sobek EmbeddableMultilingualString to NeTEx MultilingualString.
     *
     * Creates a NeTEx MultilingualString with a single value entry.
     *
     * @param source Sobek EmbeddableMultilingualString
     * @return NeTEx MultilingualString with one value in the list
     */
    default MultilingualString mapToNetex(EmbeddableMultilingualString source) {

        if(source == null ||
                source.getValue() == null ||
                source.getValue().isEmpty()) {
            return null;
        }

        MultilingualString netexString = new MultilingualString();
        netexString.withContent(source.getValue());
        netexString.setLang(source.getLang());
        return netexString;
    }
}