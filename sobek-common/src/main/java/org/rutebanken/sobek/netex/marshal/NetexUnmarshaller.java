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

/* Copied from UTTU and refined for Sobek needs */

package org.rutebanken.sobek.netex.marshal;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread safe NetexUnmarshaller service
 */
public class NetexUnmarshaller {

  private static final Logger logger = LoggerFactory.getLogger(NetexUnmarshaller.class);

  // JAXBContext is thread safe so can be shared
  private final JAXBContext publicationDeliveryContext;

  public NetexUnmarshaller(Class... clazz) {
    publicationDeliveryContext = createContext(clazz);
  }

  public <T> T unmarshalFromSource(Source source)
    throws NetexUnmarshallerUnmarshalFromSourceException {
    try {
      // the Unmarshaller is not thread safe so must be created on every call
      JAXBElement<T> element = (JAXBElement<T>) getUnmarshaller().unmarshal(source);
      return element.getValue();
    } catch (JAXBException e) {
      throw new NetexUnmarshallerUnmarshalFromSourceException(source, e);
    }
  }

  private Unmarshaller getUnmarshaller() throws JAXBException {
    return publicationDeliveryContext.createUnmarshaller();
  }

  private static JAXBContext createContext(Class... clazz) {
    try {
      JAXBContext jaxbContext = newInstance(clazz);
      logger.trace("Created context {}", jaxbContext.getClass());
      return jaxbContext;
    } catch (JAXBException e) {
      throw new NetexUnmarshallerCreateContextException(e, clazz);
    }
  }
}
