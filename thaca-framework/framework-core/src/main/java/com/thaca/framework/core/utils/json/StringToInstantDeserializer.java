package com.thaca.framework.core.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.thaca.framework.core.utils.DateUtils;
import java.io.IOException;
import java.time.Instant;

public class StringToInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext context) throws IOException {
        return DateUtils.stringToDate(p.getValueAsString());
    }
}
