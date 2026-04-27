package com.thaca.common.excel.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Defines a single column in an Excel schema.
 * Uses builder pattern for fluent configuration — no annotations, no reflection.
 */
public class ExcelColumn {

    private final String key;
    private final String header;
    private final boolean required;
    private final ExcelDataType dataType;
    private final Integer maxLength;
    private final Double minValue;
    private final Double maxValue;
    private final List<String> allowedValues;
    private final Function<String, Object> valueMapper;
    private final BiFunction<Object, RowContext, String> customValidator;
    private final String comment;
    private final String dateFormat;

    private ExcelColumn(Builder builder) {
        this.key = builder.key;
        this.header = builder.header;
        this.required = builder.required;
        this.dataType = builder.dataType;
        this.maxLength = builder.maxLength;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.allowedValues = builder.allowedValues;
        this.valueMapper = builder.valueMapper;
        this.customValidator = builder.customValidator;
        this.comment = builder.comment;
        this.dateFormat = builder.dateFormat;
    }

    public String getKey() {
        return key;
    }

    public String getHeader() {
        return header;
    }

    public boolean isRequired() {
        return required;
    }

    public ExcelDataType getDataType() {
        return dataType;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public Function<String, Object> getValueMapper() {
        return valueMapper;
    }

    public BiFunction<Object, RowContext, String> getCustomValidator() {
        return customValidator;
    }

    public String getComment() {
        return comment;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Returns the display header with required mark (*) if applicable.
     */
    public String getDisplayHeader() {
        return required ? header + " (*)" : header;
    }

    public static Builder builder(String key, String header) {
        return new Builder(key, header);
    }

    public static class Builder {

        private final String key;
        private final String header;
        private boolean required = false;
        private ExcelDataType dataType = ExcelDataType.STRING;
        private Integer maxLength;
        private Double minValue;
        private Double maxValue;
        private List<String> allowedValues;
        private Function<String, Object> valueMapper;
        private BiFunction<Object, RowContext, String> customValidator;
        private String comment;
        private String dateFormat = "yyyy-MM-dd";

        private Builder(String key, String header) {
            this.key = key;
            this.header = header;
        }

        public Builder required() {
            this.required = true;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder dataType(ExcelDataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minValue(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder maxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder range(double min, double max) {
            this.minValue = min;
            this.maxValue = max;
            return this;
        }

        public Builder allowedValues(List<String> allowedValues) {
            this.allowedValues = new ArrayList<>(allowedValues);
            return this;
        }

        public Builder allowedValues(String... values) {
            this.allowedValues = List.of(values);
            return this;
        }

        public Builder valueMapper(Function<String, Object> valueMapper) {
            this.valueMapper = valueMapper;
            return this;
        }

        public Builder customValidator(BiFunction<Object, RowContext, String> customValidator) {
            this.customValidator = customValidator;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public ExcelColumn build() {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("ExcelColumn key must not be blank");
            }
            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException("ExcelColumn header must not be blank");
            }
            return new ExcelColumn(this);
        }
    }
}
