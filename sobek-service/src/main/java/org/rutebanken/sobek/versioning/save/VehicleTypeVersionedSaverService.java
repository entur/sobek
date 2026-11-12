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

package org.rutebanken.sobek.versioning.save;


import lombok.extern.java.Log;
import org.rutebanken.sobek.error.VehicleTypeKindViolationException;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.PassengerCapacity;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.netex.id.CodespaceType;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;
import org.springframework.stereotype.Component;

import jakarta.validation.ValidationException;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;

@Component
@Log
public class VehicleTypeVersionedSaverService {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleRepository vehicleRepository;
    private final FleetRepository fleetRepository;
    private final DefaultMergingVersionedSaverService defaultVersionedSaverService;
    private final NetexIdAssigner netexIdAssigner;
    private final ValidPrefixList validPrefixList;


    public VehicleTypeVersionedSaverService(VehicleTypeRepository vehicleTypeRepository, VehicleRepository vehicleRepository, FleetRepository fleetRepository, DefaultMergingVersionedSaverService defaultVersionedSaverService, NetexIdAssigner netexIdAssigner, ValidPrefixList validPrefixList) {
        this.validPrefixList = validPrefixList;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleRepository = vehicleRepository;
        this.fleetRepository = fleetRepository;
        this.defaultVersionedSaverService = defaultVersionedSaverService;
        this.netexIdAssigner = netexIdAssigner;
    }

    public VehicleType saveNewVersion(VehicleType newVersion) {
        var existingVersion = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(newVersion.getNetexId());
        if (existingVersion != null) {
            log.log(Level.FINE, "Found existing entity from netexId {}", existingVersion.getNetexId());
        }
        return saveNewVersion(existingVersion, newVersion, Instant.now());
    }

    public VehicleType saveNewVersion(VehicleType existingVersion, VehicleType newVersion, Instant defaultValidFrom) {
        verifySameSubtype(existingVersion, newVersion);
        resolveVehicleTypeKind(existingVersion, newVersion);
        verifyNetexIdMatchesKind(newVersion);
        verifyComponents(newVersion);

        // Assign netexId if not already assigned
        // PassengerCapacity doesn't have a independent lifecycle, meaning it's linked to the VehicleType at all times
        // Therefore, if the client doesn't control the netexId, we assign it here.
        PassengerCapacity passengerCapacity = newVersion.getPassengerCapacity();
        if(passengerCapacity != null && passengerCapacity.getId() == null) {
            netexIdAssigner.assignNetexId(passengerCapacity);
        }

        // Components have no independent lifecycle either: they are children of the train, and Sobek assigns their ids
        if (newVersion instanceof Train train && train.getComponents() != null) {
            train.getComponents().stream().filter(c -> c.getNetexId() == null).forEach(netexIdAssigner::assignNetexId);
        } else if (newVersion instanceof CompoundTrain compoundTrain && compoundTrain.getComponents() != null) {
            compoundTrain.getComponents().stream().filter(c -> c.getNetexId() == null).forEach(netexIdAssigner::assignNetexId);
        }

        var saved = defaultVersionedSaverService.saveNewVersion(existingVersion, newVersion, defaultValidFrom, vehicleTypeRepository);
        if(existingVersion != null && !saved.getId().equals(existingVersion.getId())) {
            vehicleRepository.moveToTransportType(existingVersion.getId(), saved.getId());
            // Trains and compound trains that include this vehicle type follow it to the new version
            vehicleTypeRepository.moveComponentReferences(existingVersion.getId(), saved.getId());
            // Fleets that have this vehicle type as a transport type follow it to the new version
            fleetRepository.moveTransportType(existingVersion.getId(), saved.getId());
        }
        return saved;
    }

    /**
     * A vehicle type is a plain VehicleType, a Train or a CompoundTrain from the moment it is created. A new version
     * of another subtype would be stored as a different kind of row and lose the data of the previous version.
     */
    private void verifySameSubtype(VehicleType existingVersion, VehicleType newVersion) {
        if (existingVersion == null) {
            return;
        }
        Class<?> existingType = Hibernate.getClass(existingVersion);
        Class<?> newType = Hibernate.getClass(newVersion);
        if (existingType != newType) {
            throw new ValidationException("Vehicle type " + existingVersion.getNetexId() + " is a " + existingType.getSimpleName()
                    + " and cannot be changed to a " + newType.getSimpleName());
        }
    }

