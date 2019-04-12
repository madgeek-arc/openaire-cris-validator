package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CRISValidatorTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(CRISValidatorTask.class);
    private Job job;
    private TaskListener[] listeners;


    public CRISValidatorTask(Job newJob, TaskListener... listeners) {
        this.job = newJob;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        Map<String, String> results = new HashMap<>();
        Arrays.stream(listeners).forEach(TaskListener::started);
        try {
            CRISValidator object = new CRISValidator(job.getUrl(), job.getId());
            results = object.executeTests();
        } catch (MalformedURLException | SAXException e) {
            logger.error("ERROR", e);
            // TODO: get the errors of the validation
            Arrays.stream(listeners).forEach(l -> l.failed(null));
        }
        for (Map.Entry<String, String> result : results.entrySet())
            logger.info(String.format("Method: %s  -> %s", result.getKey(), result.getValue()));

        for (TaskListener listener : listeners) {
            listener.finished(results);
        }
    }
}
