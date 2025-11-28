package it.univaq.microsynth.Enum;

import org.bson.json.JsonObject;

public enum ParameterType {
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    OBJECT,
    ARRAY,
    JSON;

    public static ParameterType fromValue(Object value) {
        if (value instanceof String) return STRING;
        if (value instanceof Integer) return INTEGER;
        if (value instanceof Float || value instanceof Double) return FLOAT;
        if (value instanceof Boolean) return BOOLEAN;
        if (value instanceof java.util.List<?>) return ARRAY;

        if (value instanceof java.util.Map<?, ?> ||
                value instanceof JsonObject) {
            return JSON;
        }

        return OBJECT;
    }
}
