package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.Job;

import java.util.*;

public class MapJobDao implements JobDao {

    private static final Logger logger = LogManager.getLogger(MapJobDao.class);
    private Map<String, Job> jobs = new LinkedHashMap<>();

    public MapJobDao() {

    }

    @Override
    public Optional<Job> get(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    @Override
    public List<Job> getJobs(String userId) {
        List<Job> jobsList = new LinkedList<>();
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            if (entry.getValue().getUser().equals(userId)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<Job> getAll() {
        return new LinkedList<>(jobs.values());
    }

    @Override
    public void save(Job t) {
        jobs.put(t.getId(), t);
    }

    @Override
    public void delete(Job t) {
        if (jobs.containsKey(t.getId())) {
            jobs.remove(t);
        }
    }
}
