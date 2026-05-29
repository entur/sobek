package org.rutebanken.sobek.config;

import org.rutebanken.sobek.rest.graphql.scalars.DateScalar;
import org.rutebanken.sobek.rest.graphql.scalars.DateTimeScalar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    private final DateScalar dateScalar;
    private final DateTimeScalar dateTimeScalar;

    public GraphQLConfig(DateScalar dateScalar, DateTimeScalar dateTimeScalar) {
        this.dateScalar = dateScalar;
        this.dateTimeScalar = dateTimeScalar;
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(dateScalar.getGraphQLDateScalar())
                .scalar(dateTimeScalar.getGraphQLDateScalar());
    }
}