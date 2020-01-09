package org.eurocris.openaire.cris.validator.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class PropertiesUtils {

    private static final Logger logger = LogManager.getLogger(PropertiesUtils.class);

    public static String getProperty(String property, String propertiesPath) {
        Properties properties = new Properties();
        try (InputStream is = PropertiesUtils.class.getResourceAsStream(propertiesPath)) {
            properties.load(is);
        } catch (IOException e) {
            logger.error(e);
        }
        return properties.getProperty(property);
    }

    public static Map<String, Float> getRuleWeights(String propertiesPath) {
        Map<String, Float> ruleWeights = new TreeMap<>();
        String weights = getProperty("rule.weights", propertiesPath);
        weights = weights.replaceAll("[{}\\s]", "");
        for (String weight : weights.split(",")) {
            weight = weight.replace("'", "");
            String[] ruleValue = weight.split(":");
            if (ruleValue.length != 2) {
                logger.warn("Error in property 'rule.weights' : '{}'.  Using default value", weight);
            } else {
                ruleWeights.put(ruleValue[0], Float.parseFloat(ruleValue[1]));
            }
        }
        return ruleWeights;
    }

    private PropertiesUtils() {}
}
