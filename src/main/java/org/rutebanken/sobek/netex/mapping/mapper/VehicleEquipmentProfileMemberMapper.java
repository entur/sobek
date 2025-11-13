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

package org.rutebanken.sobek.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.EquipmentRefStructure;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.TransportTypeRefStructure;
import org.rutebanken.netex.model.VehicleEquipmentProfileMember;

public class VehicleEquipmentProfileMemberMapper extends CustomMapper<VehicleEquipmentProfileMember, org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfileMember> {

    final ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public void mapAtoB(VehicleEquipmentProfileMember VehicleEquipmentProfileMember, org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfileMember VehicleEquipmentProfileMember2, MappingContext context) {
        super.mapAtoB(VehicleEquipmentProfileMember, VehicleEquipmentProfileMember2, context);

    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.VehicleEquipmentProfileMember sobekVehicleEquipmentProfileMember, VehicleEquipmentProfileMember netexVehicleEquipmentProfileMember, MappingContext context) {
        super.mapBtoA(sobekVehicleEquipmentProfileMember, netexVehicleEquipmentProfileMember, context);

        if (sobekVehicleEquipmentProfileMember.getEquipmentRef() != null) {
            JAXBElement<EquipmentRefStructure> equipmentRef = objectFactory.createEquipmentRef(new EquipmentRefStructure().withRef(sobekVehicleEquipmentProfileMember.getEquipmentRef()));
            netexVehicleEquipmentProfileMember.withEquipmentRef(equipmentRef);
        }
    }
}
