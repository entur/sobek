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

package org.rutebanken.sobek.rest.netex.publicationdelivery;

import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.sobek.SobekTestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SobekTestApplication.class)
public class PublicationDeliveryStreamingOutputTest {

    @Autowired
    private PublicationDeliveryStreamingOutput publicationDeliveryStreamingOutput;

    /**
     * Publication delivery should not validate.
     * It does not, for instance, contain publication timestamp nor participant ref.
     */
    @Test
    public void cannotStreamInvalidXml() throws Exception {

        PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();

        StreamingResponseBody streamingResponseBody = publicationDeliveryStreamingOutput.stream(publicationDelivery);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        assertThatThrownBy(() -> {
            streamingResponseBody.writeTo(byteArrayOutputStream);
        }).hasRootCauseInstanceOf(SAXParseException.class);
    }

    @Test
    public void streamValidXml() throws Exception {
        PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure()
                .withPublicationTimestamp(LocalDateTime.now())
                .withParticipantRef("participantRef");

        StreamingResponseBody streamingResponseBody = new PublicationDeliveryStreamingOutput().stream(publicationDelivery);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        streamingResponseBody.writeTo(byteArrayOutputStream);
    }

}