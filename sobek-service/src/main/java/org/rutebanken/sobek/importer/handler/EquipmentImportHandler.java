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

package org.rutebanken.sobek.importer.handler;

import jakarta.xml.bind.JAXBElement;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.importer.EquipmentImporter;
import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.importer.converter.EquipmentIdConverter;
import org.rutebanken.sobek.netex.mapping.EquipmentMappingHelper;
import org.rutebanken.sobek.netex.mapping.NetexMapper;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class EquipmentImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentImportHandler.class);

    private final PublicationDeliveryHelper publicationDeliveryHelper;

    private final NetexMapper netexMapper;

    private final EquipmentImporter equipmentImporter;
    private final EquipmentIdConverter equipmentIdConverter;
    private final EquipmentMappingHelper equipmentMappingHelper;

    public EquipmentImportHandler(PublicationDeliveryHelper publicationDeliveryHelper,
                                  NetexMapper netexMapper,
                                  EquipmentImporter equipmentImporter, EquipmentIdConverter equipmentIdConverter, EquipmentMappingHelper equipmentMappingHelper) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.netexMapper = netexMapper;
        this.equipmentImporter = equipmentImporter;
        this.equipmentIdConverter = equipmentIdConverter;
        this.equipmentMappingHelper = equipmentMappingHelper;
    }

    public void handleEquipments(ResourceFrame netexResourceFrame, ImportParams importParams, AtomicInteger equipmentsCounter, ResourceFrame responseResourceframe) {

        if (publicationDeliveryHelper.hasEquipments(netexResourceFrame)) {
            var originalEquipments = netexResourceFrame.getEquipments().getEquipment();
            logger.info("Publication delivery contains {} equipments for import.", originalEquipments.size());

            logger.info("About to check if incoming equipments have previously been imported with the same id");
            var originalWithMappedIds = originalEquipments.stream()
                    .filter(this::isEquipment)
                    .map(jaxbElement -> (Equipment_VersionStructure) jaxbElement.getValue())
                    .map(equipmentIdConverter::convertIncomingId);

            logger.info("About to map {} equipments to internal model", netexResourceFrame.getEquipments().getEquipment().size());
            List<org.rutebanken.sobek.model.vehicle.Equipment> mappedEquipments = originalWithMappedIds
                    .map(netexMapper::mapToSobekModel)
                    .collect(Collectors.toList());
            logger.info("Mapped {} equipments to internal model", mappedEquipments.size());

            List<Equipment_VersionStructure> importedEquipments = equipmentImporter.importEquipments(mappedEquipments, equipmentsCounter);

            List<JAXBElement<? extends Equipment_VersionStructure>> equipmentElements = importedEquipments.stream()
                    .map(equipmentMappingHelper::mapToJaxbEquipment)
                    .collect(java.util.stream.Collectors.toList());

            responseResourceframe.withEquipments(
                    new EquipmentsInFrame_RelStructure()
                            .withEquipment(
                                    equipmentElements));

            logger.info("Finished importing equipments");
        }
    }
    private boolean isEquipment(JAXBElement<? extends DataManagedObjectStructure> jaxbElement) {
        var value = jaxbElement.getValue();
        return value instanceof AccessVehicleEquipment ||
                value instanceof BedEquipment ||
                value instanceof EntranceEquipment ||
                value instanceof LuggageSpotEquipment ||
                value instanceof SpotEquipment ||
                value instanceof SeatEquipment ||
                value instanceof StaircaseEquipment;
    }
}
