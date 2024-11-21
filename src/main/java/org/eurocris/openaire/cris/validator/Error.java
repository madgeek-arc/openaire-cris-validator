package org.eurocris.openaire.cris.validator;

public enum Error {
    SET_SPEC_NOT_UNIQUE("setSpec not unique"),

    METADATA_FORMAT_MISSING("Metadata format for the OpenAIRE Guidelines for CRIS Managers not present (2a)"),
    METADATA_PREFIX_EMPTY("No metadata prefix"),
    METADATA_PREFIX_INVALID("Invalid metadata prefix"),
    METADATA_PREFIX_NOT_UNIQUE("Metadata prefix not unique (2d)"),
    METADATA_NAMESPACE_NOT_UNIQUE("Metadata namespace not unique (2e)"),
    METADATA_SCHEMA_LOCATION_NOT_UNIQUE("Metadata schema location not unique (2f)"),
    METADATA_FORMAT_UNSPECIFIED("No metadata format specified"),

    OAI_IDENTIFIER_UNEXPECTED("OAI identifier other than expected"),
    OAI_IDENTIFIER_MISSING("Identify descriptions list (1b) does not contain an 'oai-identifier' element"),
    OAI_IDENTIFIER_MULTIPLE("Identify descriptions list (1b) contains multiple instances of an 'oai-identifier' element"),

    SERVICE_ELEMENT_MISSING("Identify descriptions list (1a) does not contain a 'Service' element"),
    SERVICE_ELEMENT_MULTIPLE("Identify descriptions list (1a) contains multiple instances of a 'Service' element"),

    INVALID_SET_NAME("Non-matching set name (3)"),
    MISSING_SET_NAME("Set not present (3)"),
    INVALID_SERVICE_ACRONYM("Service acronym is not the same as the repository identifier (1c)"),
    INVALID_BASE_URL("Identify response has a different endpoint base URL (1d)"),

    INVALID_PAYLOAD_NAMESPACE("The payload element not in the right namespace"),
    INVALID_PAYLOAD_LOCAL_NAME("The payload element does not have the right local name"),

    EXPECTED_VALUE_MISSING("Missing value"),
    UNEXPECTED_VALUE("Unexpected value"),
    CONSTRAINT_VIOLATION("Constraint Violation"),
    METADATA_MISSING("Metadata missing from OAI-PMH record"),
    IDENTIFIER_NOT_UNIQUE("Record identifier not unique"),
    VIOLATION_5A_REVERENTIAL_INTEGRITY("Referential integrity violation"),
    VIOLATION_5B(""),

    GENERAL_ERROR("General error");

    private final String message;

    Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
