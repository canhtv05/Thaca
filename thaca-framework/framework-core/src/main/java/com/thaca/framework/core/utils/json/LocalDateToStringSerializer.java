package com.thaca.framework.core.utils.json;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class LocalDateToStringSerializer extends StdSerializer<LocalDate> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LocalDateToStringSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializationContext context) {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(DATE_FORMATTER.format(value));
        }
    }
}
