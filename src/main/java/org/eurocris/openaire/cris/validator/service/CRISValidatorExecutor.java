package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.listener.StatusListener;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.CrisJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("crisValidatorExecutor")
public class CRISValidatorExecutor implements JobExecutor {

    private static final Logger logger = LogManager.getLogger(CRISValidatorExecutor.class);
    private ExecutorService executor;
    private final JobDao jobDao;


    public CRISValidatorExecutor(@Value("${executor.threads:8}") int threadNum, Map<String, JobDao> jobDao) {
        executor = Executors.newFixedThreadPool(threadNum);
        if (jobDao.containsKey("DBJobDao")) {
            this.jobDao = jobDao.get("DBJobDao");
        } else {
            this.jobDao = jobDao.get("mapJobDao");
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    @Override
    public Optional<CrisJob> getJob(Long jobId) {
        return jobDao.get(jobId);
    }

    @Override
    public String getStatus(Long jobId) {
        if (jobDao.get(jobId).isPresent()) {
            return jobDao.get(jobId).get().getStatus();
        }
        return "";
    }

    @Override
    public CrisJob submit(CrisJob crisJob) {
        TaskListener listener = new StatusListener(crisJob, jobDao);
        executor.submit(() -> new CRISValidatorTask(crisJob, jobDao, listener).run());
        return crisJob;
    }

    @Override
    public CrisJob submit(String url, String user) {
        CrisJob crisJob = new CrisJob(url, user);
        return submit(crisJob);
    }
}
