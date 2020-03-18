package org.eurocris.openaire.cris.validator.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.model.RuleResults;
import org.eurocris.openaire.cris.validator.service.JobDao;

import java.util.Date;
import java.util.List;

public class StatusListener implements TaskListener {

    private static final Logger logger = LogManager.getLogger(StatusListener.class);
    private Job job;
    private JobDao dao;

    public StatusListener(Job job, JobDao dao) {
        this.job = job;
        this.dao = dao;
        dao.save(job);
    }

    @Override
    public void started() {
        job.setUsageJobStatus(Job.Status.ONGOING.getKey());
        job.setContentJobStatus(Job.Status.ONGOING.getKey());
        job.setStatus(Job.Status.ONGOING.getKey());
        job.setDateStarted(new Date());
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void finished(List<RuleResults> results) {
        job.setUsageJobStatus(Job.Status.FINISHED.getKey());
        job.setContentJobStatus(Job.Status.FINISHED.getKey());
        job.setStatus(Job.Status.SUCCESSFUL.getKey());
        job.setDateFinished(new Date());
        job.setRuleResults(results);
        job.setRecordsTested(recordsTested(results));
        job.setUsageScore(createScore(results, CRISValidator.USAGE));
        job.setContentScore(createScore(results, CRISValidator.CONTENT));
        if (job.getUsageScore() <= 50 || job.getContentScore() <= 50) {
            job.setStatus(Job.Status.FAILED.getKey());
        }
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void failed(List<RuleResults> errors) {
        job.setUsageJobStatus(Job.Status.FAILED.getKey());
        job.setContentJobStatus(Job.Status.FAILED.getKey());
        job.setStatus(Job.Status.FAILED.getKey());
        job.setRuleResults(errors);
        job.setRecordsTested(recordsTested(errors));
        job.setUsageScore(0);
        job.setContentScore(0);
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    private int createScore(List<RuleResults> ruleResults, String type) {
        float score = 0;
        if (ruleResults != null && !ruleResults.isEmpty()) {
            for (RuleResults rule : ruleResults) {
                if (CRISValidator.methodsMap.get(rule.getRuleMethodName()).equals(type)) {
                    // rule score: (total - failed) / total
                    float ruleScore = 0;
                    if (rule.getCount() != 0) {
                        ruleScore = (float) (rule.getCount() - rule.getFailed()) / rule.getCount();
                        score += ruleScore * rule.getWeight();
                    }
                }
            }
        }
        return Math.round(score);
    }

    private int recordsTested(List<RuleResults> results) {
        int records = 0;
        if (results != null && !results.isEmpty()) {
            for (RuleResults ruleResults : results) {
                if (ruleResults.getType().equals(CRISValidator.CONTENT)) {
                    records += ruleResults.getCount();
                }
            }
        }
        return records;
    }
}
