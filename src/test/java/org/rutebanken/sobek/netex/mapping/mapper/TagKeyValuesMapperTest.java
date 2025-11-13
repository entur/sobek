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

import com.google.common.collect.Sets;
import org.junit.Test;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.Vehicle;
import org.rutebanken.sobek.model.tag.Tag;
import org.rutebanken.sobek.repository.TagRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TagKeyValuesMapperTest {

    private TagRepository tagRepository = mock(TagRepository.class);
    private TagKeyValuesMapper tagKeyValuesMapper = new TagKeyValuesMapper(tagRepository);
    private Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    @Test
    public void mapTagsToProperties() throws Exception {

        Tag tag = new Tag();
        tag.setCreated(now);
        tag.setName("name");
        tag.setCreatedBy("also me");
        tag.setIdreference("NMR:Vehicle:1");
        tag.setRemovedBy("me");
        tag.setRemoved(now);
        tag.setComment("comment");

        Set<Tag> tags = Sets.newHashSet(tag);
        when(tagRepository.findByIdReference("NMR:Vehicle:1")).thenReturn(tags);

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setNetexId("NMR:Vehicle:1");

        Vehicle Vehicle = new Vehicle();
        Vehicle.withKeyList(new KeyListStructure());
        tagKeyValuesMapper.mapTagsToProperties(sobekVehicle, Vehicle);



        Map<String, String> flattened = Vehicle.getKeyList().getKeyValue().stream().collect(Collectors.toMap(KeyValueStructure::getKey, KeyValueStructure::getValue));

        assertThat(flattened).containsKeys(
                "TAG-0-name",
                "TAG-0-createdBy",
                "TAG-0-created",
                "TAG-0-removed",
                "TAG-0-removedBy",
                "TAG-0-comment",
                "TAG-0-idReference");

        assertThat(flattened.get("TAG-0-idReference")).isEqualTo(tag.getIdReference());
        assertThat(flattened.get("TAG-0-name")).isEqualTo(tag.getName());
    }


    @Test
    public void mapPropertiesToTag() throws Exception {

        KeyListStructure keyListStructure = new KeyListStructure();
        keyListStructure.getKeyValue().add(new KeyValueStructure().withKey("TAG-0-name").withValue("name"));
        keyListStructure.getKeyValue().add(new KeyValueStructure().withKey("TAG-0-created").withValue(String.valueOf(now.toEpochMilli())));
        keyListStructure.getKeyValue().add(new KeyValueStructure().withKey("TAG-0-idReference").withValue("NMR:Vehicle:1"));
        keyListStructure.getKeyValue().add(new KeyValueStructure().withKey("TAG-1-name").withValue("name 2"));
        keyListStructure.getKeyValue().add(new KeyValueStructure().withKey("TAG-1-idReference").withValue("NMR:Vehicle:2"));

        Set<Tag> tags = tagKeyValuesMapper.mapPropertiesToTag(keyListStructure);

        assertThat(tags).hasSize(2);

    }

    @Test
    public void mapForthAndBack() throws Exception {
        String netexReference = "NMR:Vehicle:2";
        Tag tag = new Tag();
        tag.setCreated(now);
        tag.setName("name");
        tag.setCreatedBy("also me");
        tag.setIdreference(netexReference);
        tag.setRemovedBy("me");
        tag.setRemoved(now);
        tag.setComment("comment");

        Set<Tag> tags = Sets.newHashSet(tag);
        when(tagRepository.findByIdReference(netexReference)).thenReturn(tags);

        org.rutebanken.sobek.model.vehicle.Vehicle sobekVehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        sobekVehicle.setNetexId(netexReference);

        Vehicle Vehicle = new Vehicle();
        Vehicle.withKeyList(new KeyListStructure());
        tagKeyValuesMapper.mapTagsToProperties(sobekVehicle, Vehicle);


        Set<Tag> actual = tagKeyValuesMapper.mapPropertiesToTag(Vehicle.getKeyList());

        assertThat(actual.iterator().next()).isEqualTo(tags.iterator().next());
    }

}