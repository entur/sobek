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

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.MapperFacade;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.rutebanken.sobek.netex.mapping.mapper.NetexIdMapper.ORIGINAL_ID_KEY;

@Component
public class EquipmentMappingHelper {
    private MapperFacade facade;

    public void setFacade(MapperFacade facade) {
        this.facade = facade;
    }

    public JAXBElement<? extends org.rutebanken.netex.model.Equipment_VersionStructure> mapToJaxbEquipment(org.rutebanken.netex.model.Equipment_VersionStructure netexEquipment) {
        ObjectFactory objectFactory = new ObjectFactory();

        return switch (netexEquipment) {
            case org.rutebanken.netex.model.SeatEquipment seatEquipment -> objectFactory.createSeatEquipment(seatEquipment);
            case org.rutebanken.netex.model.BedEquipment bedEquipment -> objectFactory.createBedEquipment(bedEquipment);
            case org.rutebanken.netex.model.AccessVehicleEquipment accessVehicleEquipment -> objectFactory.createAccessVehicleEquipment(accessVehicleEquipment);
            case org.rutebanken.netex.model.EntranceEquipment entranceEquipment -> objectFactory.createEntranceEquipment(entranceEquipment);
            case org.rutebanken.netex.model.SpotEquipment spotEquipment -> objectFactory.createSpotEquipment(spotEquipment);
            case org.rutebanken.netex.model.LuggageSpotEquipment luggageSpotEquipment -> objectFactory.createLuggageSpotEquipment(luggageSpotEquipment);
            case org.rutebanken.netex.model.StaircaseEquipment staircaseEquipment -> objectFactory.createStaircaseEquipment(staircaseEquipment);
            default -> null;
        };
    }

    public void mapActualEquipmentsNeTEx2Sobek(org.rutebanken.sobek.model.vehicle.OnboardSpace_VersionStructure sobekOnboardSpace, ActualVehicleEquipments_RelStructure actualVehicleEquipments) {
        if(actualVehicleEquipments != null &&
            actualVehicleEquipments.getActualVehicleEquipment() != null &&
            !actualVehicleEquipments.getActualVehicleEquipment().isEmpty()) {

            sobekOnboardSpace.setActualVehicleEquipments(
                    actualVehicleEquipments.getActualVehicleEquipment().stream().map(this::mapActualEquipmentNeTEx2Sobek)
                            .toList());
        }
    }

    private Equipment mapActualEquipmentNeTEx2Sobek(ActualVehicleEquipment_VersionStructure actualVehicleEquipmentVersionStructure) {
        if (actualVehicleEquipmentVersionStructure.getEquipmentRef() == null || actualVehicleEquipmentVersionStructure.getEquipmentRef().getValue() == null) {
            return null;
        }

        Object refValue = actualVehicleEquipmentVersionStructure.getEquipmentRef().getValue();

        return switch (refValue) {
            case org.rutebanken.netex.model.SeatEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.SeatEquipment.class);
            case org.rutebanken.netex.model.BedEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.BedEquipment.class);
            case org.rutebanken.netex.model.AccessVehicleEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment.class);
            case org.rutebanken.netex.model.EntranceEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.EntranceEquipment.class);
            case org.rutebanken.netex.model.LuggageSpotEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment.class);
            case org.rutebanken.netex.model.SpotEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.SpotEquipment.class);
            case org.rutebanken.netex.model.StaircaseEquipmentRefStructure ref ->
                    facade.map(ref, org.rutebanken.sobek.model.vehicle.StaircaseEquipment.class);
            default -> null;
        };
    }

    public void mapActualEquipmentsSobek2NeTEx(OnboardSpace_VersionStructure netexOnboardSpace, List<Equipment> sobekEquipments) {
        if(sobekEquipments != null && !sobekEquipments.isEmpty()) {
            ActualVehicleEquipments_RelStructure actualVehicleEquipments = new ActualVehicleEquipments_RelStructure();
            actualVehicleEquipments.withActualVehicleEquipment(
                    sobekEquipments.stream().map(e -> new ActualVehicleEquipment_VersionStructure().withEquipmentRef(createNeTExEquipmentRef(e)))
                            .filter(Objects::nonNull)
                            .toList());
            netexOnboardSpace.setActualVehicleEquipments(actualVehicleEquipments);
        }
    }

    private JAXBElement<? extends EquipmentRefStructure> createNeTExEquipmentRef(org.rutebanken.sobek.model.vehicle.Equipment equipment) {
        ObjectFactory objectFactory = new ObjectFactory();
        return switch (equipment) {
            case org.rutebanken.sobek.model.vehicle.SeatEquipment seatEquipment -> objectFactory.createSeatEquipmentRef(new SeatEquipmentRefStructure().withRef(seatEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.BedEquipment bedEquipment -> objectFactory.createBedEquipmentRef(new BedEquipmentRefStructure().withRef(bedEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment accessVehicleEquipment -> objectFactory.createAccessVehicleEquipmentRef(new AccessVehicleEquipmentRefStructure().withRef(accessVehicleEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.EntranceEquipment entranceEquipment -> objectFactory.createEntranceEquipmentRef(new EntranceEquipmentRefStructure().withRef(entranceEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.SpotEquipment spotEquipment -> objectFactory.createSpotEquipmentRef(new SpotEquipmentRefStructure().withRef(spotEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment luggageSpotEquipment -> objectFactory.createLuggageSpotEquipmentRef(new LuggageSpotEquipmentRefStructure().withRef(luggageSpotEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.StaircaseEquipment staircaseEquipment -> objectFactory.createStaircaseEquipmentRef(new StaircaseEquipmentRefStructure().withRef(staircaseEquipment.getNetexId()));
            default -> null;
        };

    }

}
