package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.model.ValidationResults;
import org.eurocris.openaire.cris.validator.service.JobDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(StatusListener.class);
    private Job job;
    private JobDao dao;

    public StatusListener(Job job, JobDao dao) {
        this.job = job;
        this.dao = dao;
        dao.save(job);
    }

    @Override
    public void started(final List<ValidationResults> results) {
        job.setUsageJobStatus(Job.Status.ONGOING.getKey());
        job.setContentJobStatus(Job.Status.ONGOING.getKey());
        job.setStatus(Job.Status.ONGOING.getKey());
        job.setDateStarted(new Date());
        job.setRuleResults(results);
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void updated(final List<ValidationResults> results) {
        if (results.size() == 3) {
            job.setUsageJobStatus(Job.Status.FINISHED.getKey());
            int usageScore = createScore(results, ValidationResults.USAGE);
            job.setUsageScore(usageScore);
            if (usageScore <= 50) {
                job.setUsageJobStatus(Job.Status.FAILED.getKey());
            }
        }
        job.setRecordsTested(recordsTested(results));
        job.setContentScore(createScore(results, ValidationResults.CONTENT));
        job.setRuleResults(results);
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void finished(final List<ValidationResults> results) {
        job.setUsageJobStatus(Job.Status.FINISHED.getKey());
        job.setContentJobStatus(Job.Status.FINISHED.getKey());
        job.setStatus(Job.Status.SUCCESSFUL.getKey());
        job.setDateFinished(new Date());
        job.setRuleResults(results);
        job.setRecordsTested(recordsTested(results));
        job.setUsageScore(createScore(results, ValidationResults.USAGE));
        job.setContentScore(createScore(results, ValidationResults.CONTENT));
        if (job.getUsageScore() <= 50 || job.getContentScore() <= 50) {
            job.setStatus(Job.Status.FAILED.getKey());
        }
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void failed(final List<ValidationResults> results) {
        job.setUsageJobStatus(Job.Status.FAILED.getKey());
        job.setContentJobStatus(Job.Status.FAILED.getKey());
        job.setStatus(Job.Status.FAILED.getKey());
        job.setDateFinished(new Date());
        job.setRuleResults(results);
        job.setRecordsTested(recordsTested(results));
        job.setUsageScore(createScore(results, ValidationResults.USAGE));
        job.setContentScore(createScore(results, ValidationResults.CONTENT));
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
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
