/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.exporter;

import org.rutebanken.netex.model.LocaleStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.VersionFrameDefaultsStructure;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.time.ExportTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SobekResourceFrameExporter {

    private final NetexIdHelper netexIdHelper;

    private final ExportTimeZone exportTimeZone;


    @Autowired
    public SobekResourceFrameExporter(NetexIdHelper netexIdHelper, ExportTimeZone exportTimeZone) {

        this.netexIdHelper = netexIdHelper;
        this.exportTimeZone = exportTimeZone;
    }


    public ResourceFrame createResourceFrame(String description){
        ResourceFrame resourceFrame = new ResourceFrame();
        resourceFrame.setDescription(new MultilingualString().withContent(description));
        resourceFrame.setVersion("1");
        resourceFrame.setId(netexIdHelper.getNetexId("ResourceFrame",resourceFrame.hashCode()));

        return resourceFrame;
    }

    public void setFrameDefaultLocale(ResourceFrame resourceFrame) {

        LocaleStructure localeStructure = new LocaleStructure();
        localeStructure.setTimeZone(exportTimeZone.getDefaultTimeZoneId().toString());
        VersionFrameDefaultsStructure versionFrameDefaultsStructure = new VersionFrameDefaultsStructure();
        versionFrameDefaultsStructure.setDefaultLocale(localeStructure);
        resourceFrame.setFrameDefaults(versionFrameDefaultsStructure);
    }

}
