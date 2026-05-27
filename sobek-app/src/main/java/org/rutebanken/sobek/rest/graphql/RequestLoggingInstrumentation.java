package org.rutebanken.sobek.rest.graphql;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.*;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.rutebanken.sobek.config.HttpConfig.ET_CLIENT_NAME_HEADER;

@Component
public class RequestLoggingInstrumentation extends SimplePerformantInstrumentation {
    public static final Logger logger = LoggerFactory.getLogger(RequestLoggingInstrumentation.class);

    private final HttpServletRequest httpServletRequest;

    public RequestLoggingInstrumentation(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public  InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters, InstrumentationState state) {
        long startMillis = System.currentTimeMillis();
        var executionId = parameters.getExecutionInput().getExecutionId();

        final String etClientName = httpServletRequest.getHeader(ET_CLIENT_NAME_HEADER);

        logger.debug("[{}] ClientId: {},  Query: {}", executionId,etClientName, parameters.getQuery());
            if (parameters.getVariables() != null && !parameters.getVariables().isEmpty()) {
                logger.info("[{}] ClientId: {}, variables: {}", executionId,etClientName, parameters.getVariables());
            }

            return SimpleInstrumentationContext.whenCompleted(((executionResult, throwable) -> {

                    long endMillis = System.currentTimeMillis();
                    long duration = endMillis - startMillis;
                    if (throwable == null) {
                        logger.debug("[{}] ClientId: {}, completed in {}ms", executionId,etClientName, duration);

                    } else {
                        logger.warn("Failed in: {} ", duration, throwable);
                    }

            }));

        }

}
