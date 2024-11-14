package org.eurocris.openaire.cris.validator.exception;

public class RecordValidationException extends RuntimeException {

    private String identifier;
    private String elementLocalName;

    public RecordValidationException(String identifier, String elementLocalName) {
        super("While validating element " + elementLocalName + ": " + identifier);
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public RecordValidationException(String identifier, String elementLocalName, String message) {
        super(message);
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public RecordValidationException(String identifier, String elementLocalName, Throwable cause) {
        super("While validating element " + elementLocalName + ": " + identifier, cause);
        this.identifier = identifier;
        this.elementLocalName = elementLocalName;
    }

    public RecordValidationException(String identifier, String elementLocalName, String message, Throwable cause) {
        super(message, cause);
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
