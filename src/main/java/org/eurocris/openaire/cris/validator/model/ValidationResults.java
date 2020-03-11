package org.eurocris.openaire.cris.validator.model;

import java.util.*;

public class ValidationResults implements Map<String, RuleResults> {

    private Map<String, RuleResults> ruleResultsMap = new LinkedHashMap<>();


    public ValidationResults() {
    }

    public Map<String, RuleResults> getRuleResultsMap() {
        return ruleResultsMap;
    }

    public void setRuleResultsMap(Map<String, RuleResults> ruleResultsMap) {
        this.ruleResultsMap = ruleResultsMap;
    }

    public Collection<RuleResults> getResultEntries() {
        return this.ruleResultsMap.values();
    }

    @Override
    public int size() {
        return this.ruleResultsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ruleResultsMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.ruleResultsMap.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.ruleResultsMap.containsValue(o);
    }

    @Override
    public RuleResults get(Object o) {
        return this.ruleResultsMap.get(o);
    }

    @Override
    public RuleResults put(String s, RuleResults ruleResults) {
        return this.ruleResultsMap.put(s, ruleResults);
    }

    @Override
    public RuleResults remove(Object o) {
        return this.ruleResultsMap.remove(o);
    }

    @Override
    public void putAll(Map<? extends String, ? extends RuleResults> map) {
        this.ruleResultsMap.putAll(map);
    }

    @Override
    public void clear() {
        this.ruleResultsMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.ruleResultsMap.keySet();
    }

    @Override
    public Collection<RuleResults> values() {
        return this.ruleResultsMap.values();
    }

    @Override
    public Set<Entry<String, RuleResults>> entrySet() {
        return this.ruleResultsMap.entrySet();
    }
}
