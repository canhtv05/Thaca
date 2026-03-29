package com.thaca.framework.core.utils.json;

import java.util.Locale;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class LowerCaseTrimDeserializer extends StdDeserializer<String> {

    public LowerCaseTrimDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext context) {
        String value = p.getValueAsString();
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
