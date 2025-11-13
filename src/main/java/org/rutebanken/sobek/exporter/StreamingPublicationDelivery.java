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

package org.rutebanken.sobek.exporter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.hibernate.internal.SessionImpl;
import org.rutebanken.netex.model.DeckPlans_RelStructure;
import org.rutebanken.netex.model.EquipmentsInFrame_RelStructure;
import org.rutebanken.netex.model.Frames_RelStructure;
import org.rutebanken.netex.model.InstalledEquipment_VersionStructure;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.SanitaryEquipment;
import org.rutebanken.netex.model.TicketingEquipment;
import org.rutebanken.netex.model.VehicleEquipmentProfilesInFrame_RelStructure;
import org.rutebanken.netex.model.VehicleModelsInFrame_RelStructure;
import org.rutebanken.netex.model.VehicleTypesInFrame_RelStructure;
import org.rutebanken.netex.model.VehiclesInFrame_RelStructure;
import org.rutebanken.netex.validation.NeTExValidator;
import org.rutebanken.sobek.exporter.async.NetexMappingIterator;
import org.rutebanken.sobek.exporter.async.NetexMappingIteratorList;
import org.rutebanken.sobek.exporter.eviction.EntitiesEvictor;
import org.rutebanken.sobek.exporter.eviction.SessionEntitiesEvictor;
import org.rutebanken.sobek.exporter.params.ExportParams;
import org.rutebanken.sobek.model.ResourceFrame;
import org.rutebanken.sobek.model.ServiceFrame;
import org.rutebanken.sobek.model.vehicle.CompositeFrame;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfile;
import org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfileMember;
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.mapping.NetexMapper;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.InstalledEquipmentRepository;
import org.rutebanken.sobek.repository.VehicleEquipmentProfileRepository;
import org.rutebanken.sobek.repository.VehicleModelRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static jakarta.xml.bind.JAXBContext.newInstance;

/**
 * Stream data objects inside already serialized publication delivery.
 * To be able to export many stop places wihtout keeping them all in memory.
 */
@Transactional(readOnly = true)
@Component
public class StreamingPublicationDelivery {

    private static final Logger logger = LoggerFactory.getLogger(StreamingPublicationDelivery.class);

    private static final JAXBContext publicationDeliveryContext = createContext(PublicationDeliveryStructure.class);
    private static final ObjectFactory netexObjectFactory = new ObjectFactory();

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final DeckPlanRepository deckPlanRepository;
    private final VehicleEquipmentProfileRepository vehicleEquipmentProfileRepository;
    private final InstalledEquipmentRepository installedEquipmentRepository;
    private final PublicationDeliveryCreator publicationDeliveryCreator;
    private final SobekServiceFrameExporter sobekServiceFrameExporter;
    
    private final SobekResourceFrameExporter sobekResourceFrameExporter;
    private final NetexMapper netexMapper;
    private final SobekComositeFrameExporter sobekComositeFrameExporter;
    private NeTExValidator neTExValidator; //TODO
    /**
     * Validate against netex schema using the {@link NeTExValidator}
     * Enabling this for large xml files can lead to high memory consumption and/or massive performance impact.
     */
    private final boolean validateAgainstSchema;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public StreamingPublicationDelivery(VehicleRepository vehicleRepository,
                                        VehicleTypeRepository vehicleTypeRepository,
                                        VehicleModelRepository vehicleModelRepository,
                                        DeckPlanRepository deckPlanRepository,
                                        VehicleEquipmentProfileRepository vehicleEquipmentProfileRepository,
                                        InstalledEquipmentRepository installedEquipmentRepository,
                                        PublicationDeliveryCreator publicationDeliveryCreator,
                                        SobekServiceFrameExporter sobekServiceFrameExporter,
                                        SobekResourceFrameExporter sobekResourceFrameExporter,
                                        SobekComositeFrameExporter sobekComositeFrameExporter,
                                        NetexMapper netexMapper
//                          TODO              @Value("${asyncNetexExport.validateAgainstSchema:false}") boolean validateAgainstSchema,
                                        ) throws IOException, SAXException {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.deckPlanRepository = deckPlanRepository;
        this.vehicleEquipmentProfileRepository = vehicleEquipmentProfileRepository;
        this.installedEquipmentRepository = installedEquipmentRepository;
        this.publicationDeliveryCreator = publicationDeliveryCreator;
        this.sobekServiceFrameExporter = sobekServiceFrameExporter;
        this.sobekResourceFrameExporter = sobekResourceFrameExporter;
        this.sobekComositeFrameExporter = sobekComositeFrameExporter;
        this.netexMapper = netexMapper;
        this.validateAgainstSchema = false; //TODO  validateAgainstSchema;
    }

