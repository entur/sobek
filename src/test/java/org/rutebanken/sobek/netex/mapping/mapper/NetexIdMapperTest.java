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

import org.junit.Test;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.sobek.model.AccessibilityAssessment;
import org.rutebanken.sobek.model.ResourceFrame;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.netex.mapping.mapper.NetexIdMapper.ORIGINAL_ID_KEY;

public class NetexIdMapperTest {

    private ValidPrefixList validPrefixList = new ValidPrefixList("NSR", new HashMap<>());
    private NetexIdHelper netexIdHelper = new NetexIdHelper(validPrefixList);
    private NetexIdMapper netexIdMapper = new NetexIdMapper(validPrefixList, netexIdHelper);

    @Test
    public void mapResourceFrameIdToNetex() throws Exception {
        ResourceFrame resourceFrame = new ResourceFrame();
        resourceFrame.setNetexId("NSR:ResourceFrame:123123");

        org.rutebanken.netex.model.ResourceFrame netexResourceFrame = new org.rutebanken.netex.model.ResourceFrame();
        netexIdMapper.toNetexModel(resourceFrame, netexResourceFrame);

        assertThat(netexResourceFrame.getId()).isNotEmpty();
        assertThat(netexResourceFrame.getId()).isEqualToIgnoringCase("NSR:ResourceFrame:123123");
    }

    @Test
    public void accessibilityAssesmentIdToNetex() throws Exception {
        AccessibilityAssessment accessibilityAssessment = new AccessibilityAssessment();
        accessibilityAssessment.setNetexId("NSR:AccessibilityAssesment:123124");

        org.rutebanken.netex.model.AccessibilityAssessment netexAccessibilityAssesment = new org.rutebanken.netex.model.AccessibilityAssessment();
        netexIdMapper.toNetexModel(accessibilityAssessment, netexAccessibilityAssesment);

        assertThat(netexAccessibilityAssesment.getId()).isNotEmpty();
        assertThat(netexAccessibilityAssesment.getId()).isEqualToIgnoringCase("NSR:AccessibilityAssesment:123124");
    }

    @Test
    public void copyKeyValuesAvoidEmptyOriginalId() throws Exception {

        String originalId = "RUT:Vehicle:1,,RUT:Vehicle:2";

        org.rutebanken.netex.model.DataManagedObjectStructure netexEntity = new org.rutebanken.netex.model.Vehicle()
                .withKeyList(new KeyListStructure()
                        .withKeyValue(new KeyValueStructure()
                                .withKey(ORIGINAL_ID_KEY)
                                .withValue(originalId)));

        Vehicle vehicle = new Vehicle();

        netexIdMapper.copyKeyValuesToSobekModel(netexEntity, vehicle);

        assertThat(vehicle.getOriginalIds()).hasSize(2);
    }

    @Test
    public void copyKeyValuesForVehicleTypeEmptyPostfixRemove() throws Exception {

        String originalId = "RUT:VehicleType:";

        org.rutebanken.netex.model.DataManagedObjectStructure netexEntity = new org.rutebanken.netex.model.VehicleType()
                .withKeyList(new KeyListStructure()
                        .withKeyValue(new KeyValueStructure()
                                .withKey(ORIGINAL_ID_KEY)
                                .withValue(originalId)));

        VehicleType vehicleType = new VehicleType();

        netexIdMapper.copyKeyValuesToSobekModel(netexEntity, vehicleType);

        assertThat(vehicleType.getOriginalIds()).hasSize(0);
    }

    @Test
    public void moveKeyValuesForVehicleTypeEmptyPostfixRemove() throws Exception {

        String originalId = "RUT:VehicleType:";

        VehicleType VehicleType = new VehicleType();
        netexIdMapper.moveOriginalIdToKeyValueList(VehicleType, originalId);

        assertThat(VehicleType.getOriginalIds()).hasSize(0);
    }

    @Test
    public void copyKeyValuesForStopEmptyPostfixRemove() throws Exception {

        String originalId = "RUT:Vehicle:";

        org.rutebanken.netex.model.DataManagedObjectStructure netexEntity = new org.rutebanken.netex.model.Vehicle()
                .withKeyList(new KeyListStructure()
                        .withKeyValue(new KeyValueStructure()
                                .withKey(ORIGINAL_ID_KEY)
                                .withValue(originalId)));

        Vehicle Vehicle = new Vehicle();

        netexIdMapper.copyKeyValuesToSobekModel(netexEntity, Vehicle);

        assertThat(Vehicle.getOriginalIds()).hasSize(0);
    }

    @Test
    public void moveKeyValuesForStopEmptyPostfixRemove() throws Exception {

        String originalId = "RUT:Vehicle:";

        Vehicle Vehicle = new Vehicle();
        netexIdMapper.moveOriginalIdToKeyValueList(Vehicle, originalId);

        assertThat(Vehicle.getOriginalIds()).hasSize(0);
    }
}