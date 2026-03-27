package com.thaca.framework.core.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Locale;

public class LowerCaseTrimDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String value = p.getText();
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
