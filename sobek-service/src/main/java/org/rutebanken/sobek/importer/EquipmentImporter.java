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

import org.rutebanken.sobek.model.vehicle.Equipment;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.EquipmentMapper;
import org.rutebanken.sobek.versioning.save.EquipmentVersionedSaverService;
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
public class EquipmentImporter {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentImporter.class);

    private final EquipmentMapper equipmentMapper;

    private final EquipmentVersionedSaverService equipmentVersionedSaverService;
    private final MappingContext mappingContext;

    @Autowired
    public EquipmentImporter(EquipmentMapper equipmentMapper, EquipmentVersionedSaverService equipmentVersionedSaverService, MappingContext mappingContext) {
        this.equipmentMapper = equipmentMapper;
        this.equipmentVersionedSaverService = equipmentVersionedSaverService;
        this.mappingContext = mappingContext;
    }

    public List<org.rutebanken.netex.model.Equipment_VersionStructure> importEquipments(List<Equipment> equipments, AtomicInteger equipmentsCounter) {

        logger.info("Importing {} incoming equipment", equipments.size());

        List<Equipment> result = new ArrayList<>();
        logger.info("Importing deck plans");
        for (Equipment incomingEquipment : equipments) {
            result.add(importEquipment(incomingEquipment, equipmentsCounter));
        }

        return result.stream().map(e -> equipmentMapper.mapToNetexManual(e, mappingContext)).collect(toList());
    }

    private Equipment importEquipment(Equipment incomingEquipment, AtomicInteger equipmentsCounter) {
        logger.debug("{}", incomingEquipment);
        incomingEquipment = equipmentVersionedSaverService.saveNewVersion(incomingEquipment);

        equipmentsCounter.incrementAndGet();
        return incomingEquipment;
    }

}
