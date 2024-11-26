package org.eurocris.openaire.cris.validator.model;

import java.util.ArrayList;
import java.util.List;

public class RuleErrors {
    String name;
    Long failed;
    List<ValidationError> errors = new ArrayList<>();
}
