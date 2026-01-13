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

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.AccessVehicleEquipment;

public class AccessVehicleEquipmentMapper extends CustomMapper<AccessVehicleEquipment, org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment> {

    @Override
    public void mapAtoB(AccessVehicleEquipment neTExEquipment, org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment sobekEquipment, MappingContext context) {
        super.mapAtoB(neTExEquipment, sobekEquipment, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment sobekEquipment, AccessVehicleEquipment netexEquipment, MappingContext context) {
        super.mapBtoA(sobekEquipment, netexEquipment, context);
    }
}
