package org.eurocris.openaire.cris.validator.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.service.JobDao;
import org.eurocris.openaire.cris.validator.util.PropertiesUtils;
import org.eurocris.openaire.cris.validator.util.ValidatorRuleResults;

import java.util.Date;
import java.util.Map;

public class StatusListener implements TaskListener {

    private static final Logger logger = LogManager.getLogger(StatusListener.class);
    private Job job;
    private JobDao dao;

    private Map<String, Float> ruleWeights;

    public StatusListener(Job job, JobDao dao) {
        this.job = job;
        this.dao = dao;
        this.ruleWeights = PropertiesUtils.getRuleWeights("/cris.properties");
        dao.save(job);
    }

    @Override
    public void started() {
        job.setStatus(Job.Status.ONGOING.getKey());
        job.setDateStarted(new Date());
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void finished(Map<String, ValidatorRuleResults> results) {
        job.setStatus(Job.Status.SUCCESSFUL.getKey());
        job.setDateFinished(new Date());
        job.setRuleResults(results);
        job.setUsageScore(createScore(results, ruleWeights, CRISValidator.USAGE));
        job.setContentScore(createScore(results, ruleWeights, CRISValidator.CONTENT));
        if (job.getUsageScore() <= 50 && job.getContentScore() <= 50) {
            job.setStatus(Job.Status.FAILED.getKey());
        }
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    @Override
    public void failed(Map<String, ValidatorRuleResults> errors) {
        job.setStatus(Job.Status.FAILED.getKey());
        job.setRuleResults(errors);
        job.setUsageScore(0);
        job.setContentScore(0);
        dao.save(job);
        logger.info("Job[{}] -> {}", job.getId(), job.getStatus());
    }

    private int createScore(Map<String, ValidatorRuleResults> resultsMap, Map<String, Float> ruleWeights, String type) {
        float score = 0;
        int rulesCount = 0;
        if (resultsMap != null && !resultsMap.isEmpty()) {
            for (Map.Entry<String, ValidatorRuleResults> rule : resultsMap.entrySet()) {
                if (CRISValidator.methodsMap.get(rule.getKey()).equals(type)) {
                    rulesCount++;
                    if (rule.getValue() != null) {
                        // rule score: (total - failed) / total
                        float ruleScore = 0;
                        if (rule.getValue().getCount() != 0) {
                            ruleScore = (float) (rule.getValue().getCount() - rule.getValue().getFailed()) / rule.getValue().getCount();
                            if (ruleWeights.get(rule.getKey()) != null) {
                                score += ruleScore * ruleWeights.get(rule.getKey());
                            } else {
                                score += ruleScore / rulesCount * 100;
                            }
                        }
                    }
                }
            }
        }
        return Math.round(score);
    }
}
