package org.rutebanken.sobek;

import org.entur.auth.permission.client.spring.AuthPermissionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableTransactionManagement
@Import(TestSecurityConfiguration.class)
@ComponentScan(
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          value = SobekApplication.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = AuthPermissionProperties.class)
    },
    basePackages = {"org.entur", "org.rutebanken.sobek"})
public class SobekTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(SobekTestApplication.class, args);
  }
}
