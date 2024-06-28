package org.eurocris.openaire.cris.validator.model;

import java.util.Objects;

public class Rule {
    private int id = -1;
    private String name;
    private String ruleMethodName;
    private float weight;
    private String description;
    private String type;

    public Rule() {

    }

    public Rule(int id, String name, String ruleMethodName, float weight, String description, String type) {
        this.id = id;
        this.name = name;
        this.ruleMethodName = ruleMethodName;
        this.weight = weight;
        this.description = description;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Rule rule = (Rule) object;
        return id == rule.id && Float.compare(weight, rule.weight) == 0 && Objects.equals(name, rule.name) && Objects.equals(ruleMethodName, rule.ruleMethodName) && Objects.equals(description, rule.description) && Objects.equals(type, rule.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ruleMethodName, weight, description, type);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuleMethodName() {
        return ruleMethodName;
    }

    public void setRuleMethodName(String ruleMethodName) {
        this.ruleMethodName = ruleMethodName;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
