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

package org.rutebanken.sobek.importer;

import org.rutebanken.sobek.model.vehicle.SchematicMap;
import org.rutebanken.sobek.netex.mapping.mapstruct.SchematicMapMapper;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.versioning.save.SchematicMapVersionedSaverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@Transactional
@Component
public class SchematicMapImporter {

    private static final Logger logger = LoggerFactory.getLogger(SchematicMapImporter.class);

    private final SchematicMapMapper schematicMapMapper;

    private final SchematicMapVersionedSaverService schematicMapVersionedSaverService;

    private final MappingContext context;

    @Autowired
    public SchematicMapImporter(SchematicMapMapper schematicMapMapper, SchematicMapVersionedSaverService schematicMapVersionedSaverService, MappingContext context) {
        this.schematicMapMapper = schematicMapMapper;
        this.schematicMapVersionedSaverService = schematicMapVersionedSaverService;
        this.context = context;
    }

    public List<org.rutebanken.netex.model.SchematicMap> importSchematicMaps(List<SchematicMap> schematicMaps, AtomicInteger schematicMapsCounter) {

        logger.info("Importing {} incoming schematic maps", schematicMaps.size());

        List<SchematicMap> result = new ArrayList<>();

        logger.info("Importing schematic maps");
        for (SchematicMap incomingSchematicMap : schematicMaps) {
            result.add(importSchematicMap(incomingSchematicMap, schematicMapsCounter));
        }

        return result.stream().map(schematicMap -> schematicMapMapper.mapToNetex(schematicMap, context)).collect(toList());
    }

    private SchematicMap importSchematicMap(SchematicMap incomingSchematicMap, AtomicInteger schematicMapsCounter) {
        logger.debug("{}", incomingSchematicMap);
        incomingSchematicMap = schematicMapVersionedSaverService.saveNewVersion(incomingSchematicMap);

        schematicMapsCounter.incrementAndGet();
        return incomingSchematicMap;
    }

}
