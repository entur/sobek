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

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.sobek.importer.ImportType;
import org.rutebanken.sobek.importer.PublicationDeliveryImporter;
import org.rutebanken.sobek.netex.marshal.PublicationDeliveryUnmarshaller;
import org.rutebanken.sobek.rest.ParameterDto.ImportParametersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Import publication deliveries
 */
@RestController
@RequestMapping("/services/vehicles/netex")
public class VehicleImportController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleImportController.class);

    private final PublicationDeliveryUnmarshaller publicationDeliveryUnmarshaller;

    private final PublicationDeliveryStreamingOutput publicationDeliveryStreamingOutput;

    private final PublicationDeliveryImporter publicationDeliveryImporter;

    private final Set<ImportType> enabledImportTypes;

    @Inject
    public VehicleImportController(PublicationDeliveryUnmarshaller publicationDeliveryUnmarshaller,
                                 PublicationDeliveryStreamingOutput publicationDeliveryStreamingOutput,
                                 PublicationDeliveryImporter publicationDeliveryImporter,
                                 @Value("#{'${netex.import.enabled.types:ID_MATCH}'.split(',')}") Set<ImportType> enabledImportTypes) {

        this.publicationDeliveryUnmarshaller = publicationDeliveryUnmarshaller;
        this.publicationDeliveryStreamingOutput = publicationDeliveryStreamingOutput;
        this.publicationDeliveryImporter = publicationDeliveryImporter;
        this.enabledImportTypes = enabledImportTypes;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<StreamingResponseBody> importPublicationDelivery(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "MERGE") String importType,
            @RequestParam(required = false, defaultValue = "false") boolean skipOutput) throws IOException, JAXBException, SAXException {
        
        logger.info("Received Netex publication delivery, starting to parse...");
        
        // Create ImportParametersDto from request parameters
        ImportParametersDto importParams = new ImportParametersDto();
        importParams.importType = importType;
        importParams.skipOutput = skipOutput;
        
        ImportType effectiveImportType = safeGetImportType(importParams);
        if (!enabledImportTypes.contains(effectiveImportType)) {
            String error = "ImportType: " + effectiveImportType + " not enabled!";
            logger.warn(error);
            StreamingResponseBody errorBody = outputStream -> {
                outputStream.write(error.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            };
            return ResponseEntity.badRequest().body(errorBody);
        }

        // Get InputStream directly from HttpServletRequest
        try (InputStream inputStream = request.getInputStream()) {
            PublicationDeliveryStructure incomingPublicationDelivery = publicationDeliveryUnmarshaller.unmarshal(inputStream);
            try {
                PublicationDeliveryStructure responsePublicationDelivery;
                responsePublicationDelivery = publicationDeliveryImporter.importPublicationDelivery(incomingPublicationDelivery, importParams.toImportParams());
                if (importParams.skipOutput) {
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.ok(publicationDeliveryStreamingOutput.stream(responsePublicationDelivery));
                }


            } catch (NotAuthenticatedException  e) {
                logger.debug("Access denied for publication delivery: " + e.getMessage(), e);
                throw e;
            } catch (RuntimeException e) {
                logger.warn("Caught exception while importing publication delivery: " + incomingPublicationDelivery, e);
                throw e;
            }
        }
    }

    /**
     * Return specified ImportType or default value if not set.
     */
    private ImportType safeGetImportType(ImportParametersDto importParams) {
        if (importParams == null || importParams.importType == null) {
            return ImportType.valueOf(new ImportParametersDto().importType);
        }
        return ImportType.valueOf(importParams.importType);
    }

}
