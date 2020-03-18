package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.model.RuleResults;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        List<RuleResults> results = new LinkedList<>();
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
        if (results != null && !results.isEmpty()) {
            for (RuleResults result : results) {
                StringBuilder errors = new StringBuilder();
                result.getErrors().forEach(e -> errors.append(e.getMessage()).append('\n'));
                logger.info("Method: {}  -> Records: {} | Failed: {}\nErrors:\n{}", result.getRuleMethodName(),
                        result.getCount(), result.getFailed(), errors);
            }
            logger.info("Job[{}]\n\tUsage Score: {}\n\tContent Score: {}", job.getId(), job.getUsageScore(), job.getContentScore());
        }
    }
}
