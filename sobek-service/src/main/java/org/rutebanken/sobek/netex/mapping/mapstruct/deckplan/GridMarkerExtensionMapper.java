package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import jakarta.validation.ValidationException;
import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import org.entur.netex2.model.ExtensionsStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.Deck;
import org.rutebanken.sobek.model.vehicle.GridMarker;
import org.rutebanken.sobek.netex.extension.gridmarker.GridMarkerExtension;
import org.rutebanken.sobek.netex.extension.gridmarker.GridMarkersExtension;

/**
 * Grid markers are not a part of NeTEx. They are carried in the Extensions of a deck space, in Sobek's own namespace.
 * Maps between them and {@link GridMarker}.
 */
public final class GridMarkerExtensionMapper {

    private GridMarkerExtensionMapper() {
    }

    /**
     * The grid markers in the extensions, with the row and the column found in the given deck.
     *
     * @throws ValidationException if a marker has no label, or a row or a column that is not in the deck
     */
    public static List<GridMarker> toSobek(ExtensionsStructure extensions, Deck deck) {
        List<GridMarker> markers = new ArrayList<>();
        if (extensions == null) {
            return markers;
        }
        for (Object any : extensions.getAny()) {
            Object value = any instanceof JAXBElement<?> element ? element.getValue() : any;
            if (value instanceof GridMarkersExtension gridMarkers) {
                gridMarkers.getGridMarkers().forEach(marker -> markers.add(toSobek(marker, deck)));
            }
        }
        return markers;
    }

    /**
     * The extensions for the grid markers of a deck space, or null if it has none.
     */
    public static ExtensionsStructure toNetex(List<GridMarker> markers) {
        if (markers == null || markers.isEmpty()) {
            return null;
        }
        GridMarkersExtension gridMarkers = new GridMarkersExtension();
        for (GridMarker marker : markers) {
            GridMarkerExtension extension = new GridMarkerExtension();
            extension.setSpotRowRef(marker.getSpotRow() == null ? null : marker.getSpotRow().getNetexId());
            extension.setSpotColumnRef(marker.getSpotColumn() == null ? null : marker.getSpotColumn().getNetexId());
            if (marker.getLabel() != null) {
                extension.setLabel(new GridMarkerExtension.Label(marker.getLabel().getValue(), marker.getLabel().getLang()));
            }
            gridMarkers.getGridMarkers().add(extension);
        }
        ExtensionsStructure extensions = new ExtensionsStructure();
        extensions.getAny().add(gridMarkers);
        return extensions;
    }

    private static GridMarker toSobek(GridMarkerExtension source, Deck deck) {
        if (source.getLabel() == null || source.getLabel().getValue() == null || source.getLabel().getValue().isBlank()) {
            throw new ValidationException("A GridMarker needs a Label");
        }
        GridMarker marker = new GridMarker();
        marker.setLabel(new EmbeddableMultilingualString(source.getLabel().getValue(), source.getLabel().getLang()));
        marker.setSpotRow(find(deck.getSpotRows(), source.getSpotRowRef(), "SpotRow", source));
        marker.setSpotColumn(find(deck.getSpotColumns(), source.getSpotColumnRef(), "SpotColumn", source));
        return marker;
    }

    private static <T extends org.rutebanken.sobek.model.EntityStructure> T find(List<T> candidates, String ref, String type, GridMarkerExtension marker) {
        if (ref == null || ref.isBlank()) {
            throw new ValidationException("GridMarker '" + marker.getLabel().getValue() + "' needs a " + type + "Ref");
        }
        return (candidates == null ? List.<T>of() : candidates).stream()
                .filter(candidate -> ref.equals(candidate.getNetexId()))
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                        "GridMarker '" + marker.getLabel().getValue() + "' refers to " + type + " " + ref + ", which is not in the deck of its space"));
    }
}
