package org.eurocris.openaire.cris.validator.util;

import org.eurocris.openaire.cris.validator.model.ValidationError;

import java.util.ArrayList;
import java.util.List;

public class ValidatorRuleResults {

    private long count = 0;
    private long failed = 0;
    private List<ValidationError> errors = new ArrayList<>();

    public ValidatorRuleResults() {
    }

    public ValidatorRuleResults(long count, long failed, List<ValidationError> errors) {
        this.count = count;
        this.failed = failed;
        this.errors = errors;
    }

    public void incrCount() {
        this.failed++;
    }

    public void incrFailed() {
        this.failed++;
    }

    public void addError(ValidationError error) {
        if (this.errors.size() < 10) { // save only the first 10 errors
            this.errors.add(error);
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
