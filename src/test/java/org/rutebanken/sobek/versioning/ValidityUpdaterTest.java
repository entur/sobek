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

package org.rutebanken.sobek.versioning;

import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.ValidBetween;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidityUpdaterTest extends SobekIntegrationTest {

    @Autowired
    private ValidityUpdater validityUpdater;

    @Test
    public void terminateVersionsWithoutValidBetween() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);

        Instant now = Instant.now();
        validityUpdater.terminateVersion(Vehicle, now);

        assertThat(Vehicle.getValidBetween()).isNotNull();
        assertThat(Vehicle.getValidBetween().getToDate()).isEqualTo(now);
    }

    @Test
    public void updateValidBetweenWhenNotPreviouslySet() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);

        Instant now = Instant.now();
        validityUpdater.updateValidBetween(Vehicle, now);

        assertThat(Vehicle.getValidBetween()).isNotNull();
        assertThat(Vehicle.getValidBetween().getFromDate()).as("from date").isEqualTo(now);
        assertThat(Vehicle.getValidBetween().getToDate()).as("to date").isNull();
    }

    @Test
    public void updateValidBetweenWhenFromDateNotPreviouslySet() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Vehicle.setValidBetween(new ValidBetween(null));

        Instant now = Instant.now();
        validityUpdater.updateValidBetween(Vehicle, now);

        assertThat(Vehicle.getValidBetween()).isNotNull();
        assertThat(Vehicle.getValidBetween().getFromDate()).as("from date").isEqualTo(now);
        assertThat(Vehicle.getValidBetween().getToDate()).as("to date").isNull();
    }

    @Test
    public void useValidBetweenFromDateIfAlreadySet() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Instant now = Instant.now();
        Vehicle.setValidBetween(new ValidBetween(now));

        Instant actual = validityUpdater.updateValidBetween(Vehicle, now);

        assertThat(Vehicle.getValidBetween()).isNotNull();
        assertThat(Vehicle.getValidBetween().getFromDate()).as("from date").isEqualTo(now);
        assertThat(Vehicle.getValidBetween().getToDate()).as("to date").isNull();
        assertThat(actual).as("new version valid from").isEqualTo(now);
    }


    @Test(expected = IllegalArgumentException.class)
    public void doNotAcceptFromDateAfterToDate() {
        Vehicle Vehicle = new Vehicle();
        Vehicle.setVersion(1L);
        Instant now = Instant.now();
        Vehicle.setValidBetween(new ValidBetween(now, now.minusSeconds(10)));

        validityUpdater.updateValidBetween(Vehicle, now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAcceptFromDateBeforePreviousVersionEndDate() {
        Vehicle previousVersion = new Vehicle();
        previousVersion.setVersion(1L);
        Instant now = Instant.now();
        previousVersion.setValidBetween(new ValidBetween(now.minusSeconds(1000), now));

        Vehicle newVersion = new Vehicle();
        newVersion.setVersion(2L);
        newVersion.setValidBetween(new ValidBetween(previousVersion.getValidBetween().getToDate().minusSeconds(10)));

        validityUpdater.updateValidBetween(previousVersion, newVersion, now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAcceptFromDateBeforePreviousVersionFromDate() {
        Vehicle previousVersion = new Vehicle();
        previousVersion.setVersion(1L);
        Instant now = Instant.now();

        // No to date
        previousVersion.setValidBetween(new ValidBetween(now.minusSeconds(1000), null));

        Vehicle newVersion = new Vehicle();
        newVersion.setVersion(2L);
        newVersion.setValidBetween(new ValidBetween(previousVersion.getValidBetween().getFromDate().minusSeconds(10)));

        validityUpdater.updateValidBetween(previousVersion, newVersion, now);
    }

    @Test
    public void doNotSetEndDateOnPreviousVersionIfAlreadySet() {
        Vehicle oldVersion = new Vehicle();
        oldVersion.setVersion(1L);
        oldVersion.setValidBetween(new ValidBetween(Instant.EPOCH, Instant.EPOCH));

        validityUpdater.terminateVersion(oldVersion, Instant.now());

        assertThat(oldVersion.getValidBetween().getToDate()).isEqualTo(Instant.EPOCH);
    }

}