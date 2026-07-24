package org.rutebanken.sobek.organisation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DefaultOrgRegisterClient {

    @Value("${netex.organisations.max-in-memory-size:500KB}")
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
}
