package org.eurocris.openaire.cris.validator;

import org.apache.commons.cli.MissingArgumentException;
import org.junit.runner.JUnitCore;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import static org.eurocris.openaire.cris.validator.CRISValidator.CONN_STREAM_FACTORY;
import static org.eurocris.openaire.cris.validator.CRISValidator.getParserSchema;

//public class CRISValidatorTask implements Callable<CRISValidator> {
public class CRISValidatorTask implements Runnable {

    private String endpoint;

    CRISValidatorTask(String endpoint) {
        this.endpoint = endpoint;
    }

//    @Override
////    public CRISValidator call() throws Exception {
//    public String call() throws Exception {
//        CRISValidator.getEndpoint().set(new OAIPMHEndpoint(new URL(endpoint), getParserSchema(), CONN_STREAM_FACTORY));
//        JUnitCore.main(CRISValidator.class.getName());
//        return endpoint;
//    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        try {
            CRISValidator.getEndpoint().set(new OAIPMHEndpoint(new URL(endpoint), getParserSchema(), new FileLoggingConnectionStreamFactory( "data/" + endpoint )));
            CRISValidator object = new CRISValidator();
            object.executeTests();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (MissingArgumentException e) {
            e.printStackTrace();
        }

    }
}
