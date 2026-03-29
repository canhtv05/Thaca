package com.thaca.framework.core.utils.json;

import com.thaca.framework.core.utils.DateUtils;
import java.time.Instant;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class StringToInstantDeserializer extends StdDeserializer<Instant> {

    public StringToInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext context) {
        return DateUtils.stringToDate(p.getValueAsString());
    }
}
