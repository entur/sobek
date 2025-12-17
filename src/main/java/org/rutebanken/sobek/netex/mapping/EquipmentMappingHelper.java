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
import org.rutebanken.netex.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.rutebanken.sobek.netex.mapping.mapper.NetexIdMapper.ORIGINAL_ID_KEY;

@Component
public class EquipmentMappingHelper {
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
}
