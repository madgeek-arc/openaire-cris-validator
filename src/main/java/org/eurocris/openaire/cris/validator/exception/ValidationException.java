package org.eurocris.openaire.cris.validator.exception;

import org.eurocris.openaire.cris.validator.Error;

public class ValidationException extends RuntimeException {

    private final Error error;

    public ValidationException(Error error) {
        super(error.getMessage());
        this.error = error;
    }

    public ValidationException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public ValidationException(Error error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
