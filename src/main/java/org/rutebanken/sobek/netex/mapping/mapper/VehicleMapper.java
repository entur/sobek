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
import org.rutebanken.netex.model.*;

public class VehicleMapper extends CustomMapper<Vehicle, org.rutebanken.sobek.model.vehicle.Vehicle> {

    final ObjectFactory objectFactory = new ObjectFactory();

    @Override
    public void mapAtoB(Vehicle vehicle, org.rutebanken.sobek.model.vehicle.Vehicle vehicle2, MappingContext context) {
        super.mapAtoB(vehicle, vehicle2, context);

        if(vehicle.getVehicleModelRef() != null) {
            vehicle2.setVehicleModel(mapperFacade.map(vehicle.getVehicleModelRef(), org.rutebanken.sobek.model.vehicle.VehicleModel.class, context));
        }

        if(vehicle.getTransportTypeRef() != null) {
            TransportTypeRefStructure transportTypeRefStructure = vehicle.getTransportTypeRef().getValue();
            vehicle2.setTransportType(mapperFacade.map(transportTypeRefStructure, org.rutebanken.sobek.model.vehicle.VehicleType.class, context));
        }

    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle, Vehicle netexVehicle, MappingContext context) {
        super.mapBtoA(sobekVehicle, netexVehicle, context);
        if (sobekVehicle.getTransportType() != null) {
            JAXBElement<TransportTypeRefStructure> transportTypeRef = objectFactory.createTransportTypeRef(mapperFacade.map(sobekVehicle.getTransportType(), TransportTypeRefStructure.class, context));
            netexVehicle.withTransportTypeRef(transportTypeRef);
        }

        if (sobekVehicle.getVehicleModel() != null) {
            netexVehicle.withVehicleModelRef(mapperFacade.map(sobekVehicle.getVehicleModel(), VehicleModelRefStructure.class, context));
        }

    }
}
