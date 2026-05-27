package org.rutebanken.sobek.config;

import org.rutebanken.sobek.rest.graphql.scalars.DateScalar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    private final DateScalar dateScalar;

    public GraphQLConfig(DateScalar dateScalar) {
        this.dateScalar = dateScalar;
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(dateScalar.getGraphQLDateScalar());
    }
}