package org.rutebanken.sobek.error;

import jakarta.validation.ValidationException;

import java.util.Map;
import java.util.Optional;

/**
 * An organisation reference in the incoming data is not valid: it is missing, or the organisation is not in the
 * organisation registry. It is a ValidationException, so it is reported to API users as a bad request (HTTP 400 over
 * REST, BAD_REQUEST over GraphQL) with the message of the exception.
 * <p>
 * It is also a {@link CodedException}, so the error code that says what is wrong with the reference is kept.
 * <p>
 * This is only for the reference being wrong. A registry that cannot be read is not the fault of the data, and is not
 * reported with this exception.
 */
public class OrganisationRefViolationException extends ValidationException implements CodedException {

    private final ErrorCode code;
    private final SubCode subCode;
    private final Map<String, Object> metadata;

    public OrganisationRefViolationException(String message, CodedError codedError) {
        super(message);
        this.code = codedError.getErrorCode();
        this.subCode = codedError.getSubCode();
        this.metadata = codedError.getMetadata();
    }

    @Override
    public String getCode() {
        return Optional.ofNullable(code).map(ErrorCode::toString).orElse(null);
    }

    @Override
    public String getSubCode() {
        return Optional.ofNullable(subCode).map(SubCode::toString).orElse(null);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
