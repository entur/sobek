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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class GeneralExceptionMapperTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void rawAccessDeniedExceptionYieldsForbidden() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new AccessDeniedException("Nope"), request);
        assertEquals(HttpStatus.FORBIDDEN, rsp.getStatusCode());
    }

    @Test
    public void nestedAccessDeniedExceptionYieldsForbidden() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(
                new TransactionSystemException("", new AccessDeniedException("Nope")), request);
        assertEquals(HttpStatus.FORBIDDEN, rsp.getStatusCode());
        assertNotNull(rsp.getBody());
        assertEquals("Nope", rsp.getBody().errors.getFirst().message);
    }

    @Test
    public void nestedValidationExceptionYieldsBadRequest() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(
                new TransactionSystemException("", new ValidationException()), request);
        assertEquals(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
    }

    @Test
    public void nestedUnknownExceptionYieldsInternalServerError() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(
                new TransactionSystemException("", new RuntimeException()), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rsp.getStatusCode());
    }

    @Test
    public void nestedNotAuthenticatedExceptionYieldsUnauthorized() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(
                new TransactionSystemException("", new NotAuthenticatedException("Njet")), request);
        assertEquals(HttpStatus.UNAUTHORIZED, rsp.getStatusCode());
    }

    @Test
    public void rawUnknownExceptionYieldsInternalServerError() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new FileNotFoundException(), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, rsp.getStatusCode());
    }

    @Test
    public void errorResponseEntityContainsMessage() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new RuntimeException("Test message"), request);
        assertNotNull(rsp.getBody());
        assertEquals("Test message", rsp.getBody().errors.getFirst().message);
    }

    @Test
    public void acceptHeaderXmlReturnsXmlContentType() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Accept")).thenReturn("application/xml");
        
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new RuntimeException("Test"), request);
        
        assertEquals(MediaType.APPLICATION_XML, rsp.getHeaders().getContentType());
    }

    @Test
    public void acceptHeaderJsonReturnsJsonContentType() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Accept")).thenReturn("application/json");
        
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new RuntimeException("Test"), request);
        
        assertEquals(MediaType.APPLICATION_JSON, rsp.getHeaders().getContentType());
    }

    @Test
    public void noAcceptHeaderDefaultsToJson() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Accept")).thenReturn(null);
        Mockito.when(request.getContentType()).thenReturn(null);
        
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new RuntimeException("Test"), request);
        
        assertEquals(MediaType.APPLICATION_JSON, rsp.getHeaders().getContentType());
    }

    @Test
    public void contentTypeXmlFallbackReturnsXml() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Accept")).thenReturn(null);
        Mockito.when(request.getContentType()).thenReturn("application/xml");
        
        ResponseEntity<ErrorResponseEntity> rsp = handler.handleException(new RuntimeException("Test"), request);
        
        assertEquals(MediaType.APPLICATION_XML, rsp.getHeaders().getContentType());
    }
}
