package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.Job;

import java.util.Optional;

public interface JobExecutor {

    /**
     * Get a submitted {@link Job} using its ID {@param jobId}.
     *
     * @param jobId
     * @return
     */
    Optional<Job> getJob(String jobId);

    /**
     * Get the status of a submitted {@link Job} using its ID {@param jobId}.
     *
     * @param jobId
     * @return
     */
    String getStatus(String jobId);

    /**
     * Submits {@param job} for execution.
     *
     * @param job
     * @return {@link Job}
     */
    Job submit(Job job);

    /**
     * Submit a new job using the {@param url} and {@param user} values.
     *
     * @param url
     * @param user
     * @return {@link Job}
     */
    Job submit(String url, String user);
}
