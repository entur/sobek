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

package org.rutebanken.sobek.netex.mapping;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetexMapper {
    private static final Logger logger = LoggerFactory.getLogger(NetexMapper.class);
    private final MapperFacade facade;

    @Autowired
    public NetexMapper(List<Converter> converters, KeyListToKeyValuesMapMapper keyListToKeyValuesMapMapper,
                       DataManagedObjectStructureMapper dataManagedObjectStructureMapper,
                       PublicationDeliveryHelper publicationDeliveryHelper,
                       AccessibilityAssessmentMapper accessibilityAssessmentMapper) {

        logger.info("Setting up netexMapper with DI");

        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        logger.info("Creating netex mapperFacade with {} converters ", converters.size());

        if(logger.isDebugEnabled()) {
            logger.debug("Converters: {}", converters);
        }

        converters.forEach(converter -> mapperFactory.getConverterFactory().registerConverter(converter));

        // Issues with registering multiple mappers
        mapperFactory.registerMapper(keyListToKeyValuesMapMapper);

        mapperFactory.classMap(MultilingualString.class, org.rutebanken.sobek.model.EmbeddableMultilingualString.class)
                .customize(new MultilingualStringMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(Vehicle.class, org.rutebanken.sobek.model.vehicle.Vehicle.class)
                .exclude("transportTypeRef")
                .exclude("vehicleTypeRef")
                .customize(new VehicleMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(VehicleType.class, org.rutebanken.sobek.model.vehicle.VehicleType.class)
                .customize(new VehicleTypeMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(VehicleModel.class, org.rutebanken.sobek.model.vehicle.VehicleModel.class)
                .exclude("transportTypeRef")
                .customize(new VehicleModelMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(CompositeFrame.class, org.rutebanken.sobek.model.vehicle.CompositeFrame.class)
                .byDefault()
                .register();

        mapperFactory.classMap(PassengerCapacityStructure.class, org.rutebanken.sobek.model.vehicle.PassengerCapacity.class)
                .customize(new PassengerCapacityStructureMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(DeckPlan.class, org.rutebanken.sobek.model.vehicle.DeckPlan.class)
                .customize(new DeckPlanMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(Deck.class, org.rutebanken.sobek.model.vehicle.Deck.class)
                .customize(new DeckMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(PassengerSpace.class, org.rutebanken.sobek.model.vehicle.PassengerSpace.class)
                .customize(new PassengerSpaceMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(PassengerEntrance.class, org.rutebanken.sobek.model.vehicle.PassengerEntrance.class)
                .customize(new PassengerEntranceMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(PassengerSpot.class, org.rutebanken.sobek.model.vehicle.PassengerSpot.class)
                .exclude("spotRowRef")
                .customize(new PassengerSpotMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(SpotRow.class, org.rutebanken.sobek.model.vehicle.SpotRow.class)
                .customize(new SpotRowMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(VehicleEquipmentProfile.class, org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfile.class)
                .byDefault()
                .register();

        mapperFactory.classMap(VehicleEquipmentProfileMember.class, org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfileMember.class)
                .exclude("equipmentRef")
                .customize(new VehicleEquipmentProfileMemberMapper())
                .byDefault()
                .register();

        mapperFactory.classMap(InstalledEquipment_VersionStructure.class, org.rutebanken.sobek.model.InstalledEquipment_VersionStructure.class)
                .fieldBToA("netexId", "id")
                .fieldAToB("id", "netexId")
                .byDefault()
                .register();

        mapperFactory.classMap(SanitaryEquipment.class, org.rutebanken.sobek.model.SanitaryEquipment.class)
                .fieldBToA("netexId", "id")
                .fieldAToB("id", "netexId")
                .byDefault()
                .register();

        mapperFactory.classMap(GeneralSign.class, org.rutebanken.sobek.model.GeneralSign.class)
                .byDefault()
                .register();

        mapperFactory.classMap(AccessibilityAssessment.class, org.rutebanken.sobek.model.AccessibilityAssessment.class)
                .customize(accessibilityAssessmentMapper)
                .exclude("id")
                .byDefault()
                .register();

        mapperFactory.classMap(DataManagedObjectStructure.class, org.rutebanken.sobek.model.DataManagedObjectStructure.class)
                .fieldBToA("keyValues", "keyList")
                .field("validBetween[0]", "validBetween")
                .customize(dataManagedObjectStructureMapper)
                .exclude("id")
                .exclude("keyList")
                .exclude("keyValues")
                .exclude("version")
                .byDefault()
                .register();

        facade = mapperFactory.getMapperFacade();
    }

    public MultilingualString mapToNetexModel(org.rutebanken.sobek.model.MultilingualString multilingualString) {
        return facade.map(multilingualString, MultilingualString.class);
    }

    public org.rutebanken.sobek.model.EmbeddableMultilingualString mapToSobekModel(MultilingualString multilingualString) {
        return facade.map(multilingualString, org.rutebanken.sobek.model.EmbeddableMultilingualString.class);
    }

    public ServiceFrame mapToNetexModel(org.rutebanken.sobek.model.ServiceFrame sobekServiceFrame) {
        ServiceFrame serviceFrame = facade.map(sobekServiceFrame, ServiceFrame.class);
        return serviceFrame;
    }

    public ResourceFrame mapToNetexModel(org.rutebanken.sobek.model.ResourceFrame sobekResourceFrame){
         ResourceFrame resourceFrame = facade.map(sobekResourceFrame, ResourceFrame.class);
         return resourceFrame;
    }

    public CompositeFrame mapToNetexModel(org.rutebanken.sobek.model.vehicle.CompositeFrame sobekCompositeFrame) {
        return facade.map(sobekCompositeFrame, CompositeFrame.class);
    }

    public Vehicle mapToNetexModel(org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle) {
        return facade.map(sobekVehicle, Vehicle.class);
    }

    public org.rutebanken.sobek.model.vehicle.Vehicle mapToSobekModel(Vehicle sobekVehicle) {
        return facade.map(sobekVehicle, org.rutebanken.sobek.model.vehicle.Vehicle.class);
    }

    public VehicleType mapToNetexModel(org.rutebanken.sobek.model.vehicle.VehicleType sobekVehicleType) {
        return facade.map(sobekVehicleType, VehicleType.class);
    }

    public org.rutebanken.sobek.model.vehicle.VehicleType mapToSobekModel(VehicleType netexVehicleType) {
        return facade.map(netexVehicleType, org.rutebanken.sobek.model.vehicle.VehicleType.class);
    }

    public DeckPlan mapToNetexModel(org.rutebanken.sobek.model.vehicle.DeckPlan deckPlan) {
        return facade.map(deckPlan, DeckPlan.class);
    }

    public org.rutebanken.sobek.model.vehicle.DeckPlan mapToSobekModel(DeckPlan deckPlan) {
        return facade.map(deckPlan, org.rutebanken.sobek.model.vehicle.DeckPlan.class);
    }

    public VehicleModel mapToNetexModel(org.rutebanken.sobek.model.vehicle.VehicleModel vehicleModel) {
        return facade.map(vehicleModel, VehicleModel.class);
    }

    public org.rutebanken.sobek.model.vehicle.VehicleModel mapToSobekModel(VehicleModel vehicleModel) {
        return facade.map(vehicleModel, org.rutebanken.sobek.model.vehicle.VehicleModel.class);
    }

    public MapperFacade getFacade() {
        return facade;
    }
}
