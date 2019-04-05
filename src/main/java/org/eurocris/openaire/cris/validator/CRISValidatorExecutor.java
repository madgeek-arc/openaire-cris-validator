package org.eurocris.openaire.cris.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.listeners.StatusListener;
import org.eurocris.openaire.cris.validator.listeners.TaskListener;
import org.eurocris.openaire.cris.validator.service.JobDao;
import org.eurocris.openaire.cris.validator.service.MapJobDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CRISValidatorExecutor {

    private static final Logger logger = LogManager.getLogger(CRISValidatorExecutor.class);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2); // TODO: set number of threads

        JobDao dao = new MapJobDao();

        String[] urls = new String[]{
                "https://dwitjutife1.csc.fi/api/cerif",
                "https://devel.atira.dk/eurocris/ws/oai",
                "https://devel.atira.dk/eurocris/ws/oai",
                "http://services.nod.dans.knaw.nl/oa-cerif",
                "https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler"
        };

        for (String url : urls) {
            Job job = new Job(url, "me");
            TaskListener listener = new StatusListener(job, dao);
            executor.submit(() -> new CRISValidatorTask(job, listener).run());
        }

        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
            dao.getAll().forEach(job -> System.out.println(job.getStatus()));
        } catch (InterruptedException e) {
            logger.error("ERROR", e);
        }

        // shut down the executor manually
        executor.shutdown();
        StringBuilder report = new StringBuilder();
        for (Job job : dao.getAll()) {
            report.append(String.format("%nJob [%s]%nurl:\t\t\t%s%nuser:\t\t\t%s%nstatus:\t\t\t%s%ndate submitted:\t%s%ndate started:\t%s%ndate finished:\t%s%n%n%n",
                    job.getId(), job.getUrl(), job.getUser(), job.getStatus(), job.getDateSubmitted(), job.getDateStarted(), job.getDateFinished()));
        }
        logger.info(report);
    }
}
