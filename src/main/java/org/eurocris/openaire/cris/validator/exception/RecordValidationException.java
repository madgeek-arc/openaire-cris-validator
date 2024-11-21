package org.eurocris.openaire.cris.validator.exception;

import org.eurocris.openaire.cris.validator.Error;

public class RecordValidationException extends ValidationException {

    private String identifier;
    private String elementLocalName;

    public RecordValidationException(Error error, String identifier, String elementLocalName) {
        super(error, error.getMessage());
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public RecordValidationException(Error error, String identifier, String elementLocalName, String message) {
        super(error, message);
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public RecordValidationException(Error error, String identifier, String elementLocalName, Throwable cause) {
        super(error, cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage(), cause);
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getElementLocalName() {
        return elementLocalName;
    }
}
