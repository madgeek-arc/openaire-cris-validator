package org.eurocris.openaire.cris.validator.exception;

import org.eurocris.openaire.cris.validator.Error;

public class RecordException extends ValidationException {

    private final String identifier;

    public RecordException(Error error, String identifier, String message) {
        super(error, message  + "; value: " + identifier);
        this.identifier = identifier;
    }

    public RecordException(Error error, String identifier, String message, Throwable cause) {
        super(error, message  + "; value: " + identifier, cause);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
