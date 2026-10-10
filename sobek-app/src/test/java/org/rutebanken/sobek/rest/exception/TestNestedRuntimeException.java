package org.rutebanken.sobek.rest.exception;

import org.springframework.core.NestedRuntimeException;

public class TestNestedRuntimeException extends NestedRuntimeException {

    public TestNestedRuntimeException(String msg) {
        super(msg);
    }

    public TestNestedRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}