    private static JAXBContext createContext(Class clazz) {
        try {
            JAXBContext jaxbContext = newInstance(clazz);
            logger.info("Created context {}", jaxbContext.getClass());
            return jaxbContext;
        } catch (JAXBException e) {
            String message = "Could not create instance of jaxb context for class " + clazz;
            logger.warn(message, e);
            throw new RuntimeException("Could not create instance of jaxb context for class " + clazz, e);
        }
    }

    public void streamVehicles(ExportParams exportParams, OutputStream outputStream) throws JAXBException, XMLStreamException, IOException, InterruptedException, SAXException {

        final CompositeFrame compositeFrame = sobekComositeFrameExporter.createSobekCompositeFrame("Composite frame " + exportParams);
        final ResourceFrame resourceFrame = sobekResourceFrameExporter.createSobekResourceFrame("Resource frame"+ exportParams);

        AtomicInteger mappedVehicleCount = new AtomicInteger();
        AtomicInteger mappedVehicleTypeCount = new AtomicInteger();
        AtomicInteger mappedVehicleModelCount = new AtomicInteger();
        AtomicInteger mappedTrainCount = new AtomicInteger();

        EntitiesEvictor entitiesEvictor = instantiateEvictor();

        logger.info("Streaming export initiated. Export params: {}", exportParams);

        // We need to know these IDs before marshalling begins.
        // To avoid marshalling empty parking element and to be able to gather relevant topographic places
        // The primary ID represents a stop place with a certain version


        logger.info("Mapping composite frame to netex model");
        org.rutebanken.netex.model.CompositeFrame netexCompositeFrame = netexMapper.mapToNetexModel(compositeFrame);

        logger.info("Mapping resource frame to netex model");
        final org.rutebanken.netex.model.ResourceFrame netexResourceFrame = netexMapper.mapToNetexModel(resourceFrame);

        Frames_RelStructure framesRelStructure = new Frames_RelStructure();
        framesRelStructure.withCommonFrame(new ObjectFactory().createResourceFrame(netexResourceFrame));
        netexCompositeFrame.withFrames(framesRelStructure);

        logger.info("Preparing scrollable iterators");
        prepareVehicleTypes(exportParams, Collections.emptySet(), mappedVehicleTypeCount, netexResourceFrame, entitiesEvictor);
        prepareVehicleModels(exportParams, Collections.emptySet(), mappedVehicleModelCount, netexResourceFrame, entitiesEvictor);
        prepareVehicles(exportParams, Collections.emptySet(), mappedVehicleCount, netexResourceFrame, entitiesEvictor);
        prepareDeckPlans(netexResourceFrame);
        prepareEquipments(netexResourceFrame);

        PublicationDeliveryStructure publicationDeliveryStructure = publicationDeliveryCreator.createPublicationDelivery(netexCompositeFrame);

        Marshaller marshaller = createMarshaller();

        logger.info("Start marshalling publication delivery");
        marshaller.marshal(netexObjectFactory.createPublicationDelivery(publicationDeliveryStructure), outputStream);
        logger.info("Mapped {} vehicles, {} vehicleTypes, and {} trains to netex",
                mappedVehicleCount.get(),
                mappedVehicleTypeCount.get(),
                mappedTrainCount.get());

    }



    private void prepareVehicles(ExportParams exportParams, Set<Long> vehicleIds, AtomicInteger mappedVehicleCount, org.rutebanken.netex.model.ResourceFrame resourceFrame, EntitiesEvictor evicter) {
        // Override lists with custom iterator to be able to scroll database results on the fly.
//        if (!vehicleIds.isEmpty()) {

        List<Vehicle> vehiclesInDb = vehicleRepository.findAll();

        if (!vehiclesInDb.isEmpty()) {
            logger.info("There are vehicles to export");

            VehiclesInFrame_RelStructure vehiclesInFrame_relStructure = new VehiclesInFrame_RelStructure();

            List<org.rutebanken.netex.model.Vehicle> vehicles = new NetexMappingIteratorList<>(() -> new NetexMappingIterator<>(netexMapper, vehiclesInDb.iterator(),
                    org.rutebanken.netex.model.Vehicle.class, mappedVehicleCount, evicter));

            setField(VehiclesInFrame_RelStructure.class, "vehicle", vehiclesInFrame_relStructure, vehicles);
            resourceFrame.setVehicles(vehiclesInFrame_relStructure);
        } else {
            logger.info("No vehicles to export");
        }
    }

