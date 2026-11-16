package org.rutebanken.sobek.netex.extension.gridmarker;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * A label on a cell of the grid of the deck: the row and the column are refs to a SpotRow and a SpotColumn of the same deck.
 */
@XmlType(name = "GridMarker")
@XmlAccessorType(XmlAccessType.FIELD)
public class GridMarkerExtension {

    @XmlAttribute(name = "spotRowRef", required = true)
    private String spotRowRef;

    @XmlAttribute(name = "spotColumnRef", required = true)
    private String spotColumnRef;

    @XmlElement(name = "Label", required = true)
    private Label label;

    public String getSpotRowRef() {
        return spotRowRef;
    }

    public void setSpotRowRef(String spotRowRef) {
        this.spotRowRef = spotRowRef;
    }

    public String getSpotColumnRef() {
        return spotColumnRef;
    }

    public void setSpotColumnRef(String spotColumnRef) {
        this.spotColumnRef = spotColumnRef;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    @XmlType(name = "GridMarkerLabel")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Label {

        @XmlAttribute(name = "lang")
        private String lang;

        @XmlValue
        private String value;

        public Label() {
        }

        public Label(String value, String lang) {
            this.value = value;
            this.lang = lang;
        }

        public String getLang() {
            return lang;
        }

        public String getValue() {
            return value;
        }
    }
}
