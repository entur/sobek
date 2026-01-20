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

package org.rutebanken.sobek.netex.mapping.converter;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.*;
import org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment;
import org.rutebanken.sobek.model.vehicle.ActualVehicleEquipments_RelStructure;
import org.rutebanken.sobek.model.vehicle.BedEquipment;
import org.rutebanken.sobek.model.vehicle.EntranceEquipment;
import org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment;
import org.rutebanken.sobek.model.vehicle.SeatEquipment;
import org.rutebanken.sobek.model.vehicle.SpotEquipment;
import org.rutebanken.sobek.model.vehicle.StaircaseEquipment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EquipmentListConverter extends BidirectionalConverter<List<Equipment>, ActualVehicleEquipments_RelStructure> {
    @Override
    public ActualVehicleEquipments_RelStructure convertTo(List<Equipment> equipments, Type<ActualVehicleEquipments_RelStructure> type, MappingContext mappingContext) {

        if(equipments == null || equipments.isEmpty()) {
            return null;
        }

        var ret = new  ActualVehicleEquipments_RelStructure();
        ret.getActualVehicleEquipment().addAll(equipments.stream()
                        .map(this::mapToNeTEx)
                        .toList());
        return ret;
    }

    @Override
    public List<Equipment> convertFrom(ActualVehicleEquipments_RelStructure equipmentsRelStructure, Type<List<Equipment>> type, MappingContext mappingContext) {
        if(equipmentsRelStructure == null || equipmentsRelStructure.getActualVehicleEquipment() == null || equipmentsRelStructure.getActualVehicleEquipment().isEmpty()) {
            return null;
        }

        return equipmentsRelStructure.getActualVehicleEquipment().stream()
                .map(this::mapToSobek)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private JAXBElement<? extends EquipmentRefStructure> mapToNeTEx(Equipment equipment) {
        ObjectFactory objectFactory = new ObjectFactory();

        return switch (equipment) {
            case SeatEquipment seatEquipment -> objectFactory.createSeatEquipmentRef(new SeatEquipmentRefStructure().withRef(seatEquipment.getNetexId()));
            case BedEquipment bedEquipment -> objectFactory.createBedEquipmentRef(new BedEquipmentRefStructure().withRef(bedEquipment.getNetexId()));
            case AccessVehicleEquipment accessVehicleEquipment -> objectFactory.createAccessVehicleEquipmentRef(new AccessVehicleEquipmentRefStructure().withRef(accessVehicleEquipment.getNetexId()));
            case EntranceEquipment entranceEquipment -> objectFactory.createEntranceEquipmentRef(new EntranceEquipmentRefStructure().withRef(entranceEquipment.getNetexId()));
            case SpotEquipment spotEquipment -> objectFactory.createSpotEquipmentRef(new SpotEquipmentRefStructure().withRef(spotEquipment.getNetexId()));
            case LuggageSpotEquipment luggageSpotEquipment -> objectFactory.createLuggageSpotEquipmentRef(new LuggageSpotEquipmentRefStructure().withRef(luggageSpotEquipment.getNetexId()));
            case StaircaseEquipment staircaseEquipment -> objectFactory.createStaircaseEquipmentRef(new StaircaseEquipmentRefStructure().withRef(staircaseEquipment.getNetexId()));
            default -> null;
        };
    }

    private Equipment mapToSobek(JAXBElement<?> element) {
        if (element == null || element.getValue() == null) {
            return null;
        }

        Object value = element.getValue();

        // Handle Reference structures as seen in your XML requirement
        if (value instanceof EquipmentRefStructure ref) {
            return switch (ref) {
                case SeatEquipmentRefStructure s -> mapperFacade.map(s, SeatEquipment.class);
                case BedEquipmentRefStructure b -> mapperFacade.map(b, BedEquipment.class);
                case AccessVehicleEquipmentRefStructure a -> mapperFacade.map(a, AccessVehicleEquipment.class);
                case EntranceEquipmentRefStructure e -> mapperFacade.map(e, EntranceEquipment.class);
                case LuggageSpotEquipmentRefStructure l -> mapperFacade.map(l, LuggageSpotEquipment.class);
                case SpotEquipmentRefStructure sp -> mapperFacade.map(sp, SpotEquipment.class);
                case StaircaseEquipmentRefStructure st -> mapperFacade.map(st, StaircaseEquipment.class);
                default -> null;
            };
        }
        return null;
    }
}

