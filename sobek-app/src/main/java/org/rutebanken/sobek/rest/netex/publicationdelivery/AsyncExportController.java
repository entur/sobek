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

import org.rutebanken.sobek.exporter.AsyncPublicationDeliveryExporter;
import org.rutebanken.sobek.model.job.ExportJob;
import org.rutebanken.sobek.model.job.ExportParams;
import org.rutebanken.sobek.model.job.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Collection;

/**
 * Export publication delivery data to google cloud storage. Some parts like stops and parking asynchronously
 */
//@Tag(name="asyncExport", description = "Async export resource")
@RestController
@RequestMapping("/services/vehicles/netex/async")
public class AsyncExportController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExportController.class);

    private final AsyncPublicationDeliveryExporter asyncPublicationDeliveryExporter;

    @Autowired
    public AsyncExportController(AsyncPublicationDeliveryExporter asyncPublicationDeliveryExporter) {
        this.asyncPublicationDeliveryExporter = asyncPublicationDeliveryExporter;
    }

    @GetMapping
    public Collection<ExportJob> getAsyncExportJobs() {
        return asyncPublicationDeliveryExporter.getJobs();
    }

    @GetMapping(path = "{id}/status", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<ExportJob> getAsyncExportJob(@PathVariable(value = "id") long exportJobId) {

        ExportJob exportJob = asyncPublicationDeliveryExporter.getExportJob(exportJobId);

        if (exportJob == null) {
            return ResponseEntity.notFound().build();
        }

        logger.info("Returning job {}", exportJob);
        return ResponseEntity.ok(exportJob);
    }

    @GetMapping(path = "{id}/content", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity getAsyncExportJobContents(@PathVariable(value = "id") long exportJobId) {

        ExportJob exportJob = asyncPublicationDeliveryExporter.getExportJob(exportJobId);

        if (exportJob == null) {
            return ResponseEntity.notFound().build();
        }

        logger.info("Returning result of job {}", exportJob);
        if (!exportJob.getStatus().equals(JobStatus.FINISHED)) {
            return ResponseEntity.accepted().body("Job status is not FINISHED for job: " + exportJob);
        }

        InputStream inputStream = asyncPublicationDeliveryExporter.getJobFileContent(exportJob);
        return ResponseEntity.ok(inputStream);
    }

    @GetMapping(path = "initiate", produces = MediaType.APPLICATION_XML_VALUE + "; charset=UTF-8")
    public ResponseEntity<ExportJob> asyncExport(@ModelAttribute ExportParams exportParams) {
        ExportJob exportJob = asyncPublicationDeliveryExporter.startExportJob(exportParams);
        return ResponseEntity.ok(exportJob);
    }
}
