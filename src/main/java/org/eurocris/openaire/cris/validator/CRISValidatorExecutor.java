package org.eurocris.openaire.cris.validator;

import java.util.concurrent.*;

public class CRISValidatorExecutor {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10); // TODO: set number of threads

        // Runnable, return void, nothing, submit and run the task async
//        executor.submit(() -> new CRISValidatorTask("TEST").call());
        executor.submit(() -> new CRISValidatorTask("https://dwitjutife1.csc.fi/api/cerif").run());
        executor.submit(() -> new CRISValidatorTask("https://dwitjutife1.csc.fi/api/cerif").run());
        executor.submit(() -> new CRISValidatorTask("https://devel.atira.dk/eurocris/ws/oai").run());
        executor.submit(() -> new CRISValidatorTask("https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler").run());

        // Callable, return a future, submit and run the task async
//        Future<String> futureTask1 = executor.submit(() -> new CRISValidatorTask("https://dwitjutife1.csc.fi/api/cerif").call());
//        Future<String> futureTask2 = executor.submit(() -> new CRISValidatorTask("https://oamemtfa.uci.ru.nl/metis-oaipmh-endpoint/OAIHandler").call());
//        Future<String> futureTask1 = executor.submit(() -> new CRISValidatorTask("TEST Callable").call());
//        Future<Integer> futureTask1 = executor.submit(() -> {
//            new CRISValidatorTask("testCallable");
//            return 1 + 1;
//        });

        try {

            otherTask("Before Future Result");

            // block until future returned a result,
            // timeout if the future takes more than 5 seconds to return the result
//            String result = futureTask1.get(5, TimeUnit.SECONDS);
//            String result2 = futureTask2.get(5, TimeUnit.SECONDS);


//            System.out.println("Get future result : " + result);
//            System.out.println("Get future result 2 : " + result2);

            otherTask("After Future Result");


//        } catch (InterruptedException e) {// thread was interrupted
//            e.printStackTrace();
//        } catch (ExecutionException e) {// thread threw an exception
//            e.printStackTrace();
//        } catch (TimeoutException e) {// timeout before the future task is complete
//            e.printStackTrace();
        } finally {

            // shut down the executor manually
            executor.shutdown();

        }

    }

    private static void otherTask(String name) {
        System.out.println("I'm other task! " + name);
    }

}
