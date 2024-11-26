package org.eurocris.openaire.cris.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eurocris.openaire.cris.validator.Error;

public class ValidationError {
    private String error;
    private String identifier;
    private String message;
    private Object object;

    @JsonIgnore
    private Throwable throwable;

    public ValidationError() {
    }

    public ValidationError(Error error, String message) {
        this.error = error.getMessage();
        this.message = message;
        this.throwable = new Exception(message);
    }

    public ValidationError(Error error, Throwable throwable) {
        this.error = error.getMessage();
        this.message = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
    }

    public ValidationError(Error error, String message, Object object, Throwable throwable) {
        this.error = error.getMessage();
        this.message = message;
        this.object = object;
        this.throwable = throwable;
    }

    public ValidationError(Error error, String identifier, String message, Object object, Throwable throwable) {
        this.error = error.getMessage();
        this.identifier = identifier;
        this.message = message;
        this.object = object;
        this.throwable = throwable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ValidationError setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
