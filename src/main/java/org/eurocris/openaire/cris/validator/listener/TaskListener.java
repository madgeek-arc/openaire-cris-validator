package org.eurocris.openaire.cris.validator.listener;

public interface TaskListener<T> {

    /**
     * Task started.
     */
    void started();

    /**
     * Task finished. Handle results {@param results}.
     *
     * @param results
     */
    void finished(T results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param errors
     */
    void failed(T errors);

}
