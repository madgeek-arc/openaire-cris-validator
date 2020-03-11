package org.eurocris.openaire.cris.validator.listener;

import org.eurocris.openaire.cris.validator.model.ValidationResults;

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
    void finished(ValidationResults results);

    /**
     * Task failed. Handle errors {@param errors}.
     *
     * @param errors
     */
    void failed(ValidationResults errors);

}
