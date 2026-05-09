package com.thaca.common.dtos.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResponseDTO {

    private int totalRows;
    private int successCount;
    private int errorCount;
    private boolean hasErrors;
    private List<ImportErrorDTO> errors;
    private List<Map<String, Object>> preview;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportErrorDTO {

        private int row;
        private String column;
        private String columnKey;
        private String message;
        private String value;
    }
}
