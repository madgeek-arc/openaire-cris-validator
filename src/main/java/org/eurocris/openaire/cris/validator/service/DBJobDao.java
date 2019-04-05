package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.Job;

import java.util.List;
import java.util.Optional;

public class DBJobDao implements JobDao {

    @Override
    public Optional<Job> get(String id) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public List<Job> getJobs(String userId) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public List<Job> getAll() {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public void save(Job t) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public void delete(Job t) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }
}
