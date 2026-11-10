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
    default Page<VehicleType> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, VehicleTypeKindEnumeration vehicleTypeKind, String name, Pageable pageable) {
        return findCurrentFiltered(dataOwnerRef, netexIds, transportModes, vehicleTypeKind, null, name, pageable);
    }

    /**
     * @param vehicleTypeKind only return vehicle types of this kind; null returns all kinds
     * @param subtype only return vehicle types of exactly this class: VehicleType for the plain ones, Train or
     *                CompoundTrain; null returns all subtypes
     */
    Page<VehicleType> findCurrentFiltered(String dataOwnerRef, List<String> netexIds, List<AllPublicTransportModesEnumeration> transportModes, VehicleTypeKindEnumeration vehicleTypeKind, Class<? extends VehicleType> subtype, String name, Pageable pageable);

    /**
     * True if a compound train that is valid now holds this version of a train. A train in use cannot be deactivated.
     */
    boolean existsValidCompoundTrainWithTrain(String trainNetexId, Long trainVersion);

    /**
     * True if a train that is valid now has a component with this version of a train element. A train element in use
     * cannot be deactivated.
     */
    boolean existsValidTrainWithTrainElement(String trainElementNetexId, Long trainElementVersion);

    /**
     * Moves the references from the components of trains and compound trains that are valid now from one version of
     * a train or train element to another. Components of historic versions keep their references.
     */
    void moveComponentReferences(Long fromVehicleTypeId, Long toVehicleTypeId);

    void moveToDeckPlan(Long fromDeckPlanId, Long toDeckPlanId);

    boolean existsValidWithDeckPlan(String deckPlanNetexId, Long deckPlanVersion);

    List<String> findCurrentNeTExIds();
}

