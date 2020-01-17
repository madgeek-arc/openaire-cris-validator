package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.util.PropertiesUtils;
import org.eurocris.openaire.cris.validator.util.ValidatorRuleResults;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.util.*;

public class CRISValidatorTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(CRISValidatorTask.class);
    private Job job;
    private JobDao jobDao;
    private TaskListener[] listeners;

    private Map<String, Float> ruleWeights;

    public CRISValidatorTask(Job job, JobDao jobDao, TaskListener... listeners) {
        this.job = job;
        this.jobDao = jobDao;
        this.listeners = listeners;

        this.ruleWeights = PropertiesUtils.getRuleWeights("/cris.properties");
    }

    @Override
    public void run() {
        Map<String, ValidatorRuleResults> results = new HashMap<>();
        Arrays.stream(listeners).forEach(TaskListener::started);
        try {
            CRISValidator object = new CRISValidator(job.getUrl(), job.getId());
            results = object.executeTests();
            job.setRuleResults(results);
            job.setScore(createJobScore(results, ruleWeights));
            jobDao.save(job);
        } catch (MalformedURLException | SAXException e) {
            logger.error("ERROR", e);
            // TODO: get the errors of the validation
            Arrays.stream(listeners).forEach(l -> l.failed(null));
        }
        for (Map.Entry<String, ValidatorRuleResults> result : results.entrySet()) {
            StringBuilder errors = new StringBuilder();
            result.getValue().getErrors().forEach(e -> errors.append(e).append('\n'));
            logger.info("Method: {}  -> Records: {} | Failed: {}\nErrors:\n{}", result.getKey(),
                    result.getValue().getCount(), result.getValue().getFailed(), errors);
        }
        logger.info("Job[{}] - Score: {}", job.getId(), job.getScore());

        for (TaskListener listener : listeners) {
            listener.finished(results);
        }
    }

    private int createJobScore(Map<String, ValidatorRuleResults> resultsMap, Map<String, Float> ruleWeights) {
        float score = 0;
        if (resultsMap != null && !resultsMap.isEmpty()) {
            for (Map.Entry<String, ValidatorRuleResults> rule : resultsMap.entrySet()) {
                if (rule.getValue() != null) {
                    // rule score: (total - failed) / total
                    float ruleScore = 0;
                    if (rule.getValue().getCount() != 0) {
                        ruleScore = (float) (rule.getValue().getCount() - rule.getValue().getFailed()) / rule.getValue().getCount();
                        if (ruleWeights.get(rule.getKey()) != null) {
                            score += ruleScore * ruleWeights.get(rule.getKey());
                        } else {
                            score += ruleScore / resultsMap.size() * 100;
                        }
                    }
                }
            }
        }
        return Math.round(score);
    }
}
