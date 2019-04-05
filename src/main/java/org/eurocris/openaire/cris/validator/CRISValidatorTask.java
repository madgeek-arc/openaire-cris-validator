package org.eurocris.openaire.cris.validator;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.listeners.TaskListener;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.eurocris.openaire.cris.validator.CRISValidator.getParserSchema;

public class CRISValidatorTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(CRISValidatorTask.class);
    private Job job;
    private TaskListener[] listeners;


    CRISValidatorTask(Job newJob, TaskListener... listeners) {
        this.job = newJob;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        Map<String, String> results = new HashMap<>();
        Arrays.stream(listeners).forEach(TaskListener::started);
        try {
            CRISValidator.getEndpoint().set(new OAIPMHEndpoint(new URL(job.getUrl()), getParserSchema(), new FileLoggingConnectionStreamFactory("data/" + job.getId())));
            CRISValidator object = new CRISValidator();
            results = object.executeTests();
        } catch (MalformedURLException | SAXException | MissingArgumentException e) {
            logger.error("ERROR", e);
            Arrays.stream(listeners).forEach(l -> l.failed(null));
        }
        for (Map.Entry<String, String> result : results.entrySet())
            logger.info(String.format("Method: %s  -> %s", result.getKey(), result.getValue()));

        for (TaskListener listener : listeners) {
            listener.finished(results);
        }
    }
}
