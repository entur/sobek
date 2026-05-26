package org.rutebanken.sobek.rest.netex.publicationdelivery;

import lombok.extern.slf4j.Slf4j;
import org.rutebanken.sobek.exporter.StreamingPublicationDelivery;
import org.rutebanken.sobek.rest.ParameterDto.ExportParametersDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
@RestController
@RequestMapping("/services/vehicles/netex")
@Slf4j
public class VehicleExportController {

    @Qualifier("syncStreamingPublicationDelivery")
    @Autowired
    private StreamingPublicationDelivery streamingPublicationDelivery;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getVehicleNetex(@ModelAttribute ExportParametersDto exportParams) {
        log.info("Exporting publication delivery. {}", exportParams);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamVehicles(exportParams.toExportParams(), outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok(streamingResponseBody);
    }

    @GetMapping(path = "deckplans/{netexId}", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getOneDeckPlan(@ModelAttribute ExportParametersDto exportParams, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery. {}", exportParams);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneDeckPlan(exportParams.toExportParams(), netexId, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok(streamingResponseBody);
    }

    @GetMapping(path = "vehicles/{netexId}", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getOneVehicle(@ModelAttribute ExportParametersDto exportParams, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery for Vehicle NeTEx id {}. {}", netexId, exportParams);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneVehicle(exportParams.toExportParams(), netexId, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream vehicle. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok(streamingResponseBody);
    }
}