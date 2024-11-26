package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.CrisJob;

import java.util.Optional;

public interface JobExecutor {

    /**
     * Get a submitted {@link CrisJob} using its ID {@param jobId}.
     *
     * @param jobId
     * @return
     */
    Optional<CrisJob> getJob(Long jobId);

    /**
     * Get the status of a submitted {@link CrisJob} using its ID {@param jobId}.
     *
     * @param jobId
     * @return
     */
    String getStatus(Long jobId);

    /**
     * Submits {@param job} for execution.
     *
     * @param crisJob
     * @return {@link CrisJob}
     */
    CrisJob submit(CrisJob crisJob);

    /**
     * Submit a new job using the {@param url} and {@param user} values.
     *
     * @param url
     * @param user
     * @return {@link CrisJob}
     */
    CrisJob submit(String url, String user);
}
