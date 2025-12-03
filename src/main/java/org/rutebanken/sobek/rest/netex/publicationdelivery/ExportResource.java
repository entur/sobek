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

package org.rutebanken.sobek.rest.netex.publicationdelivery;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBException;
import org.rutebanken.sobek.exporter.StreamingPublicationDelivery;
import org.rutebanken.sobek.exporter.params.ExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;

@Component
@Tag(name = "Sync export resource", description = "Sync export resource")
@Produces("application/xml")
@Path("netex")
public class ExportResource {

    private static final Logger logger = LoggerFactory.getLogger(ExportResource.class);

    @Qualifier("syncStreamingPublicationDelivery")
    @Autowired
    private StreamingPublicationDelivery streamingPublicationDelivery;

    @Autowired
    public ExportResource() {

    }

    @GET
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    public Response exportVehicles(@BeanParam ExportParams exportParams) throws JAXBException, IOException, SAXException {
        logger.info("Exporting publication delivery. {}", exportParams);


        StreamingOutput streamingOutput = outputStream -> {
            try {
                streamingPublicationDelivery.streamVehicles(exportParams, outputStream);
            } catch (Exception e) {
                logger.warn("Could not stream site frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return Response.ok(streamingOutput).build();
    }
}
