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
import org.rutebanken.netex.model.VehicleType;

public class VehicleTypeMapper extends CustomMapper<VehicleType, org.rutebanken.sobek.model.vehicle.VehicleType> {

    @Override
    public void mapAtoB(VehicleType vehicleType, org.rutebanken.sobek.model.vehicle.VehicleType vehicleType2, MappingContext context) {
        super.mapAtoB(vehicleType, vehicleType2, context);

        if(vehicleType.getDeckPlanRef() != null) {
            vehicleType2.setDeckPlan(mapperFacade.map(vehicleType.getDeckPlanRef(), org.rutebanken.sobek.model.vehicle.DeckPlan.class, context));
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.VehicleType sobekVehicleType, VehicleType netexVehicleType, MappingContext context) {
        super.mapBtoA(sobekVehicleType, netexVehicleType, context);

        if(sobekVehicleType.getPassengerCapacity() != null) {
            netexVehicleType.withPassengerCapacity(mapperFacade.map(sobekVehicleType.getPassengerCapacity(), org.rutebanken.netex.model.PassengerCapacityStructure.class, context));
        }

        if(sobekVehicleType.getDeckPlan() != null) {
            netexVehicleType.withDeckPlanRef(mapperFacade.map(sobekVehicleType.getDeckPlan(), org.rutebanken.netex.model.DeckPlanRefStructure.class, context));
        }
    }
}
