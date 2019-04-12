package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.Job;

import java.util.List;
import java.util.Optional;

public interface JobDao {

    /**
     * Get the {@link Job} with id {@param id}.
     *
     * @param id
     * @return {@link Optional<Job>}
     */
    Optional<Job> get(String id);

    /**
     * Get all Jobs of the user with ID {@param userId}.
     *
     * @param userId
     * @return {@link List<Job>}
     */
    List<Job> getJobs(String userId);

    /**
     * Get a {@link List} with all Jobs.
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
