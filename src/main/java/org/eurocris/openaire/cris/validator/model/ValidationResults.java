package org.eurocris.openaire.cris.validator.model;

import org.eurocris.openaire.cris.validator.Error;
import org.eurocris.openaire.cris.validator.exception.RecordException;
import org.eurocris.openaire.cris.validator.exception.RecordValidationException;
import org.eurocris.openaire.cris.validator.exception.ValidationException;

import java.util.*;

public class ValidationResults {

    private static final int NUM_ERRORS = 50;
    public static final String USAGE = "USAGE";
    public static final String CONTENT = "CONTENT";

    private Set<String> metadataPrefixSet = new HashSet<>();
    private String set;
    private String type = CONTENT;
    private long count = 0;
    private long failed = 0;
    private Map<Error, Long> rulesFailedCounts = new HashMap<>();
    private List<ValidationError> errors = new ArrayList<>();

    public ValidationResults() {
    }

    public ValidationResults(Set<String> metadataPrefixSet, String set, String type, long count, long failed, List<ValidationError> errors) {
        this.metadataPrefixSet = metadataPrefixSet;
        this.set = set;
        this.type = type;
        this.count = count;
        this.failed = failed;
        this.errors = errors;
    }

    public void add(ValidationResults results) {
        if (this.set == null) {
            this.set = results.getSet();
        }
        if (Objects.equals(this.set, results.getSet())) {
            this.count += results.getCount();
            this.failed += results.getFailed();
            this.errors.addAll(results.getErrors());
            for (Map.Entry<Error, Long> ruleCount : results.getRulesFailedCounts().entrySet()) {
                this.rulesFailedCounts.compute(ruleCount.getKey(), (k, v) -> v == null ? ruleCount.getValue() : v + ruleCount.getValue());
            }
        }
    }

    public Set<String> getMetadataPrefixSet() {
        return metadataPrefixSet;
    }

    public ValidationResults setMetadataPrefixSet(Set<String> metadataPrefixSet) {
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
        rulesFailedCounts.merge(Error.GENERAL_ERROR, 1L, Long::sum);
    }

    public void addError(ValidationException e) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(e.getError(), null, e.getMessage(), null, e));
        }
        rulesFailedCounts.merge(e.getError(), 1L, Long::sum);
    }

    public void addError(RecordException e) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(e.getError(), e.getIdentifier(), e.getMessage(), null, e));
        }
        rulesFailedCounts.merge(e.getError(), 1L, Long::sum);
    }

    public void addError(RecordValidationException e) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(e.getError(), e.getIdentifier(), e.getMessage(), e.getElementLocalName(), e));
        }
        rulesFailedCounts.merge(e.getError(), 1L, Long::sum);
    }

    public void addError(Throwable throwable) {
        if (this.errors.size() < NUM_ERRORS) { // save only the first X errors
            this.errors.add(new ValidationError(Error.GENERAL_ERROR, throwable.getMessage(), null, throwable));
        }
        rulesFailedCounts.merge(Error.GENERAL_ERROR, 1L, Long::sum);
    }

    public String getSet() {
        return set;
    }

    public ValidationResults setSet(String set) {
        this.set = set;
        return this;
    }

    public String getType() {
        return type;
    }

    public ValidationResults setType(String type) {
        this.type = type;
        return this;
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

    public Map<Error, Long> getRulesFailedCounts() {
        return rulesFailedCounts;
    }

    public void setRulesFailedCounts(Map<Error, Long> rulesFailedCounts) {
        this.rulesFailedCounts = rulesFailedCounts;
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
