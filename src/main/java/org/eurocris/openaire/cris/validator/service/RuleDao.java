package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.Rule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RuleDao {

    /**
     * Get the {@link Rule} of the method with name {@param ruleMethodName}.
     *
     * @param ruleMethodName
     * @return {@link Optional <Rule>}
     */
    Optional<Rule> get(String ruleMethodName);

    /**
     * Get all Rules.
     *
     * @return {@link List < Rule >}
     */
    List<Rule> getAll();

    /**
     * Get all Rules in map.
     *
     * @return {@link Map < String, Rule >}
     */
    Map<String, Rule> getRuleMap();

    /**
     * Saves the {@link Rule} {@param t}.
     *
     * @param ruleMethodName
     * @param t
     */
    void save(String ruleMethodName, Rule t);

    /**
     * Deletes the {@link Rule} for the method with name {@param ruleMethodName}.
     *
     * @param ruleMethodName
     */
    void delete(String ruleMethodName);
}
