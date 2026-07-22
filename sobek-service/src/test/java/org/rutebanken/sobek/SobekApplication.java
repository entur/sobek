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

package org.rutebanken.sobek;

import org.rutebanken.sobek.exporter.AsyncPublicationDeliveryExporter;
import org.rutebanken.sobek.organisation.NetexPublicationDeliveryFileOrganisationRegistry;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.repository.OrganisationRepository;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Configuration
@EnableTransactionManagement
@ContextConfiguration(classes = SobekApplication.TestConfig.class)
@ComponentScan(basePackages = { "org.entur", "org.rutebanken.sobek"},
        excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AsyncPublicationDeliveryExporter.class, OrganisationRepository.class})})
public class SobekApplication {

    public static void main(String[] args) {
        SpringApplication.run(SobekApplication.class, args);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public CustomScopeConfigurer customScopeConfigurer() {
            CustomScopeConfigurer configurer = new CustomScopeConfigurer();
            configurer.addScope("request", new SimpleThreadScope());
            return configurer;
        }
    }

}