    /**
     * The components of a train are positions for train elements, and the components of a compound train are trains:
     * the types of the fields say so. What is left to check is that each component has its target, that the target
     * exists, and that it belongs to the same data owner (a train is made of its owner's elements, and a compound train
     * of its owner's trains). They must also be of the same kind, physical or contractual, as the train or compound train
     * that holds them: the export of a kind has the elements and trains of that kind only, and a reference to one that is
     * not in the export would make the export invalid. A compound train cannot hold a compound train, so there are no
     * cycles to look for.
     */
    private void verifyComponents(VehicleType vehicleType) {
        if (vehicleType instanceof Train train && train.getComponents() != null) {
            for (int i = 0; i < train.getComponents().size(); i++) {
                TrainElement element = train.getComponents().get(i).getTrainElement();
                String position = "Component " + (i + 1) + " of train " + train.getNetexId();
                if (element == null || element.getId() == null) {
                    throw new ValidationException(position + " does not refer to an existing train element.");
                }
                if (!Objects.equals(element.getDataOwnerRef(), train.getDataOwnerRef())) {
                    throw new ValidationException(position + " refers to train element " + element.getNetexId() + ", which belongs to data owner "
                            + element.getDataOwnerRef() + ". A train can only be made of train elements of its own data owner, "
                            + train.getDataOwnerRef() + ".");
                }
                if (element.getVehicleTypeKind() != train.getVehicleTypeKind()) {
                    throw new VehicleTypeKindViolationException(position + " is of the train element " + element.getNetexId() + ", which is "
                            + element.getVehicleTypeKind() + ". A " + train.getVehicleTypeKind() + " train can only be made of "
                            + train.getVehicleTypeKind() + " train elements.");
                }
            }
        } else if (vehicleType instanceof CompoundTrain compoundTrain && compoundTrain.getComponents() != null) {
            for (int i = 0; i < compoundTrain.getComponents().size(); i++) {
                Train part = compoundTrain.getComponents().get(i).getTrain();
                String position = "Component " + (i + 1) + " of compound train " + compoundTrain.getNetexId();
                if (part == null || part.getId() == null) {
                    throw new ValidationException(position + " does not refer to an existing train.");
                }
                if (!Objects.equals(part.getDataOwnerRef(), compoundTrain.getDataOwnerRef())) {
                    throw new ValidationException(position + " refers to train " + part.getNetexId() + ", which belongs to data owner "
                            + part.getDataOwnerRef() + ". A compound train can only hold trains of its own data owner, "
                            + compoundTrain.getDataOwnerRef() + ".");
                }
                if (part.getVehicleTypeKind() != compoundTrain.getVehicleTypeKind()) {
                    throw new VehicleTypeKindViolationException(position + " is of the train " + part.getNetexId() + ", which is "
                            + part.getVehicleTypeKind() + ". A " + compoundTrain.getVehicleTypeKind() + " compound train can only hold "
                            + compoundTrain.getVehicleTypeKind() + " trains.");
                }
            }
        }
    }

    /**
     * In Sobek's own codespaces, contractual vehicle types are in CVT and physical ones are not. Generated ids always
     * get this right (see NetexIdHelper); this guards the ids of existing vehicle types. An id in any other codespace
     * says nothing about the kind.
     */
    private void verifyNetexIdMatchesKind(VehicleType vehicleType) {
        String netexId = vehicleType.getNetexId();
        if (netexId == null || validPrefixList.getCodespaceTypeOf(netexId, Hibernate.getClass(vehicleType)) !=CodespaceType.SOBEK) {
            return;
        }
        boolean inContractualCodespace = ValidPrefixList.isContractualVehicleTypeId(netexId);
        boolean contractual = vehicleType.getVehicleTypeKind() == VehicleTypeKindEnumeration.CONTRACTUAL;
        if (inContractualCodespace != contractual) {
            throw new VehicleTypeKindViolationException("Vehicle type id " + netexId + " does not match its kind " + vehicleType.getVehicleTypeKind()
                    + ". Only contractual vehicle types use the " + ValidPrefixList.CONTRACTUAL_VEHICLE_TYPE_PREFIX + " codespace.");
        }
    }

    /**
     * The kind (physical/contractual) is fixed when the vehicle type is created.
     * Creating a new vehicle type: the kind is required.
     * Updating: not specified inherits from the existing version; specified and different is rejected.
     */
    private void resolveVehicleTypeKind(VehicleType existingVersion, VehicleType newVersion) {
        VehicleTypeKindEnumeration requested = newVersion.getVehicleTypeKind();
        if (existingVersion == null) {
            if (requested == null) {
                throw new VehicleTypeKindViolationException("vehicleTypeKind is required when creating a vehicle type: PHYSICAL or CONTRACTUAL");
            }
            return;
        }
        VehicleTypeKindEnumeration existing = existingVersion.getVehicleTypeKind();
        if (requested == null) {
            newVersion.setVehicleTypeKind(existing);
        } else if (existing != null && requested != existing) {
            throw new VehicleTypeKindViolationException("Vehicle type " + existingVersion.getNetexId()
                    + " is " + existing + " and cannot be changed to " + requested);
        }
    }
}
