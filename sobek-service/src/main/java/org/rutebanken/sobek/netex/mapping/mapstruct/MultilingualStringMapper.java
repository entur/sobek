
package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.Mapper;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.TextType;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

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

        final var content = source.getContent();
        AtomicReference<String> lang = new AtomicReference<>(source.getLang());

        AtomicReference<String> stringContent = new AtomicReference<>("");
        content.forEach(value -> {
           if(value != null) {
               if(value instanceof JAXBElement<?> jaxbElement) {
                   Object elementValue = jaxbElement.getValue();
                   if (elementValue instanceof TextType textType) {
                       if (textType.getLang() != null) {
                           lang.set(textType.getLang());
                       }
                       if (textType.getValue() != null) {
                           stringContent.set(stringContent.get() + textType.getValue());
                       }
                   }
               } else {
                   stringContent.set(stringContent.get() + value.toString());
               }
           }
        });

        return new EmbeddableMultilingualString(stringContent.toString(), lang.get());
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
        netexString.withContent(List.of(createTextType(source.getLang(), source.getValue())));
        return netexString;
    }

    private JAXBElement<? extends TextType> createTextType (String lang, String value){
        return OBJECT_FACTORY.createMultilingualStringText(new TextType()
                .withLang(lang)
                .withValue(value));
    }

}