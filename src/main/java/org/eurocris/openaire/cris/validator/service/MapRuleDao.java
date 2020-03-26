package org.eurocris.openaire.cris.validator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.eurocris.openaire.cris.validator.model.Rule;
import org.eurocris.openaire.cris.validator.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class MapRuleDao implements RuleDao {

    private static final Logger logger = LogManager.getLogger(MapRuleDao.class);
    private Map<String, Rule> rules = new LinkedHashMap<>();

    @Value("#{${rule.weights}}")
    private Map<String, Float> ruleWeights;

    @Value("#{${rule.names}}")
    private Map<String, String> ruleNames;

    @Value("#{${rule.descriptions}}")
    private Map<String, String> ruleDescriptions;

    public MapRuleDao() {
        int i = -1;
        Map<String, Float> weights = PropertiesUtils.getRuleWeights("/cris.properties");
        Map<String, String> descriptions = PropertiesUtils.getRuleDescriptions("/cris.properties");
        for (String method : weights.keySet()) {
            Rule rule = new Rule(i, method, method, weights.get(method), descriptions.get(method), CRISValidator.methodsMap.get(method));
            this.rules.put(method, rule);
            i--;
            logger.info("Creating new rule for {}: {}", method, rule);
        }
    }

    @PostConstruct
    void createRules() {
        int i = -1;
        for (String method : ruleWeights.keySet()) {
            Rule rule = new Rule(i, ruleNames.get(method), method, ruleWeights.get(method), ruleDescriptions.get(method), CRISValidator.methodsMap.get(method));
            this.rules.put(method, rule);
            i--;
            logger.info("Creating new rule for {}: {}", method, rule);
        }
    }

    @Override
    public Optional<Rule> get(String ruleMethodName) {
        return Optional.of(this.rules.get(ruleMethodName));
    }

    @Override
    public List<Rule> getAll() {
        return new LinkedList<>(this.rules.values());
    }

    @Override
    public Map<String, Rule> getRuleMap() {
        return this.rules;
    }

    @Override
    public void save(String ruleMethodName, Rule t) {
        this.rules.put(ruleMethodName, t);
    }

    @Override
    public void delete(String ruleMethodName) {
        this.rules.remove(ruleMethodName);
    }
}
