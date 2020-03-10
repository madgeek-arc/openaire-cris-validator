package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.util.ValidatorRuleResults;

import java.util.Map;

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
    void finished(Map<String, ValidatorRuleResults> results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param errors
     */
    void failed(Map<String, ValidatorRuleResults> errors);

}
