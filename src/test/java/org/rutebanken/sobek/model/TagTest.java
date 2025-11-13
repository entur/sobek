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

package org.rutebanken.sobek.model;

import org.junit.Test;
import org.rutebanken.sobek.SobekIntegrationTest;
import org.rutebanken.sobek.model.tag.Tag;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class TagTest extends SobekIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private org.rutebanken.sobek.repository.VehicleRepository vehicleRepository;

    @Test
    public void testSave() {

        Vehicle vehicle = new Vehicle();
        vehicle.setVersion(1L);
        vehicleRepository.save(vehicle);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVersion(2L);
        vehicleRepository.save(vehicle2);

        Tag tag = new Tag();
        tag.setName("fix-coordinates");
        tag.setIdreference(vehicle.getNetexId());
        tagRepository.save(tag);

        Tag tag2 = new Tag();
        tag2.setName("something-else");
        tag2.setIdreference(vehicle.getNetexId());
        tagRepository.save(tag2);

        Set<Tag> actual = tagRepository.findByIdReference(vehicle.getNetexId());

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(2);
    }
}