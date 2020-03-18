package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.RuleResults;

import java.util.List;

public interface TaskListener {

    /**
     * Task started.
     */
    void started();

    /**
     * Task finished. Handle results {@param results}.
     *
     * @param results
     */
    void finished(List<RuleResults> results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param errors
     */
    void failed(List<RuleResults> errors);

}
