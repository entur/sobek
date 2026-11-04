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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public ResponseEntity<StreamingResponseBody> getVehicleNetex(@ModelAttribute ExportParametersDto exportParams, BindingResult bindingResult) {
        log.info("Exporting publication delivery.");

        // Verify before streaming starts, so bad input gives 400 instead of a failed 200
        verifyBinding(bindingResult);
        if (exportParams.getVehicleTypeKind() == null) {
            throw new ValidationException("vehicleTypeKind is required: PHYSICAL, CONTRACTUAL or BOTH");
        }
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
    public ResponseEntity<StreamingResponseBody> getOneDeckPlan(@ModelAttribute ExportParametersDto exportParams, BindingResult bindingResult, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery. {}", exportParams);

        // Verify before streaming starts, so access denied gives 403 instead of a failed 200
        verifyBinding(bindingResult);
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
    public ResponseEntity<StreamingResponseBody> getOneVehicle(@ModelAttribute ExportParametersDto exportParams, BindingResult bindingResult, @PathVariable("netexId") String netexId) {
        log.info("Exporting publication delivery for Vehicle NeTEx id {}. {}", netexId, exportParams);

        // Verify before streaming starts, so access denied gives 403 instead of a failed 200
        verifyBinding(bindingResult);
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
     * A request parameter that cannot be bound, e.g. a vehicleTypeKind that is not PHYSICAL, CONTRACTUAL or BOTH, is a
     * client error. Without a BindingResult next to the model attribute Spring throws a binding exception that no
     * handler classifies, which would end up as an HTTP 500.
     */
    private static void verifyBinding(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            return;
        }
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> describe(bindingResult, error))
                .collect(Collectors.joining("; "));
        throw new ValidationException(message.isEmpty() ? "Invalid request parameters" : message);
    }

    private static String describe(BindingResult bindingResult, FieldError error) {
        Class<?> fieldType = bindingResult.getFieldType(error.getField());
        String allowedValues = fieldType != null && fieldType.isEnum()
                ? " Allowed values: " + Arrays.stream(fieldType.getEnumConstants()).map(String::valueOf).collect(Collectors.joining(", "))
                : "";
        return "Invalid value '" + error.getRejectedValue() + "' for parameter '" + error.getField() + "'." + allowedValues;
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