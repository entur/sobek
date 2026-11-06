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

package org.rutebanken.sobek.repository;

import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehicleTypeRepositoryCustom extends DataManagedObjectStructureRepository<VehicleType> {

    /** @param vehicleTypeKind only return vehicle types of this kind; null returns all kinds */
    Page<VehicleType> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, VehicleTypeKindEnumeration vehicleTypeKind, String name, Pageable pageable);

    /**
     * Moves the references from train components (of trains and compound trains) that are valid now from one
     * version of a vehicle type to another. Components of historic versions keep their references.
     */
    void moveComponentReferences(Long fromVehicleTypeId, Long toVehicleTypeId);

    void moveToDeckPlan(Long fromDeckPlanId, Long toDeckPlanId);

    boolean existsValidWithDeckPlan(String deckPlanNetexId, Long deckPlanVersion);

    List<String> findCurrentNeTExIds();
}

