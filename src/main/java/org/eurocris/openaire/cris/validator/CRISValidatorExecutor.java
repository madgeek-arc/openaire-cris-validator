package org.eurocris.openaire.cris.validator;

import java.util.concurrent.*;

public class CRISValidatorExecutor {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10); // TODO: set number of threads


        executor.submit(() -> new CRISValidatorTask("https://dwitjutife1.csc.fi/api/cerif").run());
//        executor.submit(() -> new CRISValidatorTask("https://dwitjutife1.csc.fi/api/cerif").run());
//        executor.submit(() -> new CRISValidatorTask("https://devel.atira.dk/eurocris/ws/oai").run());
//        executor.submit(() -> new CRISValidatorTask("https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler").run());

        executor.shutdown();



    }

}
