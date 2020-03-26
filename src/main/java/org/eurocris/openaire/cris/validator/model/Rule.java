package org.eurocris.openaire.cris.validator.model;

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
