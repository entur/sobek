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

package org.rutebanken.sobek.config;

import org.rutebanken.sobek.exporter.*;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.*;
import org.rutebanken.sobek.netex.mapping.mapstruct.deckplan.DeckPlanMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.EquipmentMapper;
import org.rutebanken.sobek.repository.*;
import org.rutebanken.sobek.repository.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;

@Configuration
public class StreamingPublicationDeliveryConfig {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private DeckPlanRepository deckPlanRepository;

    @Autowired
    private VehicleEquipmentProfileRepository vehicleEquipmentProfileRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private PublicationDeliveryCreator publicationDeliveryCreator;

    @Autowired
    private SobekServiceFrameExporter sobekServiceFrameExporter;

    @Autowired
    private SobekResourceFrameExporter sobekResourceFrameExporter;

    @Autowired
    private SobekComositeFrameExporter sobekComositeFrameExporter;

    @Autowired
    private NetexIdHelper netexIdHelper;

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private DeckPlanMapper deckPlanMapper;

    @Autowired
    private VehicleModelMapper vehicleModelMapper;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Value("${asyncNetexExport.validateAgainstSchema:false}")
    private boolean validateAsyncExport;

    @Value("${syncNetexExport.validateAgainstSchema:true}")
    private boolean validateSyncExport;

    @Autowired
    private MappingContext mappingContext;

//    @Bean("asyncStreamingPublicationDelivery") TODO
//    public StreamingPublicationDelivery asyncStreamingPublicationDelivery() throws IOException, SAXException {
//        return createStreamingPublicationDelivery(validateAsyncExport);
//    }

    @Bean("syncStreamingPublicationDelivery")
    public StreamingPublicationDelivery syncStreamingPublicationDelivery() throws IOException, SAXException {
        return createStreamingPublicationDelivery(validateSyncExport);
    }

    private StreamingPublicationDelivery createStreamingPublicationDelivery(boolean validate) throws IOException, SAXException {
        return new StreamingPublicationDelivery(vehicleRepository, vehicleTypeRepository, vehicleModelRepository, deckPlanRepository, vehicleEquipmentProfileRepository, equipmentRepository, publicationDeliveryCreator,
                sobekServiceFrameExporter, vehicleTypeMapper, equipmentMapper, vehicleMapper, deckPlanMapper, vehicleModelMapper, referenceResolver, mappingContext, sobekResourceFrameExporter, sobekComositeFrameExporter
                 /* TODO validate,*/ );
    }
}
