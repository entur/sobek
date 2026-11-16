package org.rutebanken.sobek.netex.extension.gridmarker;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The grid markers of a deck space, as they are carried in its Extensions.
 */
@XmlRootElement(name = "GridMarkers")
@XmlAccessorType(XmlAccessType.FIELD)
public class GridMarkersExtension {

    public static final String NAMESPACE = "urn:entur:sobek:ext:gridmarkers:1.0";

    @XmlElement(name = "GridMarker")
    private List<GridMarkerExtension> gridMarkers;

    public List<GridMarkerExtension> getGridMarkers() {
        if (gridMarkers == null) {
            gridMarkers = new ArrayList<>();
        }
        return gridMarkers;
    }
}
