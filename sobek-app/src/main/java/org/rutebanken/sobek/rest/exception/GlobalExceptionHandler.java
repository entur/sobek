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

package org.rutebanken.sobek.rest.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Map<HttpStatus, Set<Class<?>>> mapping;

    public GlobalExceptionHandler() {
        mapping = new HashMap<>();
        mapping.put(HttpStatus.BAD_REQUEST,
                Set.of(ValidationException.class, OptimisticLockException.class, 
                       EntityNotFoundException.class, DataIntegrityViolationException.class, NetexMappingException.class));
        mapping.put(HttpStatus.CONFLICT, Set.of(EntityExistsException.class));
        mapping.put(HttpStatus.FORBIDDEN, Set.of(AccessDeniedException.class));
        mapping.put(HttpStatus.UNAUTHORIZED, Set.of(NotAuthenticatedException.class));
        mapping.put(HttpStatus.NOT_FOUND, Set.of(NoSuchElementException.class));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleException(Exception ex, HttpServletRequest request) {
        // Find the exception that determines the status
        Throwable exceptionForStatus = findExceptionForStatus(ex);
        HttpStatus status = toStatus(ex);
        
        // Extract message from the same exception that determined the status
        String errorMessage = extractErrorMessage(exceptionForStatus != null ? exceptionForStatus : ex);
        ErrorResponseEntity error = new ErrorResponseEntity(errorMessage);
        
        return ResponseEntity
                .status(status)
                .body(error);
    }

    protected HttpStatus toStatus(Throwable e) {
        Throwable matchedException = findExceptionForStatus(e);
        return matchedException != null ? matchStatus(matchedException) : HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    private Throwable findExceptionForStatus(Throwable e) {
        // Find the first exception in the chain that matches a configured classification
        Throwable current = e;
        while (current != null) {
            HttpStatus status = matchStatus(current);
            if (status != null) {
                return current;
            }

            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }

        return null;
    }

    private HttpStatus matchStatus(Throwable e) {
        for (Map.Entry<HttpStatus, Set<Class<?>>> entry : mapping.entrySet()) {
            if (entry.getValue().stream().anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
                return entry.getKey();
            }
        }

        return null;
    }

    private String extractErrorMessage(Throwable e) {
        // First try to get message from the exception itself
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        
        // For NestedRuntimeException, check root cause
        if (e instanceof NestedRuntimeException nestedRuntimeException) {
            Throwable rootCause = nestedRuntimeException.getRootCause();
            if (rootCause != null && rootCause.getMessage() != null && !rootCause.getMessage().trim().isEmpty()) {
                return rootCause.getMessage();
            }
        }
        
        // Check the immediate cause
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
            return cause.getMessage();
        }
        
        // If no message found, return exception class name
        return e.getClass().getSimpleName();
    }
}