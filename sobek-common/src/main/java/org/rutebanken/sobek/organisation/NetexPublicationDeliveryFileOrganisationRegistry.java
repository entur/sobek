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

package org.rutebanken.sobek.organisation;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "uttu.organisations.netex-file-uri")
public class NetexPublicationDeliveryFileOrganisationRegistry
        extends NetexPublicationDeliveryOrganisationRegistry {

    private final String netexFileUri;

    public NetexPublicationDeliveryFileOrganisationRegistry(
            @Value("${uttu.organisations.netex-file-uri}") String netexFileUri
    ) {
        this.netexFileUri = netexFileUri;
    }

    @Override
    protected Source getPublicationDeliverySource() {
        // Check if it's a classpath resource (starts with "classpath:")
        if (netexFileUri.startsWith("classpath:")) {
            String resourcePath = netexFileUri.substring("classpath:".length());
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }
            return new StreamSource(inputStream);
        } else {
            // Treat as file system path
            return new StreamSource(new java.io.File(netexFileUri));
        }
    }
}
