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

package org.rutebanken.sobek.config;


import org.rutebanken.sobek.filter.LoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {

    /**
     * Client ID header.
     * Is used for identifiying clients calling our API
     */
    public static final String ET_CLIENT_ID_HEADER = "ET-Client-ID";

    /**
     * Client Name header.
     * Is used for getting the name of clients calling our API.
     */
    public static final String ET_CLIENT_NAME_HEADER = "ET-Client-Name";

    @Bean
    public FilterRegistrationBean filterRegistrationBean(@Autowired LoggingFilter loggingFilter) {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(loggingFilter);
        registration.addUrlPatterns("/*");
        registration.setName("loggingFilter");
        registration.setOrder(1);
        return registration;
    }

}
