package org.eurocris.openaire.cris.validator.model;

public class ValidationError {
    private String identifier;
    private String message;
    private Object object;
    private Throwable throwable;

    public ValidationError() {
    }

    public ValidationError(String message) {
        this.message = message;
        this.throwable = new Exception(message);
    }

    public ValidationError(String message, Object object) {
        this.message = message;
        this.object = object;
    }

    public ValidationError(String message, Object object, Throwable throwable) {
        this.message = message;
        this.object = object;
        this.throwable = throwable;
    }

    public ValidationError(String identifier, String message, Object object, Throwable throwable) {
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
