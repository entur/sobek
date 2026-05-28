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
import jakarta.validation.ValidationException;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
                       EntityNotFoundException.class, DataIntegrityViolationException.class));
        mapping.put(HttpStatus.CONFLICT, Set.of(EntityExistsException.class));
        mapping.put(HttpStatus.FORBIDDEN, Set.of(AccessDeniedException.class));
        mapping.put(HttpStatus.UNAUTHORIZED, Set.of(NotAuthenticatedException.class));
        mapping.put(HttpStatus.NOT_FOUND, Set.of(NoSuchElementException.class));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleException(Exception ex) {
        Throwable rootCause = getRootCause(ex);
        HttpStatus status = toStatus(rootCause);
        
        ErrorResponseEntity error = new ErrorResponseEntity(rootCause.getMessage());
        
        return ResponseEntity
                .status(status)
                .body(error);
    }

    protected HttpStatus toStatus(Throwable e) {
        for (Map.Entry<HttpStatus, Set<Class<?>>> entry : mapping.entrySet()) {
            if (entry.getValue().stream().anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
                return entry.getKey();
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Throwable getRootCause(Throwable e) {
        Throwable rootCause = e;

        if (e instanceof NestedRuntimeException nestedRuntimeException) {
            if (nestedRuntimeException.getRootCause() != null) {
                rootCause = nestedRuntimeException.getRootCause();
            }
        }
        return rootCause;
    }
}