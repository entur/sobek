package org.rutebanken.sobek.rest.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
    }

    @Nested
    @DisplayName("HTTP Status Mapping Tests")
    class StatusMappingTests {

        @ParameterizedTest
        @MethodSource("exceptionToStatusProvider")
        @DisplayName("Should map exception types to correct HTTP status")
        void shouldMapExceptionToCorrectStatus(Exception exception, HttpStatus expectedStatus) {
            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertEquals(expectedStatus, response.getStatusCode());
        }

        static Stream<Arguments> exceptionToStatusProvider() {
            return Stream.of(
                Arguments.of(new ValidationException("Validation failed"), HttpStatus.BAD_REQUEST),
                Arguments.of(new OptimisticLockException("Lock failed"), HttpStatus.BAD_REQUEST),
                Arguments.of(new EntityNotFoundException("Not found"), HttpStatus.BAD_REQUEST),
                Arguments.of(new DataIntegrityViolationException("Integrity violation"), HttpStatus.BAD_REQUEST),
                Arguments.of(new EntityExistsException("Already exists"), HttpStatus.CONFLICT),
                Arguments.of(new AccessDeniedException("Access denied"), HttpStatus.FORBIDDEN),
                Arguments.of(new NotAuthenticatedException("Not authenticated"), HttpStatus.UNAUTHORIZED),
                Arguments.of(new NoSuchElementException("Element not found"), HttpStatus.NOT_FOUND),
                Arguments.of(new RuntimeException("Unknown error"), HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }

        @Test
        @DisplayName("Should find matching exception in nested chain")
        void shouldFindMatchingExceptionInChain() {
            Exception rootCause = new ValidationException("Validation error");
            Exception wrappedException = new RuntimeException("Wrapper", rootCause);

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(wrappedException, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle deeply nested exception chains")
        void shouldHandleDeeplyNestedChains() {
            Exception deepCause = new EntityNotFoundException("Deep cause");
            Exception middle = new RuntimeException("Middle", deepCause);
            Exception top = new RuntimeException("Top", middle);

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(top, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Error Message Extraction Tests")
    class MessageExtractionTests {

        @Test
        @DisplayName("Should extract message from direct exception")
        void shouldExtractMessageFromDirectException() {
            Exception exception = new ValidationException("Direct message");

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertNotNull(response.getBody());
            assertEquals("Direct message", response.getBody().errors.get(0).message);
        }

        @Test
        @DisplayName("Should extract message from matched exception in chain")
        void shouldExtractMessageFromMatchedException() {
            Exception rootCause = new EntityNotFoundException("Entity not found message");
            Exception wrapper = new RuntimeException("Wrapper message", rootCause);

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(wrapper, request);

            assertNotNull(response.getBody());
            assertEquals("Entity not found message", response.getBody().errors.get(0).message);
        }

        @Test
        @DisplayName("Should extract message from NestedRuntimeException root cause")
        void shouldExtractMessageFromNestedRuntimeException() {
            NestedRuntimeException exception = new TestNestedRuntimeException(
                "Outer message",
                new RuntimeException("Root cause message")
            );

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertNotNull(response.getBody());
            // Should use outer message first
            assertEquals("Outer message", response.getBody().errors.get(0).message);
        }

        @Test
        @DisplayName("Should fallback to class name when no message available")
        void shouldFallbackToClassNameWhenNoMessage() {
            Exception exception = new RuntimeException((String) null);

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertNotNull(response.getBody());
            assertEquals("RuntimeException", response.getBody().errors.get(0).message);
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            Exception exception = new RuntimeException("   ");

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertNotNull(response.getBody());
            assertEquals("RuntimeException", response.getBody().errors.get(0).message);
        }
    }

    @Nested
    @DisplayName("Content Type Negotiation Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should return XML when Accept header contains application/xml")
        void shouldReturnXmlForAcceptHeader() {
            when(request.getHeader("Accept")).thenReturn("application/xml");

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(
                new RuntimeException("Error"), request
            );

            assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        }

        @Test
        @DisplayName("Should prioritize Accept header over Content-Type")
        void shouldPrioritizeAcceptHeader() {
            when(request.getHeader("Accept")).thenReturn("application/json");
            when(request.getContentType()).thenReturn("application/xml");

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(
                new RuntimeException("Error"), request
            );

            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        }

        @Test
        @DisplayName("Should default to JSON when no headers specified")
        void shouldDefaultToJson() {
            ResponseEntity<ErrorResponseEntity> response = handler.handleException(
                new RuntimeException("Error"), request
            );

            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        }

        @Test
        @DisplayName("Should handle null Accept header")
        void shouldHandleNullAcceptHeader() {
            when(request.getHeader("Accept")).thenReturn(null);
            when(request.getContentType()).thenReturn(null);

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(
                new RuntimeException("Error"), request
            );

            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should create complete response with all components")
        void shouldCreateCompleteResponse() {
            when(request.getHeader("Accept")).thenReturn("application/json");
            Exception exception = new ValidationException("Validation failed");

            ResponseEntity<ErrorResponseEntity> response = handler.handleException(exception, request);

            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("Validation failed", response.getBody().errors.get(0).message),
                () -> assertEquals(1, response.getBody().errors.size())
            );
        }
    }

    // Helper class for testing NestedRuntimeException
    private static class TestNestedRuntimeException extends NestedRuntimeException {
        public TestNestedRuntimeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}