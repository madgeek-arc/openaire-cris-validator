package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.Job;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface JobDao {

    /**
     * Get a specific {@link Job}.
     *
     * @param id Identifier of the Job
     * @return {@link Optional<Job>}
     */
    Optional<Job> get(String id);

    /**
     * Get all user Jobs.
     *
     * @param userId User identifier
     * @return {@link List<Job>}
     */
    List<Job> getJobs(String userId);

    /**
     * Get user Jobs having a specific status.
     *
     * @param userId           User identifier
     * @param validationStatus
     * @return
     */
    List<Job> getJobs(String userId, String validationStatus);

    /**
     * Get user Jobs using paging.
     *
     * @param userId User identifier
     * @param offset the offset of the returned list
     * @param size   the size of objects returned
     * @return
     */
    List<Job> getJobs(String userId, int offset, int size);

    /**
     * Get user Jobs using date filters.
     *
     * @param userId
     * @param dateFrom minimum Job submission date
     * @param dateTo   maximum Job submission date
     * @return
     */
    List<Job> getJobs(String userId, Date dateFrom, Date dateTo);

    /**
     * Get user Jobs using date and status filters.
     *
     * @param userId           the user ID of the Jobs
     * @param offset           the offset of the returned list
     * @param size             the size of objects returned
     * @param dateFrom         minimum Job submission date
     * @param dateTo           maximum Job submission date
     * @param validationStatus the current status of the Job.
     * @return {@link List} of {@param size} {@link Job} objects starting from {@param offset}.
     */
    List<Job> getJobs(String userId, int offset, int size, Date dateFrom, Date dateTo, String validationStatus);

    /**
     * Get all Jobs.
     *
     * @return {@link List<Job>}
     */
    List<Job> getAll();

    /**
     * Saves the {@link Job} {@param t}.
     *
     * @param t
     */
    void save(Job t);

    /**
     * Deletes the {@link Job} {@param t}.
     *
     * @param t
     */
    void delete(Job t);

}
