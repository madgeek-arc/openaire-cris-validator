package org.eurocris.openaire.cris.validator.exception;

public class ValidationMethodException extends RuntimeException {

    public ValidationMethodException(String error) {
        super(error);
    }

    public ValidationMethodException(String error, Throwable e) {
        super(error, e);
    }
}
