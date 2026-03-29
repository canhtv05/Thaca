package com.thaca.framework.core.utils.json;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class InstantToStringSerializer extends StdSerializer<Instant> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(
        ZoneId.systemDefault()
    );

    public InstantToStringSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext context) {
        if (value != null) {
            gen.writeString(formatter.format(value));
        } else {
            gen.writeNull();
        }
    }
}
