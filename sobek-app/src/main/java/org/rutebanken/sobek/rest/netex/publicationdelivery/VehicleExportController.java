package org.rutebanken.sobek.rest.netex.publicationdelivery;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.rutebanken.sobek.auth.AuthorizationService;
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

    @Autowired
    AuthorizationService authorizationService;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getVehicleNetex(@ModelAttribute ExportParametersDto exportParams) {
        log.info("Exporting publication delivery.");

        authorizationService.verifyCanViewDataOwner(exportParams.getDataOwner());

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamVehicles(exportParams.toExportParams(), outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8"))
                .body(streamingResponseBody);
    }

    @GetMapping(path = "deckplans/{netexId}", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getOneDeckPlan(@ModelAttribute ExportParametersDto exportParams, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery. {}", exportParams);

        // Verify before streaming starts, so access denied gives 403 instead of a failed 200
        verifyCanExportSingleObject(exportParams.getDataOwner());

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneDeckPlan(exportParams.toExportParams(), netexId, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream composite frame. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8"))
                .body(streamingResponseBody);
    }

    @GetMapping(path = "vehicles/{netexId}", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> getOneVehicle(@ModelAttribute ExportParametersDto exportParams, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery for Vehicle NeTEx id {}. {}", netexId, exportParams);

        // Verify before streaming starts, so access denied gives 403 instead of a failed 200
        verifyCanExportSingleObject(exportParams.getDataOwner());

        StreamingResponseBody streamingResponseBody = outputStream -> {
            try {
                streamingPublicationDelivery.streamOneVehicle(exportParams.toExportParams(), netexId, outputStream);
            } catch (Exception e) {
                log.warn("Could not stream vehicle. {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8"))
                .body(streamingResponseBody);
    }

    /**
     * Single objects are looked up by id + data owner, which is required for everyone, admins included.
     * Only the requested data owner is checked here, never the object, so that the response does not reveal whether
     * an object exists in a data owner the user cannot access
     */
    private void verifyCanExportSingleObject(String dataOwnerRef) {
        if (dataOwnerRef == null || dataOwnerRef.isBlank()) {
            throw new ValidationException("dataOwner is required");
        }
        authorizationService.verifyCanViewDataOwner(dataOwnerRef);
    }


}