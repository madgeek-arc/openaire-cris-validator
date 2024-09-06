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
    private final Job job;
    private final JobDao jobDao;
    private final RuleDao ruleDao;
    private final TaskListener[] listeners;

    public CRISValidatorTask(Job job, JobDao jobDao, RuleDao ruleDao, TaskListener... listeners) {
        this.job = job;
        this.jobDao = jobDao;
        this.ruleDao = ruleDao;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        final List<RuleResults> results = new LinkedList<>();
        Arrays.stream(listeners).forEach(s -> s.started(results));
        try {
            CRISValidator object = new CRISValidator(job.getUrl(), String.valueOf(job.getId()), ruleDao.getRuleMap());
            object.runTests(results, () -> {Arrays.stream(listeners).forEach(s -> s.updated(results)); return null;} );
            Arrays.stream(listeners).forEach(s -> s.finished(results));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Arrays.stream(listeners).forEach(l -> l.failed(results));
        }
        if (results != null && !results.isEmpty()) {
            for (RuleResults result : results) {
                StringBuilder errors = new StringBuilder();
                result.getErrors().forEach(e -> errors.append(e.getMessage()).append('\n'));
                logger.info("Method: {}  -> Records: {} | Failed: {}\nErrors:\n{}", result.getRule().getRuleMethodName(),
                        result.getCount(), result.getFailed(), errors);
            }
            logger.info("Job[{}]\n\tUsage Score: {}\n\tContent Score: {}", job.getId(), job.getUsageScore(), job.getContentScore());
        }
    }
}
