package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.listener.StatusListener;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("crisValidatorExecutor")
public class CRISValidatorExecutor implements JobExecutor {

    private static final Logger logger = LogManager.getLogger(CRISValidatorExecutor.class);
    private ExecutorService executor;
    private JobDao dao;


    @Autowired
    public CRISValidatorExecutor(@Value("${executor.threads:8}") int threadNum, JobDao dao) {
        executor = Executors.newFixedThreadPool(threadNum);
        this.dao = dao;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    @Override
    public Optional<Job> getJob(String jobId) {
        return dao.get(jobId);
    }

    @Override
    public String getStatus(String jobId) {
        if (dao.get(jobId).isPresent()) {
            return dao.get(jobId).get().getStatus();
        }
        return "";
    }

    @Override
    public Job submit(Job job) {
        TaskListener listener = new StatusListener(job, dao);
        executor.submit(() -> new CRISValidatorTask(job, listener).run());
        return job;
    }

    @Override
    public Job submit(String url, String user) {
        Job job = new Job(url, user);
        return submit(job);
    }
}
