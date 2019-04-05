package org.eurocris.openaire.cris.validator.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.Job;
import org.eurocris.openaire.cris.validator.service.JobDao;

import java.util.Date;
import java.util.Map;

public class StatusListener implements TaskListener<Map<String, String>> {

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
        job.setStatus("running");
        job.setDateStarted(new Date());
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }

    @Override
    public void finished(Map<String, String> results) {
        job.setStatus("finished");
        job.setDateFinished(new Date());
        job.setRules(results);
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }

    @Override
    public void failed(Map<String, String> errors) {
        job.setStatus("failed");
        job.setRules(errors);
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }
}
