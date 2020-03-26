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
                logger.error("Error in property 'rule.weights' : '{}'.", weight);
            } else {
                ruleWeights.put(ruleValue[0], Float.parseFloat(ruleValue[1]));
            }
        }
        return ruleWeights;
    }

    public static Map<String, String> getRuleDescriptions(String propertiesPath) {
        Map<String, String> ruleDescriptions = new TreeMap<>();
        String descriptions = getProperty("rule.descriptions", propertiesPath);

        for (String desc : descriptions.split(",")) {
            desc = desc.replace("'", "");
            String[] ruleValue = desc.split(":");
            if (ruleValue.length != 2) {
                logger.error("Error in property 'rule.descriptions' : '{}'.", desc);
            } else {
                ruleDescriptions.put(ruleValue[0], ruleValue[1]);
            }
        }
        return ruleDescriptions;
    }

    private PropertiesUtils() {}
}
