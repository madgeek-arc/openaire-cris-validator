package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.CrisJob;
import org.eurocris.openaire.cris.validator.model.ValidationResults;
import org.eurocris.openaire.cris.validator.service.JobDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(StatusListener.class);
    private CrisJob crisJob;
    private final JobDao dao;

    public StatusListener(CrisJob crisJob, JobDao dao) {
        this.crisJob = crisJob;
        this.dao = dao;
    }

    @Override
    public void started(final List<ValidationResults> results) {
        crisJob.setUsageJobStatus(CrisJob.Status.ONGOING.getKey());
        crisJob.setContentJobStatus(CrisJob.Status.ONGOING.getKey());
        crisJob.setStatus(CrisJob.Status.ONGOING.getKey());
        crisJob.setDateStarted(new Date());
        crisJob.setRuleResults(results);
        crisJob = dao.save(crisJob);
        logger.info("Job[{}] -> {}", crisJob.getId(), crisJob.getStatus());
    }

    @Override
    public void updated(final List<ValidationResults> results) {
        if (results.size() == 3) {
            crisJob.setUsageJobStatus(CrisJob.Status.FINISHED.getKey());
            int usageScore = createScore(results, ValidationResults.USAGE);
            crisJob.setUsageScore(usageScore);
            if (usageScore <= 50) {
                crisJob.setUsageJobStatus(CrisJob.Status.FAILED.getKey());
            }
        }
        crisJob.setTotalRecords(recordsTested(results));
        crisJob.setContentScore(createScore(results, ValidationResults.CONTENT));
        crisJob.setRuleResults(results);
        crisJob = dao.save(crisJob);
        logger.info("Job[{}] -> {}", crisJob.getId(), crisJob.getStatus());
    }

    @Override
    public void finished(final List<ValidationResults> results) {
        crisJob.setUsageJobStatus(CrisJob.Status.FINISHED.getKey());
        crisJob.setContentJobStatus(CrisJob.Status.FINISHED.getKey());
        crisJob.setStatus(CrisJob.Status.SUCCESSFUL.getKey());
        crisJob.setDateFinished(new Date());
        crisJob.setRuleResults(results);
        crisJob.setTotalRecords(recordsTested(results));
        crisJob.setUsageScore(createScore(results, ValidationResults.USAGE));
        crisJob.setContentScore(createScore(results, ValidationResults.CONTENT));
        if (crisJob.getUsageScore() <= 50 || crisJob.getContentScore() <= 50) {
            crisJob.setStatus(CrisJob.Status.FAILED.getKey());
        }
        crisJob = dao.save(crisJob);
        logger.info("Job[{}] -> {}", crisJob.getId(), crisJob.getStatus());
    }

    @Override
    public void failed(final List<ValidationResults> results) {
        crisJob.setUsageJobStatus(CrisJob.Status.FAILED.getKey());
        crisJob.setContentJobStatus(CrisJob.Status.FAILED.getKey());
        crisJob.setStatus(CrisJob.Status.FAILED.getKey());
        crisJob.setDateFinished(new Date());
        crisJob.setRuleResults(results);
        crisJob.setTotalRecords(recordsTested(results));
        crisJob.setUsageScore(createScore(results, ValidationResults.USAGE));
        crisJob.setContentScore(createScore(results, ValidationResults.CONTENT));
        crisJob = dao.save(crisJob);
        logger.info("Job[{}] -> {}", crisJob.getId(), crisJob.getStatus());
    }

    private int createScore(List<ValidationResults> validationResults, String type) {
        float score = 0;
        List<Float> ruleScores = new ArrayList<>();
        if (validationResults != null && !validationResults.isEmpty()) {
            for (ValidationResults rResults : validationResults) {
                if (rResults.getType() != null && rResults.getType().equalsIgnoreCase(type)) {
                    // rule score: (total - failed) / total
                    float ruleScore;
                    if (rResults.getCount() != 0) {
                        ruleScore = ((float) rResults.getCount() - rResults.getFailed()) / rResults.getCount();
                        ruleScores.add(ruleScore);
                    }
                }
            }
            for (Float ruleScore : ruleScores) {
                score += ruleScore;
            }
            score = score / ruleScores.size() * 100;
        }
        return Math.round(score);
    }

    private int recordsTested(List<ValidationResults> results) {
        int records = 0;
        if (results != null && !results.isEmpty()) {
            for (ValidationResults validationResults : results) {
                if (validationResults.getType() != null && validationResults.getType().equalsIgnoreCase(ValidationResults.CONTENT)) {
                    records += (int) validationResults.getCount();
                }
            }
        }
        return records;
    }
}
