package org.rutebanken.sobek.rest.exception;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
    }

    @Test
    void shouldReturnJsonContentTypeByDefault() throws Exception {
        mockMvc.perform(get("/test/validation-exception"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].message").value("Validation failed"));
    }

    @Test
    void shouldReturnXmlContentTypeWhenAcceptHeaderIsXml() throws Exception {
        mockMvc.perform(get("/test/validation-exception")
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(xpath("/ErrorResponseEntity/errors/errors/message/text()")
                .string("Validation failed"));
    }

    @Test
    void shouldReturnJsonContentTypeWhenAcceptHeaderIsJson() throws Exception {
        mockMvc.perform(get("/test/not-found-exception")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].message").value("Item not found"));
    }

    @Test
    void shouldReturnJsonForWildcardAcceptHeader() throws Exception {
        mockMvc.perform(get("/test/runtime-exception")
                .accept("*/*"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].message").value("Something went wrong"));
    }

    @Test
    void shouldRespectQualityParametersInAcceptHeader() throws Exception {
        mockMvc.perform(get("/test/validation-exception")
                .header("Accept", "application/json;q=0.1, application/xml;q=0.9"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    void shouldPreferJsonWhenHigherQuality() throws Exception {
        mockMvc.perform(get("/test/validation-exception")
                .header("Accept", "application/json;q=0.9, application/xml;q=0.1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldHandleMultipleAcceptTypesWithoutQuality() throws Exception {
        // First compatible type should be chosen (after sorting by specificity)
        mockMvc.perform(get("/test/validation-exception")
                .header("Accept", "text/html, application/json, application/xml"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldHandleNestedExceptionsWithCorrectStatus() throws Exception {
        mockMvc.perform(get("/test/nested-exception")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].message").value("Nested not found"));
    }

    @Test
    void shouldIncludeErrorMessageInBothJsonAndXml() throws Exception {
        // Test JSON
        mockMvc.perform(get("/test/custom-message")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].message").value("Custom error message"));

        // Test XML
        mockMvc.perform(get("/test/custom-message")
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(xpath("/ErrorResponseEntity/errors/errors/message/text()")
                .string("Custom error message"));
    }

    /**
     * Test controller that throws various exceptions
     */
    @RestController
    @RequestMapping("/test")
    static class TestExceptionController {

        @GetMapping("/validation-exception")
        public String throwValidationException() {
            throw new ValidationException("Validation failed");
        }

        @GetMapping("/not-found-exception")
        public String throwNotFoundException() {
            throw new NoSuchElementException("Item not found");
        }

        @GetMapping("/runtime-exception")
        public String throwRuntimeException() {
            throw new RuntimeException("Something went wrong");
        }

        @GetMapping("/nested-exception")
        public String throwNestedException() {
            NoSuchElementException cause = new NoSuchElementException("Nested not found");
            throw new RuntimeException("Wrapper exception", cause);
        }

        @GetMapping("/custom-message")
        public String throwCustomMessage() {
            throw new ValidationException("Custom error message");
        }
    }
}