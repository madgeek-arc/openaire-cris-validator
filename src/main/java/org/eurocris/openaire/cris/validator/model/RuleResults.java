package org.eurocris.openaire.cris.validator.model;

import org.openarchives.oai._2.RecordType;

import java.util.ArrayList;
import java.util.List;

public class RuleResults {

    private String ruleMethodName;
    private String type;
    private int ruleId = -1;
    private long count = 0;
    private long failed = 0;
    private List<ValidationError> errors = new ArrayList<>();

    public RuleResults() {
    }

    public RuleResults(String ruleMethodName, String type, int ruleId, long count, long failed, List<ValidationError> errors) {
        this.ruleMethodName = ruleMethodName;
        this.type = type;
        this.ruleId = ruleId;
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

    public String getRuleMethodName() {
        return ruleMethodName;
    }

    public void setRuleMethodName(String ruleMethodName) {
        this.ruleMethodName = ruleMethodName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
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
