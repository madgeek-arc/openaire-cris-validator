package org.eurocris.openaire.cris.validator.exception;

public class RecordException extends RuntimeException {

    private String identifier;

    public RecordException(String identifier, String message) {
        super(message  + "; value: " + identifier);
        this.identifier = identifier;
    }

    public RecordException(String identifier, String message, Throwable cause) {
        super(message  + "; value: " + identifier, cause);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
