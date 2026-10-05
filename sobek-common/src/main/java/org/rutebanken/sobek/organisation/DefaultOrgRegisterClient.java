package org.rutebanken.sobek.organisation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.entur.oauth2.AuthorizedWebClientBuilder;

@Configuration
public class DefaultOrgRegisterClient {

    @Value("${sobek.organisations.max-in-memory-size:500KB}")
    DataSize maxInMemorySize;

    @Bean("orgRegisterClient")
    @ConditionalOnMissingBean(name = "orgRegisterClient")
    WebClient orgRegisterClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .defaultHeader("Et-Client-Name", "entur-sobek")
                .exchangeStrategies(ExchangeStrategies
                        .builder()
                        .codecs(codecs -> codecs
                                .defaultCodecs()
                                .maxInMemorySize((int)maxInMemorySize.toBytes()))
                        .build())
                .build();
    }

    @Bean("authorizedOrgRegisterClient")
    @ConditionalOnMissingBean(name = "authorizedOrgRegisterClient")
    WebClient authorizedOrgRegisterClient(WebClient.Builder webClientBuilder,
                                OAuth2ClientProperties properties,
                                @Value("${sobek.oauth2.client.audience}") String audience
    ) {
        return new AuthorizedWebClientBuilder(webClientBuilder)
            .withOAuth2ClientProperties(properties)
            .withAudience(audience)
            .withClientRegistrationId("internal")
            .build()
            .mutate()
            .defaultHeader("Et-Client-Name", "entur-sobek")
            .exchangeStrategies(ExchangeStrategies
                .builder()
                .codecs(codecs -> codecs
                    .defaultCodecs()
                    .maxInMemorySize((int)maxInMemorySize.toBytes()))
                .build())
            .build();
    }

}
