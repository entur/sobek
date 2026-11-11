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

package org.rutebanken.sobek.netex.util;

import jakarta.xml.bind.JAXBElement;
import org.entur.netex2.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PublicationDeliveryHelper {

    public String getValueByKey(DataManagedObjectStructure dataManagedObject, String key) {

        return Stream.of(dataManagedObject)
                .filter(Objects::nonNull)
                .map(DataManagedObjectStructure::getKeyList)
                .filter(Objects::nonNull)
                .flatMap(keyList -> keyList.getKeyValue().stream())
                .filter(keyValueStructure -> keyValueStructure.getKey().equals(key))
                .map(KeyValueStructure::getValue)
                .findFirst().orElse(null);
    }

    public ResourceFrame findResourceFrame(PublicationDeliveryStructure incomingPublicationDelivery) {
        List<JAXBElement<? extends Common_VersionFrameStructure>> compositeFrameOrCommonFrame = incomingPublicationDelivery.getDataObjects().getCompositeFrameOrCommonFrame();

        Optional<ResourceFrame> optionalResourceFrame = compositeFrameOrCommonFrame
                .stream()
                .filter(element -> element.getValue() instanceof ResourceFrame)
                .map(element -> (ResourceFrame) element.getValue())
                .findFirst();

        if (optionalResourceFrame.isPresent()) {
            return optionalResourceFrame.get();
        }

        optionalResourceFrame = compositeFrameOrCommonFrame
                .stream()
                .filter(element -> element.getValue() instanceof CompositeFrame)
                .map(element -> (CompositeFrame) element.getValue())
                .map(Composite_VersionFrameStructure::getFrames)
                .flatMap(frames -> frames.getCommonFrame().stream())
                .filter(jaxbElement -> jaxbElement.getValue() instanceof ResourceFrame)
                .map(jaxbElement -> (ResourceFrame) jaxbElement.getValue())
                .findAny();

        return optionalResourceFrame.orElse(null);

    }

    /**
     * The frame with the fleets, which NeTEx puts in a MobilityServiceFrame. It is found in the delivery itself or
     * in a composite frame. Only one is used per delivery.
     */
    public MobilityServiceFrame findMobilityServiceFrame(PublicationDeliveryStructure incomingPublicationDelivery) {
        List<JAXBElement<? extends Common_VersionFrameStructure>> frames = incomingPublicationDelivery.getDataObjects().getCompositeFrameOrCommonFrame();

        Optional<MobilityServiceFrame> mobilityServiceFrame = frames.stream()
                .filter(element -> element.getValue() instanceof MobilityServiceFrame)
                .map(element -> (MobilityServiceFrame) element.getValue())
                .findFirst();

        if (mobilityServiceFrame.isPresent()) {
            return mobilityServiceFrame.get();
        }

        return frames.stream()
                .filter(element -> element.getValue() instanceof CompositeFrame)
                .map(element -> (CompositeFrame) element.getValue())
                .map(Composite_VersionFrameStructure::getFrames)
                .flatMap(compositeFrames -> compositeFrames.getCommonFrame().stream())
                .filter(jaxbElement -> jaxbElement.getValue() instanceof MobilityServiceFrame)
                .map(jaxbElement -> (MobilityServiceFrame) jaxbElement.getValue())
                .findAny()
                .orElse(null);
    }

    public boolean hasFleets(MobilityServiceFrame mobilityServiceFrame) {
        return mobilityServiceFrame != null
                && mobilityServiceFrame.getFleets() != null
                && mobilityServiceFrame.getFleets().getFleet() != null
                && !mobilityServiceFrame.getFleets().getFleet().isEmpty();
    }

    public boolean hasVehicles(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getVehicles() != null
                && netexResourceFrame.getVehicles().getVehicle() != null
                && !netexResourceFrame.getVehicles().getVehicle().isEmpty();
    }

    public int numberOfVehicles(ResourceFrame netexResourceFrame) {
        return hasVehicles(netexResourceFrame) ? netexResourceFrame.getVehicles().getVehicle().size() : 0;
    }

    public boolean hasVehicleTypes(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getVehicleTypes() != null
                && netexResourceFrame.getVehicleTypes().getTransportType() != null
                && !netexResourceFrame.getVehicleTypes().getTransportType().isEmpty();
    }

    public boolean hasVehicleModels(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getVehicleModels() != null
                && netexResourceFrame.getVehicleModels().getVehicleModel() != null
                && !netexResourceFrame.getVehicleModels().getVehicleModel().isEmpty();
    }

    public boolean hasDeckPlans(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getDeckPlans() != null
                && netexResourceFrame.getDeckPlans().getDeckPlan() != null
                && !netexResourceFrame.getDeckPlans().getDeckPlan().isEmpty();
    }

    public boolean hasEquipments(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getEquipments() != null
                && netexResourceFrame.getEquipments().getEquipment() != null
                && !netexResourceFrame.getEquipments().getEquipment().isEmpty();
    }

    public boolean hasSchematicMaps(ResourceFrame netexResourceFrame) {
        return netexResourceFrame.getSchematicMaps() != null
                && netexResourceFrame.getSchematicMaps().getSchematicMap() != null
                && !netexResourceFrame.getSchematicMaps().getSchematicMap().isEmpty();
    }
}
