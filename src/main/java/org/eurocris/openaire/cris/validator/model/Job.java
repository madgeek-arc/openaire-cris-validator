package org.eurocris.openaire.cris.validator.model;

import java.util.*;
import java.util.stream.Collectors;

public class Job {

    private String id;
    private String url;
    private String user;
    private String status;

    private Date dateSubmitted;
    private Date dateStarted = null;
    private Date dateFinished = null;

    private Map<String, String> ruleErrors = new LinkedHashMap<>();

    public enum Status {
        PENDING("pending"),
        ONGOING("ongoing"),
        SUCCESSFUL("successful"),
        FAILED("failed");

        private final String status;

        Status(final String status) {
            this.status = status;
        }

        public String getKey() {
            return status;
        }

        /**
         * @return the Enum representation for the given string.
         * @throws IllegalArgumentException if unknown string.
         */
        public static Status fromString(String s) throws IllegalArgumentException {
            return Arrays.stream(Status.values())
                    .filter(v -> v.status.equals(s))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + s + " ; Valid options: "
                            + Arrays.stream(values())
                            .map(Status::getKey)
                            .collect(Collectors.joining(", "))));
        }
    }

    public Job() {
        this.id = UUID.randomUUID().toString();
        this.status = "pending";
        this.dateSubmitted = new Date();
    }

    public Job(String url, String user) {
        this.url = url;
        this.user = user;

        this.id = UUID.randomUUID().toString();
        this.status = "pending";
        this.dateSubmitted = new Date();
    }

    public int getScore() {
        float score = 0;
        if (ruleErrors != null && !ruleErrors.isEmpty()) {
            for (Map.Entry<String, String> rule : ruleErrors.entrySet()) {
                if (rule.getValue() != null && rule.getValue().equals("")) {
                    score += (float) 1 / ruleErrors.size() * 100;
                }
            }
        }
        return Math.round(score);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(Date dateFinished) {
        this.dateFinished = dateFinished;
    }

    public Map<String, String> getRuleErrors() {
        return ruleErrors;
    }

    public void setRuleErrors(Map<String, String> ruleErrors) {
        this.ruleErrors = ruleErrors;
    }
}
