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

    @GET
    @Path("vehicles/{id}")
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    public Response getOneVehicle(@BeanParam ExportParametersDto exportParams, @PathParam("id") String id) {
        log.info("Exporting publication delivery for Vehicle {}. {}", id, exportParams);

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