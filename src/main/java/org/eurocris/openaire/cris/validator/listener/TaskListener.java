package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.ValidationResults;

import java.util.List;

public interface TaskListener {

    /**
     * Task started.
     *
     * @param results
     */
    void started(final List<ValidationResults> results);

    /**
     * Task updated.
     *
     * @param results
     */
    void updated(final List<ValidationResults> results);

    /**
     * Task finished. Handle results {@param results}.
     *
     * @param results
     */
    void finished(final List<ValidationResults> results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param results
     */
    void failed(final List<ValidationResults> results);

}
