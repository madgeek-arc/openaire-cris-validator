package org.eurocris.openaire.cris.validator.exception;

public class ValidationRuleException extends RuntimeException {

    private final String error;
    private final Object object;

    public ValidationRuleException(String error) {
        super(error);
        this.error = error;
        this.object = null;
    }

    public ValidationRuleException(String error, Object obj) {
        super(error);
        this.error = error;
        this.object = obj;
    }

    public String getError() {
        return error;
    }

    public Object getObject() {
        return object;
    }
}
