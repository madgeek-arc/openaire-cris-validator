package org.eurocris.openaire.cris.validator.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CrisJob {

    @Id
    @GeneratedValue(generator = "negative-sequence")
    @GenericGenerator(
            name = "negative-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "negative_seq"),
                    @Parameter(name = "initial_value", value = "-1"),
                    @Parameter(name = "increment_size", value = "-1")
            }
    )
    @Column(updatable = false, nullable = false)
    private Long id;

    private String url;
    private String admin;
    private String status;
    private int usageScore = 0;
    private int contentScore = 0;
    private int totalRecords = 0;

    private String usageJobStatus;
    private String contentJobStatus;

    private Date dateSubmitted;
    private Date dateStarted;
    private Date dateFinished = null;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private List<ValidationResults> validationResults = new LinkedList<>();


    public enum Status {
        PENDING("pending"),
        ONGOING("ongoing"),
        FINISHED("finished"),
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

    public CrisJob() {
        this.status = Status.PENDING.getKey();
        this.dateSubmitted = new Date();
        this.dateStarted = new Date();
    }

    public CrisJob(String url, String admin) {
        this.url = url;
        this.admin = admin;

        this.status = Status.PENDING.getKey();
        this.dateSubmitted = new Date();
        this.dateStarted = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String user) {
        this.admin = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUsageScore() {
        return usageScore;
    }

    public void setUsageScore(int usageScore) {
        this.usageScore = usageScore;
    }

    public int getContentScore() {
        return contentScore;
    }

    public void setContentScore(int contentScore) {
        this.contentScore = contentScore;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getUsageJobStatus() {
        return usageJobStatus;
    }

    public void setUsageJobStatus(String usageJobStatus) {
        this.usageJobStatus = usageJobStatus;
    }

    public String getContentJobStatus() {
        return contentJobStatus;
    }

    public void setContentJobStatus(String contentJobStatus) {
        this.contentJobStatus = contentJobStatus;
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

    public List<ValidationResults> getRuleResults() {
        return validationResults;
    }

    public void setRuleResults(List<ValidationResults> validationResults) {
        this.validationResults = validationResults;
    }

    public String getReport() {
        return String.format("%nJob [%s]%nurl:\t\t\t%s%nuser:\t\t\t%s%nstatus:\t\t\t%s%nusage score:\t\t%s%ncontent score:\t\t%s%ndate submitted:\t%s%ndate started:\t%s%ndate finished:\t%s%n%n%n",
                this.getId(), this.getUrl(), this.getAdmin(), this.getStatus(), this.getUsageScore(), this.getContentScore(), this.getDateSubmitted(), this.getDateStarted(), this.getDateFinished());
    }
}
