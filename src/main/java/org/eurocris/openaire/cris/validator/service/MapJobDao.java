package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.model.Job;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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
    public List<Job> getJobs(String userId, String validationStatus) {
        List<Job> jobsList = new LinkedList<>();
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            if (entry.getValue().getUser().equals(userId) && entry.getValue().getStatus().equals(validationStatus)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<Job> getJobs(String userId, int offset, int size) {
        List<Job> allUserJobs = getJobs(userId);
        int to = offset + size;
        return allUserJobs.subList(offset, to > allUserJobs.size() ? allUserJobs.size() : to);
    }

    @Override
    public List<Job> getJobs(String userId, Date dateFrom, Date dateTo) {
        List<Job> jobsList = new LinkedList<>();
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            if (entry.getValue().getUser().equals(userId)
                    && entry.getValue().getDateSubmitted().after(dateFrom)
                    && entry.getValue().getDateSubmitted().before(dateTo)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<Job> getJobs(String userId, int offset, int size, Date dateFrom, Date dateTo, String validationStatus) {
        List<Job> jobsList = new LinkedList<>();
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            if (entry.getValue().getUser().equals(userId)
                    && entry.getValue().getStatus().equals(validationStatus)
                    && entry.getValue().getDateSubmitted().after(dateFrom)
                    && entry.getValue().getDateSubmitted().before(dateTo)) {
                jobsList.add(entry.getValue());
            }
        }
        int to = offset + size;
        return jobsList.subList(offset, to > jobsList.size() ? jobsList.size() : to);
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
