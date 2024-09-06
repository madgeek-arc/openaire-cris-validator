package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.RuleResults;

import java.util.List;

public interface TaskListener {

    /**
     * Task started.
     *
     * @param results
     */
    void started(final List<RuleResults> results);

    /**
     * Task updated.
     *
     * @param results
     */
    void updated(final List<RuleResults> results);

    /**
     * Task finished. Handle results {@param results}.
     *
     * @param results
     */
    void finished(final List<RuleResults> results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param results
     */
    void failed(final List<RuleResults> results);

}
