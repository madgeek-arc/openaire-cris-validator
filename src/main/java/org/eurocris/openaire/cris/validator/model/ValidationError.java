package org.eurocris.openaire.cris.validator.model;

import org.eurocris.openaire.cris.validator.exception.ValidationRuleException;

public class ValidationError {
    private String message;
    private Object object;

    public ValidationError() {}

    public ValidationError(String message) {
        this.message = message;
    }

    public ValidationError(String message, Object object) {
        this.message = message;
        this.object = object;
    }

    public static ValidationError of(ValidationRuleException e) {
        return new ValidationError(e.getMessage(), e.getObject());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
