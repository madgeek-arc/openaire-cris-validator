package org.eurocris.openaire.cris.validator.model;

import org.eurocris.openaire.cris.validator.exception.RecordException;
import org.eurocris.openaire.cris.validator.exception.RecordValidationException;

import java.util.*;

public class RuleResults {

    private static final int NUM_ERRORS = 50;

    private Set<String> metadataPrefixSet = new HashSet<>();
    private Rule rule;
    private long count = 0;
    private long failed = 0;
    private List<ValidationError> errors = new ArrayList<>();

    public RuleResults() {
    }

    public RuleResults(Set<String> metadataPrefixSet, Rule rule, long count, long failed, List<ValidationError> errors) {
        this.metadataPrefixSet = metadataPrefixSet;
        this.rule = rule;
        this.count = count;
        this.failed = failed;
        this.errors = errors;
    }

    public void add(RuleResults results) {
        if (this.rule == null) {
            this.rule = results.getRule();
        }
        if (Objects.equals(this.rule, results.getRule())) {
            this.count += results.getCount();
            this.failed += results.getFailed();
            this.errors.addAll(results.getErrors());
        }
    }

    public Set<String> getMetadataPrefixSet() {
        return metadataPrefixSet;
    }

    public RuleResults setMetadataPrefixSet(Set<String> metadataPrefixSet) {
        this.metadataPrefixSet = metadataPrefixSet;
        return this;
    }

    public void incrCount() {
        this.count++;
    }

    public void incrFailed() {
        this.failed++;
    }

    public void addError(ValidationError error) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(error);
        }
    }

    public void addError(RecordException e) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(e.getIdentifier(), e.getMessage(), null, e));
        }
    }

    public void addError(RecordValidationException e) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(e.getIdentifier(), e.getMessage(), e.getElementLocalName(), e));
        }
    }

    public void addError(Throwable throwable) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(throwable.getMessage(), null, throwable));
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

    public List<String> getFailedIds() {
        List<String> failed = new ArrayList<>();
        for (ValidationError error : errors) {
            if (error != null) {
                if (error.getIdentifier() != null) {
                    failed.add(error.getIdentifier());
                }
            }
        }
        return failed;
    }

    public boolean hasErrorMessage(String message) {
        for (ValidationError error : errors) {
            if (error.getMessage().equals(message)) {
                return true;
            }
        }
        return false;
    }
}
