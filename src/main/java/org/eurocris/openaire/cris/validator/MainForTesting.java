package org.eurocris.openaire.cris.validator;

import org.eurocris.openaire.cris.validator.listener.StatusListener;
import org.eurocris.openaire.cris.validator.listener.TaskListener;
import org.eurocris.openaire.cris.validator.model.Job;
import org.eurocris.openaire.cris.validator.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class containing a main method for testing/debugging the Validator.
 */
public class MainForTesting {

    public static final Logger logger = LoggerFactory.getLogger(MainForTesting.class);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        JobDao dao = new MapJobDao();
        RuleDao ruleDao = new MapRuleDao();

        String[] urls = new String[]{
                "https://pure.eur.nl/ws/oai",
                "https://virta-jtp.csc.fi/api/cerif"
//                "https://dwitjutife1.csc.fi/api/cerif",
//                "https://virta-jtp.csc.fi/api/cerif",
//                "https://devel.atira.dk/eurocris/ws/oai",
//                "https://devel.atira.dk/eurocris/ws/oai",
//                "http://services.nod.dans.knaw.nl/oa-cerif",
//                "https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler"
        };

        for (String url : urls) {
            Job job = new Job(url, "me");
            TaskListener listener = new StatusListener(job, dao);
            executor.submit(() -> new CRISValidatorTask(job, dao, ruleDao, listener).run());
        }

        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
            dao.getAll().forEach(job -> logger.info(job.getStatus()));
        } catch (InterruptedException e) {
            logger.error("ERROR", e);
            throw e;
        }

        // shut down the executor manually
        executor.shutdown();
        StringBuilder report = new StringBuilder();
        for (Job job : dao.getAll()) {
            report.append(String.format("%nJob [%s]%nurl:\t\t\t%s%nuser:\t\t\t%s%nstatus:\t\t\t%s%nusage score:\t\t%s%ncontent score:\t\t%s%ndate submitted:\t%s%ndate started:\t%s%ndate finished:\t%s%n%n%n",
                    job.getId(), job.getUrl(), job.getUser(), job.getStatus(), job.getUsageScore(), job.getContentScore(), job.getDateSubmitted(), job.getDateStarted(), job.getDateFinished()));
        }
        logger.info("{}", report);
    }
}
