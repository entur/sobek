package org.rutebanken.sobek.rest.netex.publicationdelivery;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.rutebanken.sobek.exporter.StreamingPublicationDelivery;
import org.rutebanken.sobek.rest.ParameterDto.ExportParametersDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Produces("application/xml")
@Path("netex")
@Slf4j
public class VehicleExportResource {

    @Qualifier("syncStreamingPublicationDelivery")
    @Autowired
    private StreamingPublicationDelivery streamingPublicationDelivery;

    @GET
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    public Response getVehicleNetex(@BeanParam ExportParametersDto exportParams) {
        log.info("Exporting publication delivery. {}", exportParams);


        StreamingOutput streamingOutput = outputStream -> {
            try {
                streamingPublicationDelivery.streamVehicles(exportParams.toExportParams(), outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return Response.ok(streamingOutput).build();
    }


    @GET
    @Path("deckplans/{id}")
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    public Response getOneDeckPlan(@BeanParam ExportParametersDto exportParams, @PathParam("id") String id) {
        log.info("Exporting publication delivery. {}", exportParams);


        StreamingOutput streamingOutput = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneDeckPlan(exportParams.toExportParams(), id, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return Response.ok(streamingOutput).build();
    }

    // Path param is `{id}` (not `{netexId}`) for symmetry with
    // @Path("deckplans/{id}") above — both endpoints look up by NeTEx id.
    // Renaming one without the other would break the convention. If we
    // want a rename, do it to both at once (tracked under the DRY-up in #101).
    @GET
    @Path("vehicles/{id}")
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    public Response getOneVehicle(@BeanParam ExportParametersDto exportParams, @PathParam("id") String id) {
        log.info("Exporting publication delivery for Vehicle NeTEx id {}. {}", id, exportParams);

        StreamingOutput streamingOutput = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneVehicle(exportParams.toExportParams(), id, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream vehicle. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return Response.ok(streamingOutput).build();
    }
}