package org.rutebanken.sobek.error;

import jakarta.validation.ValidationException;

/**
 * A rule about the kind of a vehicle type (physical or contractual) was broken by the incoming data.
 * It is a ValidationException, so it is reported to API users as a bad request (HTTP 400 over REST,
 * BAD_REQUEST over GraphQL) with the message of the exception.
 */
public class VehicleTypeKindViolationException extends ValidationException {

    public VehicleTypeKindViolationException(String message) {
        super(message);
    }
}
