package org.eurocris.openaire.cris.validator.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.service.JobDao;
import org.eurocris.openaire.cris.validator.util.ValidationResults;

import java.util.Date;
import java.util.Map;

public class StatusListener implements TaskListener<Map<String, ValidationResults>> {

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
        job.setStatus(Job.Status.ONGOING.getKey());
        job.setDateStarted(new Date());
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }

    @Override
    public void finished(Map<String, ValidationResults> results) {
        job.setStatus(Job.Status.SUCCESSFUL.getKey());
        job.setDateFinished(new Date());
        job.setRuleResults(results);
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }

    @Override
    public void failed(Map<String, ValidationResults> errors) {
        job.setStatus(Job.Status.FAILED.getKey());
        job.setRuleResults(errors);
        dao.save(job);
        logger.info(String.format("Job[%s] -> %s", job.getId(), job.getStatus()));
    }
}
