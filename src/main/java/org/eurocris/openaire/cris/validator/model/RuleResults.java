package org.eurocris.openaire.cris.validator.model;

import org.openarchives.oai._2.RecordType;

import java.util.ArrayList;
import java.util.List;

public class RuleResults {

    private Rule rule;
    private long count = 0;
    private long failed = 0;
    private List<ValidationError> errors = new ArrayList<>();

    public RuleResults() {
    }

    public RuleResults(Rule rule, long count, long failed, List<ValidationError> errors) {
        this.rule = rule;
        this.count = count;
        this.failed = failed;
        this.errors = errors;
    }

    public void incrCount() {
        this.count++;
    }

    public void incrFailed() {
        this.failed++;
    }

    public void addError(ValidationError error) {
        if (this.errors.size() < 10) { // save only the first 10 errors
            this.errors.add(error);
        }
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
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

    public List<String> getErrorMessages() {
        List<String> errorMessages = new ArrayList<>();
        for (ValidationError error : errors) {
            if (error != null) {
                if (error.getObject() instanceof RecordType) {
                    errorMessages.add(((RecordType) error.getObject()).getHeader().getIdentifier());
                } else if (error.getMessage() != null) {
                    errorMessages.add(error.getMessage());
                }
            }
        }
        return errorMessages;
    }
}
