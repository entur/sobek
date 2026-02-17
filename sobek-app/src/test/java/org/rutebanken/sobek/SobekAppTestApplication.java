package org.rutebanken.sobek;

import org.rutebanken.sobek.auth.OAuth2Config;
import org.rutebanken.sobek.auth.SobekSecurityConfig;
import org.rutebanken.sobek.config.AuthorizationServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableTransactionManagement
@ComponentScan(
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SobekSecurityConfig.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SobekApplication.class),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = OAuth2Config.class),
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          value = AuthorizationServiceConfig.class)
    },
    basePackages = {"org.entur", "org.rutebanken.sobek"})
public class SobekAppTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(SobekAppTestApplication.class, args);
  }
}