    private void prepareVehicleTypes(ExportParams exportParams, Set<Long> vehicleIds, AtomicInteger mappedVehicleTypeCount, org.rutebanken.netex.model.ResourceFrame resourceFrame, EntitiesEvictor evicter) {
        // Override lists with custom iterator to be able to scroll database results on the fly.
//        if (!vehicleIds.isEmpty()) {

        List<VehicleType> vehicleTypesInDb = vehicleTypeRepository.findAll();

        if (!vehicleTypesInDb.isEmpty()) {
            logger.info("There are vehicle types to export");

            VehicleTypesInFrame_RelStructure vehicleTypesInFrameRelStructure = new VehicleTypesInFrame_RelStructure();
            List<org.rutebanken.netex.model.VehicleType> vehicleTypes = vehicleTypesInDb.stream().map(vt -> netexMapper.getFacade().map(vt, org.rutebanken.netex.model.VehicleType.class)).toList();

            List<JAXBElement<org.rutebanken.netex.model.VehicleType>> jaxbVehicleTypes = vehicleTypes.stream().map(vt -> new ObjectFactory().createVehicleType(vt)).toList();
            setField(VehicleTypesInFrame_RelStructure.class, "transportType_DummyType", vehicleTypesInFrameRelStructure, jaxbVehicleTypes);
            resourceFrame.setVehicleTypes(vehicleTypesInFrameRelStructure);
        } else {
            logger.info("No vehicle types to export");
        }
    }

    private void prepareVehicleModels(ExportParams exportParams, Set<Long> vehicleIds, AtomicInteger mappedVehicleModelCount, org.rutebanken.netex.model.ResourceFrame resourceFrame, EntitiesEvictor evicter) {
        // Override lists with custom iterator to be able to scroll database results on the fly.
//        if (!vehicleIds.isEmpty()) {

        List<VehicleModel> vehicleModelsInDb = vehicleModelRepository.findAll();

        if (!vehicleModelsInDb.isEmpty()) {
            logger.info("There are vehicle models to export");

            VehicleModelsInFrame_RelStructure vehicleModelsInFrameRelStructure = new VehicleModelsInFrame_RelStructure();
            List<org.rutebanken.netex.model.VehicleModel> vehicleModels = vehicleModelsInDb.stream().map(vt -> netexMapper.getFacade().map(vt, org.rutebanken.netex.model.VehicleModel.class)).toList();

            setField(VehicleModelsInFrame_RelStructure.class, "vehicleModel", vehicleModelsInFrameRelStructure, vehicleModels);
            resourceFrame.setVehicleModels(vehicleModelsInFrameRelStructure);
        } else {
            logger.info("No vehicle models to export");
        }
    }

    private void prepareDeckPlans(org.rutebanken.netex.model.ResourceFrame resourceFrame) {
        // Override lists with custom iterator to be able to scroll database results on the fly.
//        if (!vehicleIds.isEmpty()) {

        List<DeckPlan> deckPlansInDb = deckPlanRepository.findAll();

        if (!deckPlansInDb.isEmpty()) {
            logger.info("There are deck plans to export");

            DeckPlans_RelStructure deckPlansRelStructure = new DeckPlans_RelStructure();

            List<org.rutebanken.netex.model.DeckPlan> deckPlans = deckPlansInDb.stream().map(vt -> netexMapper.getFacade().map(vt, org.rutebanken.netex.model.DeckPlan.class)).toList();

            setField(DeckPlans_RelStructure.class, "deckPlan", deckPlansRelStructure, deckPlans);
            resourceFrame.setDeckPlans(deckPlansRelStructure);
        } else {
            logger.info("No deck plans to export");
        }
    }

