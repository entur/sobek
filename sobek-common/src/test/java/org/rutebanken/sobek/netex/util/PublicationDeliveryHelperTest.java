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

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PublicationDeliveryHelperTest {

    @Autowired
    private PublicationDeliveryHelper publicationDeliveryHelper;

    @SuppressWarnings("unchecked")
    @Test
    public void findResourceFrameFromCompositeFrame() {
        ObjectFactory objectFactory = new ObjectFactory();

        PublicationDeliveryStructure publicationDeliveryStructure = new PublicationDeliveryStructure()
                .withDataObjects(
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(
                                        objectFactory.createCompositeFrame(
                                                new CompositeFrame()
                                                        .withFrames(new Frames_RelStructure()
                                                            .withCommonFrame(objectFactory.createCommonFrame(new ResourceFrame()))))));

        ResourceFrame resourceFrame = publicationDeliveryHelper.findResourceFrame(publicationDeliveryStructure);
        assertThat(resourceFrame).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findResourceFrameFromCommonFrame() {
        ObjectFactory objectFactory = new ObjectFactory();

        PublicationDeliveryStructure publicationDeliveryStructure = new PublicationDeliveryStructure()
                .withDataObjects(
                        new PublicationDeliveryStructure.DataObjects()
                                .withCompositeFrameOrCommonFrame(
                                        objectFactory.createCommonFrame(new ResourceFrame())));

        ResourceFrame resourceFrame = publicationDeliveryHelper.findResourceFrame(publicationDeliveryStructure);
        assertThat(resourceFrame).isNotNull();
    }

}