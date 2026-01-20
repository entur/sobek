package org.rutebanken.sobek.model.vehicle;

import jakarta.xml.bind.JAXBElement;
import org.rutebanken.sobek.model.Common_VersionFrameStructure;
import org.rutebanken.sobek.model.ContainmentAggregationStructure;

import java.util.ArrayList;
import java.util.List;

public class Frames_RelStructure extends ContainmentAggregationStructure {
    private List<JAXBElement<? extends Common_VersionFrameStructure>> commonFrame;

    public List<JAXBElement<? extends Common_VersionFrameStructure>> getCommonFrame() {
        if (this.commonFrame == null) {
            this.commonFrame = new ArrayList();
        }

        return this.commonFrame;
    }

}