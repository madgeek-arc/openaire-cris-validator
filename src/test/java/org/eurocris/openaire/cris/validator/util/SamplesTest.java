package org.eurocris.openaire.cris.validator.util;

import org.eurocris.openaire.cris.validator.CRISValidator;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;

/**
 * The test suite to test the OpenAIRE Guidelines 1.1 set of samples.
 */
public class SamplesTest extends CRISValidator {

    @SuppressWarnings("javadoc")
    public SamplesTest() throws MalformedURLException, SAXException {
        super("file:samples/", "test", null);
    }

}