    private void prepareEquipments(org.rutebanken.netex.model.ResourceFrame resourceFrame) {

        List<VehicleEquipmentProfile> equipmentProfilesInDb = vehicleEquipmentProfileRepository.findAll();

        if (!equipmentProfilesInDb.isEmpty()) {
            logger.info("There are vehicle equipment profiles to export");

            VehicleEquipmentProfilesInFrame_RelStructure vehicleEquipmentProfilesInFrameRelStructure = new VehicleEquipmentProfilesInFrame_RelStructure();

            List<org.rutebanken.netex.model.VehicleEquipmentProfile> equipmentProfiles = equipmentProfilesInDb.stream().map(vt -> netexMapper.getFacade().map(vt, org.rutebanken.netex.model.VehicleEquipmentProfile.class)).toList();

            setField(VehicleEquipmentProfilesInFrame_RelStructure.class, "vehicleEquipmentProfileOrRechargingEquipmentProfile", vehicleEquipmentProfilesInFrameRelStructure, equipmentProfiles);
            resourceFrame.setVehicleEquipmentProfiles(vehicleEquipmentProfilesInFrameRelStructure);

            List<org.rutebanken.sobek.model.InstalledEquipment_VersionStructure> equipmentsInDb = equipmentProfilesInDb.stream()
                    .flatMap(ep -> ep.getVehicleEquipmentProfileMembers().stream())
                            .map(VehicleEquipmentProfileMember::getEquipmentRef)
                                    .flatMap(ref -> installedEquipmentRepository.findByNetexId(ref).stream()).toList(); //TODO - optimize :)

            EquipmentsInFrame_RelStructure equipmentsInFrameRelStructure = new EquipmentsInFrame_RelStructure();

            List<? extends JAXBElement<? extends InstalledEquipment_VersionStructure>> netexEquipment =
                    equipmentsInDb.stream().map(e -> netexMapper.getFacade().map(e, InstalledEquipment_VersionStructure.class))
                            .map(this::mapToJaxbInstalledEquipment)
                            .toList();

            setField(EquipmentsInFrame_RelStructure.class, "equipment", equipmentsInFrameRelStructure, netexEquipment);
            resourceFrame.setEquipments(equipmentsInFrameRelStructure);

        } else {
            logger.info("No vehicle equipment profiles to export");
        }
    }

    public JAXBElement<? extends InstalledEquipment_VersionStructure> mapToJaxbInstalledEquipment(org.rutebanken.netex.model.InstalledEquipment_VersionStructure netexEquipment) {
        ObjectFactory objectFactory = new ObjectFactory();

        return switch (netexEquipment) {
            case SanitaryEquipment sanitaryEquipment -> objectFactory.createSanitaryEquipment(sanitaryEquipment);
            case TicketingEquipment ticketingEquipment -> objectFactory.createTicketingEquipment(ticketingEquipment);
            default -> null;
        };
    }

    public void stream(ExportParams exportParams, OutputStream outputStream) throws JAXBException, XMLStreamException, IOException, InterruptedException, SAXException {
        stream(exportParams, outputStream, false);
    }

    public void stream(ExportParams exportParams, OutputStream outputStream, boolean ignorePaging) throws JAXBException, XMLStreamException, IOException, InterruptedException, SAXException {

        final ServiceFrame serviceFrame = sobekServiceFrameExporter.createSobekServiceFrame("Service frame " + exportParams);

        final ResourceFrame resourceFrame = sobekResourceFrameExporter.createSobekResourceFrame("Resource frame"+ exportParams);


        EntitiesEvictor entitiesEvictor = instantiateEvictor();

        logger.info("Streaming export initiated. Export params: {}", exportParams);

        // We need to know these IDs before marshalling begins.
        // To avoid marshalling empty parking element and to be able to gather relevant topographic places
        // The primary ID represents a stop place with a certain version

        logger.info("Mapping service frame to netex model");
        final org.rutebanken.netex.model.ServiceFrame netexServiceFrame = netexMapper.mapToNetexModel(serviceFrame);

        logger.info("Mapping resource frame to netex model");
        final org.rutebanken.netex.model.ResourceFrame netexResourceFrame = netexMapper.mapToNetexModel(resourceFrame);


        PublicationDeliveryStructure publicationDeliveryStructure;
        publicationDeliveryStructure = publicationDeliveryCreator.createPublicationDelivery(netexServiceFrame, netexResourceFrame);


        Marshaller marshaller = createMarshaller();

        logger.info("Start marshalling publication delivery");
        marshaller.marshal(netexObjectFactory.createPublicationDelivery(publicationDeliveryStructure), outputStream);

    }

    private EntitiesEvictor instantiateEvictor() {
        if (entityManager != null) {
            SessionImpl currentSession = entityManager.unwrap(SessionImpl.class);
            return new SessionEntitiesEvictor(currentSession);
        } else {
            return new EntitiesEvictor() {
                @Override
                public void evictKnownEntitiesFromSession(Object entity) {
                    // Intentionally left blank
                }
            };
        }
    }

    /**
     * Set field value with reflection.
     * Used for setting list values in netex model.
     */
    private void setField(Class clazz, String fieldName, Object instance, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Cannot set field " + fieldName + " of " + instance, e);
        }
    }

    private Marshaller createMarshaller() throws JAXBException, IOException, SAXException {
        Marshaller marshaller = publicationDeliveryContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");

        if (validateAgainstSchema) {
            neTExValidator = NeTExValidator.getNeTExValidator();
            marshaller.setSchema(neTExValidator.getSchema());
        }

        return marshaller;
    }
}
