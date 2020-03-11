package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.util.ValidatorRuleResults;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CRISValidatorTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(CRISValidatorTask.class);
    private Job job;
    private JobDao jobDao;
    private TaskListener[] listeners;

    public CRISValidatorTask(Job job, JobDao jobDao, TaskListener... listeners) {
        this.job = job;
        this.jobDao = jobDao;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        Map<String, ValidatorRuleResults> results = new HashMap<>();
        Arrays.stream(listeners).forEach(TaskListener::started);
        try {
            CRISValidator object = new CRISValidator(job.getUrl(), job.getId());
            results = object.executeTests();
            for (TaskListener listener : listeners) {
                listener.finished(results);
            }
        } catch (Exception e) {
            logger.error("ERROR", e);
            Arrays.stream(listeners).forEach(l -> l.failed(null));
        }
        if (results != null && !results.entrySet().isEmpty()) {
            for (Map.Entry<String, ValidatorRuleResults> result : results.entrySet()) {
                StringBuilder errors = new StringBuilder();
                result.getValue().getErrors().forEach(e -> errors.append(e.getMessage()).append('\n'));
                logger.info("Method: {}  -> Records: {} | Failed: {}\nErrors:\n{}", result.getKey(),
                        result.getValue().getCount(), result.getValue().getFailed(), errors);
            }
            logger.info("Job[{}]\n\tUsage Score: {}\n\tContent Score: {}", job.getId(), job.getUsageScore(), job.getContentScore());
        }
    }
}
