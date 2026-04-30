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

package org.rutebanken.sobek.netex.marshal;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.SobekTestApplication;
import org.rutebanken.sobek.netex.marshal.PublicationDeliveryUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


@SpringBootTest(classes = SobekTestApplication.class)
public class PublicationDeliveryUnmarshallerTest {

    @Autowired
    PublicationDeliveryUnmarshaller publicationDeliveryUnmarshaller;

    @Test
    public void expectUnmarshalExceptionWhenIncorrectPublicationDeliveryXml() throws IOException, SAXException, JAXBException {

        String notValidPublicationDeliveryXml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <PublicationDelivery xmlns="http://www.netex.org.uk/netex" xmlns:ns2="http://www.opengis.net/gml/3.2" xmlns:ns3="http://www.siri.org.uk/siri">
                </PublicationDeliivery>""";

        InputStream inputStream = new ByteArrayInputStream(notValidPublicationDeliveryXml.getBytes());

        Assertions.assertThatThrownBy(() -> publicationDeliveryUnmarshaller.unmarshal(inputStream))
                .isInstanceOf(UnmarshalException.class);

    }
}