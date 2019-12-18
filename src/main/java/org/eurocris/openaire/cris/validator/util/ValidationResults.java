package org.eurocris.openaire.cris.validator.util;

import java.util.ArrayList;
import java.util.List;

public class ValidationResults {

    private long count = 0;
    private long failed = 0;
    private List<String> errors = new ArrayList<>();

    public ValidationResults() {
    }

    public ValidationResults(long count, long failed, List<String> errors) {
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

    public void addError(String error) {
        this.errors.add(error);
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

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
