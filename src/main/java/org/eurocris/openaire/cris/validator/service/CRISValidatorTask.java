package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.CrisJob;
import org.eurocris.openaire.cris.validator.model.ValidationResults;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CRISValidatorTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(CRISValidatorTask.class);
    private CrisJob crisJob;
    private final JobDao jobDao;
    private final TaskListener[] listeners;

    public CRISValidatorTask(CrisJob crisJob, JobDao jobDao, TaskListener... listeners) {
        this.crisJob = crisJob;
        this.jobDao = jobDao;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        final List<ValidationResults> results = new LinkedList<>();
        Arrays.stream(listeners).forEach(s -> s.started(results));
        try {
            CRISValidator object = new CRISValidator(crisJob.getUrl());
            object.runTests(results, () -> {Arrays.stream(listeners).forEach(s -> s.updated(results)); return null;} );
            Arrays.stream(listeners).forEach(s -> s.finished(results));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Arrays.stream(listeners).forEach(l -> l.failed(results));
        }
    }
}
