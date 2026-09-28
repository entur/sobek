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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.validation.NeTExValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.io.ByteStreams.toByteArray;
import static jakarta.xml.bind.JAXBContext.newInstance;

@Component
public class PublicationDeliveryUnmarshaller {

    private static final Logger logger = LoggerFactory.getLogger(PublicationDeliveryUnmarshaller.class);

    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = newInstance(PublicationDeliveryStructure.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private NeTExValidator neTExValidator;

    @Value("${publicationDeliveryUnmarshaller.validateAgainstSchema:true}")
    private boolean validateAgainstSchema;

    public PublicationDeliveryUnmarshaller() {
        //TODO
    }

    public PublicationDeliveryStructure unmarshal(InputStream inputStream) throws JAXBException, IOException, SAXException {
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());


        if(logger.isDebugEnabled()) {
            String xml = new String(toByteArray(inputStream));
            logger.debug("Debug is enabled. Will log the input (this kills performance.):\n{}", xml);
            inputStream = new ByteArrayInputStream(xml.getBytes());
            logger.debug("Valdiation enabled? {}", validateAgainstSchema);
        }

        if(validateAgainstSchema) {
            this.neTExValidator = NeTExValidator.getNeTExValidator();
            jaxbUnmarshaller.setSchema(neTExValidator.getSchema());
        }

        logger.debug("Unmarshalling incoming publication delivery structure. Schema validation enabled: {}", validateAgainstSchema);

        // Create XXE-protected SAXSource
        SAXSource saxSource = createSecureSAXSource(inputStream);

        JAXBElement<PublicationDeliveryStructure> jaxbElement = jaxbUnmarshaller.unmarshal(saxSource, PublicationDeliveryStructure.class);
        PublicationDeliveryStructure publicationDeliveryStructure = jaxbElement.getValue();
        logger.debug("Done unmarshalling incoming publication delivery structure with schema validation enabled: {}", validateAgainstSchema);
        return publicationDeliveryStructure;
    }

    /**
     * Creates a secure SAXSource with XXE protection enabled.
     * This prevents XML External Entity (XXE) attacks by disabling external entity processing.
     *
     * @param inputStream the input stream containing XML data
     * @return a secure SAXSource
     * @throws SAXException if a SAX error occurs
     */
    private SAXSource createSecureSAXSource(InputStream inputStream) throws SAXException {
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            
            // Disable external entity processing to prevent XXE attacks
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            // Disable DTD processing entirely
            saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // Enable secure processing
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            
            // Disable XInclude processing
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setXIncludeAware(false);
            
            // Create the SAXSource with the secure parser
            return new SAXSource(saxParserFactory.newSAXParser().getXMLReader(), new org.xml.sax.InputSource(inputStream));
            
        } catch (ParserConfigurationException e) {
            throw new SAXException("Failed to configure secure XML parser", e);
        }
    }
}
