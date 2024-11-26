package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.model.CrisJob;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MapJobDao implements JobDao {

    private static final Logger logger = LogManager.getLogger(MapJobDao.class);
    private Map<String, CrisJob> jobs = new LinkedHashMap<>();

    public MapJobDao() {

    }

    @Override
    public Optional<CrisJob> get(Long id) {
        return Optional.ofNullable(jobs.get(id));
    }

    @Override
    public List<CrisJob> getJobs(String userId) {
        List<CrisJob> jobsList = new LinkedList<>();
        for (Map.Entry<String, CrisJob> entry : jobs.entrySet()) {
            if (entry.getValue().getAdmin().equals(userId)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<CrisJob> getJobs(String userId, String validationStatus) {
        if (validationStatus.equals("all")) {
            return getJobs(userId);
        }
        List<CrisJob> jobsList = new LinkedList<>();
        for (Map.Entry<String, CrisJob> entry : jobs.entrySet()) {
            if (entry.getValue().getAdmin().equals(userId) && entry.getValue().getStatus().equals(validationStatus)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<CrisJob> getJobs(String userId, int offset, int size) {
        List<CrisJob> allUserCrisJobs = getJobs(userId);
        int to = offset + size;
        return allUserCrisJobs.subList(offset, to > allUserCrisJobs.size() ? allUserCrisJobs.size() : to);
    }

    @Override
    public List<CrisJob> getJobs(String userId, Date dateFrom, Date dateTo) {
        List<CrisJob> jobsList = new LinkedList<>();
        for (Map.Entry<String, CrisJob> entry : jobs.entrySet()) {
            if (entry.getValue().getAdmin().equals(userId)
                    && entry.getValue().getDateSubmitted().after(dateFrom)
                    && entry.getValue().getDateSubmitted().before(dateTo)) {
                jobsList.add(entry.getValue());
            }
        }
        return jobsList;
    }

    @Override
    public List<CrisJob> getJobs(String userId, int offset, int size, Date dateFrom, Date dateTo, String validationStatus) {
        List<CrisJob> jobsList = new LinkedList<>();
        for (Map.Entry<String, CrisJob> entry : jobs.entrySet()) {
            if (entry.getValue().getAdmin().equals(userId)
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
    public List<CrisJob> getAll() {
        return new LinkedList<>(jobs.values());
    }

    @Override
    public CrisJob save(CrisJob t) {
        return jobs.put(String.valueOf(t.getId()), t);
    }

    @Override
    public void delete(CrisJob t) {
        if (jobs.containsKey(t.getId())) {
            jobs.remove(t);
        }
    }
}
