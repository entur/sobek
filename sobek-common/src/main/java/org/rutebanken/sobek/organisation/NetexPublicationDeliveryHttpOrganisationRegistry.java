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

import java.io.ByteArrayInputStream;
import java.time.Duration;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "netex.organisations.netex-http-uri")
public class NetexPublicationDeliveryHttpOrganisationRegistry
  extends NetexPublicationDeliveryOrganisationRegistry {

  private final String netexHttpUri;
  private final WebClient orgRegisterClient;

  public NetexPublicationDeliveryHttpOrganisationRegistry(
    @Value("${netex.organisations.netex-http-uri}") String netexHttpUri,
    @Value("${netex.organisations.cache-duration-seconds:3600}") String cacheDurationSeconds,
    WebClient orgRegisterClient
  ) {
      super(cacheDurationSeconds);
      this.netexHttpUri = netexHttpUri;
      this.orgRegisterClient = orgRegisterClient;
  }

  @Override
  protected Source getPublicationDeliverySource() {
    byte[] response = orgRegisterClient
      .get()
      .uri(netexHttpUri)
      .retrieve()
      .bodyToMono(byte[].class)
      .block(Duration.ofSeconds(30));

    if (response == null) {
      return null;
    }

    return new StreamSource(new ByteArrayInputStream(response));
  }
}